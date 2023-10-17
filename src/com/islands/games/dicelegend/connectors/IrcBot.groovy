package com.islands.games.dicelegend.connectors

import com.islands.games.dicelegend.Duel
import com.islands.games.dicelegend.GameState
import com.islands.games.dicelegend.Player
import com.islands.games.dicelegend.exceptions.GameException
import com.islands.games.dicelegend.meta.Printable
import com.islands.games.dicelegend.moves.MoveParser
import org.pircbotx.Configuration
import org.pircbotx.PircBotX
import org.pircbotx.User
import org.pircbotx.cap.SASLCapHandler
import org.pircbotx.hooks.ListenerAdapter
import org.pircbotx.hooks.events.MessageEvent
import org.pircbotx.hooks.events.PrivateMessageEvent

import java.util.regex.Pattern

/**
 * Dedicated class for connecting to the configured IRC channel, and for facilitating the game loop.
 */
class IrcBot implements Printable {
    // IRC bot stats
    // TODO: make configurable at runtime
    static PircBotX bot
    static String channel
    static List<String> admins
    static String server
    static String botNick
    static SASLCapHandler saslHandler

    // Duel stats
    static List<Player> players = []
    static Map<Player,Player> challenges = [:]

    /**
     * The over-sized object that handles all incoming chats.
     */
    static def listener = new ListenerAdapter() {
        // This is for messages to the channel.
        @Override
        /**
         * Processes incoming messages sent to the channel.
         * @param event Object representing the incoming message.
         */
        void onMessage(MessageEvent event) throws Exception {
            debug "Channel message received!"
            def msg = event.message
            def user = event.user

            debug "MSG: $msg"
            debug "USR: $user"

            switch (msg) {
                case '?test':
                    debug "> Test command."
                    event.respond("Success!")
                    break
                case '?SHUTDOWN':
                    debug "> Shutdown command."
                    if (user.nick in admins) {
                        stopBot()
                    }
                    break
                case '?register':
                    debug "> Register command."
                    registerPlayer(user, event)
                    break
                case '?retract':
                    debug "> Retract command."
                    retractChallenge(user, event)
                    break
                case '?accept':
                    debug "> Accept command."
                    acceptChallenge(user, event)
                    break
                case '?reject':
                    debug "> Reject command."
                    rejectChallenge(user, event)
                    break
                default:
                    debug "> Other!"
                    parseOther(msg, user, event)
                    break
            }
        }

        // This is for private messages.
        @Override
        /**
         * Processes incoming messages sent to the bot in a private message.
         * @param event Object representing the incoming message.
         */
        void onPrivateMessage(PrivateMessageEvent event) throws Exception {
            debug "Private message received!"
            def msg = event.message
            def user = event.user

            if (!user) return // This makes sure we don't needlessly process server messages.
            debug "MSG: $msg"
            debug "USR: $user"

            if (msg in ['?moves', '?help']) {
                def names = MoveParser.moves.collect { it.name }
                event.respond("Here's a list of all moves available:")
                event.respond(names.join(", "))
                return
            }

            def move = MoveParser.getMove(msg)
            if (move) {
                def me = getPlayer(user)
                try {
                    if (Duel.setMove(me, move)) {
                        event.respond("Move set!")
                        if (Duel.gameState == GameState.READY_TO_PROCESS) {
                            messageChannel("Fighters have chosen their moves! Here are the results!")
                            if (Duel.processRound()) {
                                messageChannel("The fight is over!")
                                def winner = Duel.players.find { it.currentHealth }
                                if (!winner) {
                                    messageChannel("The result is a tie! Both fighters were defeated at the same time!!!")
                                } else {
                                    messageChannel("$winner.name, you are the winner! Congratulations!")
                                }
                                Duel.reset()
                            } else {
                                messageChannel("Current HP:")
                                Duel.players.each {
                                    messageChannel("* $it.name : $it.currentHealth")
                                }
                            }
                        }
                    } else {
                        event.respond("It's not time for you to send in a move!")
                    }
                } catch (GameException ignored) {
                    event.respond("You're not in a duel!")
                }
            } else {
                event.respond("That's not a valid move! Check your spelling?")
            }
        }
    }

    //////////////////////////

    static void initBot(ConfigObject conf) {
        channel = conf.channel
        admins = conf.admins
        server = conf.server
        botNick = conf.botNick

        ConfigObject sasl = conf.sasl
        if(sasl) {
            saslHandler = new SASLCapHandler(sasl.user,sasl.pass)
        }
    }

    //////////////////////////

    /**
     * Starts the bot with configured values.
     */
    static void startBot() {
        def builder = new Configuration.Builder()
                .setName(botNick)
                .addServer(server)
                .addAutoJoinChannel(channel)
                .addListener(listener)

        if(saslHandler) {
            builder.addCapHandler(saslHandler)
        }

        def conf = builder.buildConfiguration()

        println "Bot: $conf"
        bot = new PircBotX(conf)
        Thread.start {
            bot.startBot()
        }
        println "The bot was started!"
    }

    /**
     * Shuts the bot, and the program, down.
     */
    static void stopBot() {
        messageChannel('Bye for now!')
        bot.stopBotReconnect()
        bot.sendIRC().quitServer()
    }

    /**
     * Sends a message to the configured channel.
     * @param message
     */
    static void messageChannel(String message) {
        bot.sendIRC().message(channel,message)
    }

    //////////////////////////

    /**
     * Adds a new {@link com.islands.games.dicelegend.Player} to the list of registered {@link IrcBot#players}.
     * @param user The triggering user, to be made into a Player.
     * @param event The triggering event. Provided so the bot can easily reply.
     */
    static def registerPlayer(user,event) {
        def nick = user.nick
        if(nick in players*.name) {
            event.respond("There is already a registered player with your name!")
        } else {
            players << new Player(nick)
            event.respond("Success! You have registered as a new player.")
        }
    }

    /**
     * Retracts the user's challenge, if any.
     * @param user The triggering user.
     * @param event The triggering event. Provided so the bot can easily reply.
     */
    static def retractChallenge(user,event) {
        def me = getPlayer(user)
        if(me) {
            if(me in challenges.keySet()) {
                def you = challenges.remove(me)
                event.respond("Your challenge to $you.name has been retracted.")
            } else {
                event.respond("You have issued any challenges! You can do so with `?challenge <target>`.")
            }
        } else {
            event.respond("You aren't a registered player, there's nothing to retract!")
        }
    }

    /**
     * Accepts a challenge issued to the user, if any.
     * @param user The triggering user.
     * @param event The triggering event. Provided so the bot can easily reply.
     */
    static def acceptChallenge(user,event) {
        def me = getPlayer(user)
        if(me) {
            if(me in challenges.values()) {
                def you = challenges.find { k,v -> v == me }.key
                if(Duel.startDuel(you,me)) {
                    messageChannel("$you.name, your challenge to $user.nick has been accepted!")
                    messageChannel("A duel is now underway, between challenger $you.name and $user.nick!")
                    messageChannel("Fighters, privately message me the name of the move you want to use!")
                } else {
                    // TODO: queue for duels?
                    event.respond("There's already a duel underway! Wait til it's over to accept!")
                }
            } else {
                event.respond("You haven't been challenged, you goose!")
            }
        } else {
            event.respond("You aren't a registered player, there's nothing to retract!")
        }
    }

    /**
     * Rejects a waiting challenge, if any.
     * @param user The triggering user.
     * @param event The triggering event. Provided so the bot can easily reply.
     */
    static def rejectChallenge(user,event) {
        def me = getPlayer(user)
        if(me) {
            // TODO
        } else {
            event.respond("You aren't a registered player, there's nothing to reject!")
        }
    }

    /**
     * Method to handle messages sent that are not single-word commands.
     * @param message Text of the message.
     * @param user The triggering user.
     * @param event The triggering event. Provided so the bot can easily reply.
     */
    static def parseOther(message,user,event) {
        Map<Pattern,Closure> patterns = [:]
        // For when a challenge is issued.
        patterns[~/\?challenge .*/] = {
            if(getPlayer(user)) {
                def splits = message.split(' ')
                if (splits.size() > 2) {
                    event.respond("The `challenge` command only takes one argument: the person you're challenging.")
                } else {
                    String target = splits[1]
                    if (getPlayer(target)) {
                        def me = getPlayer(user)
                        def you = getPlayer(target)
                        if(me in challenges.keySet()) {
                            event.respond("You already have an open challenge to ${challenges[me].name}! Retract it with `?retract` or wait for them to respond!")
                        } else {
                            challenges[me] = you
                            messageChannel("$target, you have been issued a challenge by $user.nick! Accept or refuse with `?accept` or `?refuse`!")
                        }
                    } else {
                        event.respond("There is no registered player with name `$target`!")
                    }
                }
            } else {
                event.respond("You aren't a registered player! Do it with `?register`!")
            }
        }
        patterns[~/\?retr /] = {
            // TODO: anything? this was going to be /\?retract/ but that was moved to the main body
        }



        patterns.find { k, v ->
            debug ">> Checking pattern $k..."
            def valid = message ==~ k
            debug ">> Is valid? $valid"
            valid
        }.value.call()
    }

    //////////////////////////

    /**
     * Quick method to grab a Player by the User.
     */
    static Player getPlayer(User user) {
        players.find { it.name == user.nick }
    }
    /**
     * Quick method to grab a Player by the nick.
     */
    static Player getPlayer(String name) {
        players.find { it.name == name }
    }
}