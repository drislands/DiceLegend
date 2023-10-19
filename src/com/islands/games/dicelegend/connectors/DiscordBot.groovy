package com.islands.games.dicelegend.connectors

import com.islands.games.dicelegend.Duel
import com.islands.games.dicelegend.Player
import com.islands.games.dicelegend.connectors.discord.Command
import com.islands.games.dicelegend.meta.Printable
import com.islands.games.dicelegend.moves.Trait
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.interactions.commands.OptionMapping
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.requests.GatewayIntent
import net.dv8tion.jda.api.utils.ChunkingFilter
import net.dv8tion.jda.api.utils.cache.CacheFlag

/**
 * Dedicated class for connecting as a configured Discord bot, and for facilitating the game loop.
 */
class DiscordBot implements Printable {
    // Discord bot stats
    static String token
    static long guildId
    static JDA jda
    static String channel
    static TextChannel DUEL_CHANNEL
    static def admins
    static List<Command> slashCommands = []

    // Duel stats
    static Map<User,Player> players = [:]
    static Map<Player,Player> challenges = [:]

    //////////////////////////

    // Define slash commands.
    static {
        slashCommands << Command.makeCommand('ping',
                'Just a test command to confirm responsiveness.'){
            def time = System.currentTimeMillis()
            reply("Pong!").setEphemeral(true).flatMap {
                getHook().editOriginal("Pong: ${System.currentTimeMillis() - time}")
            }.queue()
        }
        slashCommands << Command.makeCommand('shutdown',
                'Shuts the bot down. Admin-only.'){
            if(user.name in admins) {
                reply("Oh dang you went and did it").queue()
                sleep(2500)
                jda.shutdown()
            } else {
                reply("You're not allowed!").queue()
            }
        }
        slashCommands << Command.makeCommand('register',
                'Registers you as a player.'){
            def me = user
            if(me in players) {
                reply("HEY! You're already registered, you goose!").queue()
            } else {
                def p = new Player(me.name)
                players[me] = p
                reply("Success! You have registered as a new player.").queue()
            }
        }
        slashCommands << Command.makeCommand('retract',
                "Retracts the challenge you've issued."){
            def me = getPlayer(user)
            if(me) {
                if(me in challenges.keySet()) {
                    def you = challenges.remove(me)
                    reply("Your challenge to $you.name has been retracted.").queue()
                } else {
                    reply("You have issued any challenges! You can do so with `?challenge <target>`.").queue()
                }
            } else {
                reply("You aren't a registered player, there's nothing to retract!").queue()
            }
        }
        slashCommands << Command.makeCommand('accept',
                'Accepts a challenge issued to you.'){
            def me = getPlayer(user)
            if(me) {
                if(me in challenges.values()) {
                    def you = challenges.find { k,v -> v == me }.key
                    if(Duel.startDuel(you,me)) {
                        reply("Challenge accepted!").queue()
                        messageChannel("$you.name, your challenge to $user.name has been accepted!")
                        messageChannel("A duel is now underway, between challenger $you.name and $user.name!")
                        messageChannel("Fighters, use `/move` to choose your move!")
                    } else {
                        // TODO: queue for duels?
                        reply("There's already a duel underway! Wait til it's over to accept!").queue()
                    }
                } else {
                    reply("You haven't been challenged, you goose!").queue()
                }
            } else {
                reply("You aren't a registered player, there's nothing to retract!").queue()
            }
        }
        slashCommands << Command.makeCommand('reject',
                'Rejects a challenge issued to you.'){
            def me = getPlayer(user)
            if(me) {
                // TODO
                reply("This function is a work in progress!").queue()
            } else {
                reply("You aren't a registered player, there's nothing to reject!").queue()
            }
        }

        //////////////////////////////

        slashCommands << Command.makeCommand('challenge',
                'Challenge another user to a duel!'){
            addOption(OptionType.USER,"user",
                    "The user to challenge",true)
        }{
            deferReply().queue()
            def me = getPlayer(user)
            def target = getOption("user",{it.getAsUser()})
            def you = getPlayer(target)

            if(me) {
                if(target) {
                    if(me in challenges.keySet()) {
                        reply("You already have an open challenge to ${challenges[me].name}! Retract it with `/retract` or wait for them to respond!").queue()
                    } else {
                        challenges[me] = you
                        reply("Challenge successfully issued!").queue()
                        messageChannel("$target.asTag, you have been issued a challenge by $member.nickname! Accept or refuse with `/accept` and `/reject`!")
                    }
                } else {
                    reply("That person isn't a registered player!").queue()
                }
            } else {
                reply("You aren't a registered player! Use `/register` to get registered!").queue()
            }
        }
        slashCommands << Command.makeCommand('practice',
                'Start a practice session with the bot.'){
            addOption(OptionType.STRING,"element",
                    "Optionally force the bot to use a specific element")
        }{
            deferReply().queue()
            def me = getPlayer(user)
            def target = getOption("element",{ it.getAsString() })
            def element = Trait.get(target)

            if(me) {
                if (target && !element) {
                    reply("`$target` doesn't match any known traits. Check your spelling?")
                } else {
                    if(me in challenges.values()) {
                        reply("You can't practice with an active challenge waiting! Either reject or accept it!").queue()
                    } else if(me in challenges.keySet()) {
                        reply("You can't practice with an active challenge waiting! Either retract it or wait " +
                                "for ${challenges[me].name} to accept!}").queue()
                    } else {
                        if(Duel.startDuel(me,element)) {
                            def you = Duel.trainingDummy
                            messageChannel("$member.nickname, your practice session ${element?"with dummy focused on $element moves ":''}is initiating!")
                            messageChannel("A practice duel is now underway, between challenger $me.name and $you.name!")
                            messageChannel("Fighter, use `/move` to choose your move!")
                        } else {
                            reply("There's already a duel underway! Wait til it's over to accept!").queue()
                        }
                    }
                }
            } else {
                reply("You aren't a registered player! Use `/register` to get registered!").queue()
            }
        }
        slashCommands << Command.makeCommand('move',
                "Choose your move") {
            addOption(OptionType.STRING,"move","The name of the move to pick",true)
        } {

        }
    }

    //////////////////////////

    static def messageListener = new ListenerAdapter() {
        @Override
        void onMessageReceived(MessageReceivedEvent event) {
            switch(event.message.contentRaw) {
                case '?SHUTDOWN':
                    if(event.author.name in admins) {
                        messageChannel("Bye!")
                        privateMessage(event.author, "Shutdown initiated.")
                        sleep(2500)
                        jda.shutdown()
                    } else {
                        messageChannel("You aren't an admin!")
                    }
                    break
                case '?COMMANDS':
                    messageChannel jda.retrieveCommands().complete().collect {
                        "$it.name"
                    }.join(", ")
            }
        }
    }

    static def slashListener = new ListenerAdapter() {
        @SuppressWarnings('GroovyAssignabilityCheck')
        @Override
        void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
            def command = slashCommands.find {
                it.name == event.name
            }

            command.action.delegate = event
            command.action()
        }
    }

    //////////////////////////

    static void initBot(ConfigObject conf) {
        token = conf.token
        channel = conf.channel
        admins = conf.admins
        guildId = conf.guildId
    }

    static void lightBot() {
        jda = JDABuilder.createLight(token,
                GatewayIntent.GUILD_MESSAGES,
                GatewayIntent.MESSAGE_CONTENT,
                GatewayIntent.GUILD_MEMBERS)
                .addEventListeners(messageListener)
                .addEventListeners(slashListener)
                .build()


        debug "Waiting til ready..."
        jda.awaitReady()
        debug "Ready!"

        setCommands()

        DUEL_CHANNEL = jda.getTextChannelsByName(channel,true)[0]
    }

    static void startBot() {
        def builder = JDABuilder.createDefault(token)

        builder.with {
            disableCache(
                    CacheFlag.MEMBER_OVERRIDES,
                    CacheFlag.VOICE_STATE,
                    CacheFlag.ACTIVITY
            )
            setBulkDeleteSplittingEnabled(false)
            setChunkingFilter(ChunkingFilter.NONE)
            disableIntents(GatewayIntent.GUILD_PRESENCES, GatewayIntent.GUILD_MESSAGE_TYPING)
            setLargeThreshold(50)

            addEventListeners(messageListener)
            addEventListeners(slashListener)

            setActivity(Activity.customStatus("Channeling the elements"))
        }

        jda = builder.build()

        debug "Waiting til ready..."
        jda.awaitReady()
        debug "Ready!"

        DUEL_CHANNEL = jda.getTextChannelsByName(channel,true)[0]
    }

    @SuppressWarnings('GroovyAssignabilityCheck')
    static void setCommands() {
        jda.getGuildById(guildId)
           .updateCommands().addCommands(
            slashCommands.collect { c ->
                def slash = Commands.slash(c.name,c.description)

                if(c.extra) {
                    c.extra.delegate = slash
                    c.extra()
                }

                slash
            }
        ).queue()
    }

    //////////////////////////

    static void messageChannel(String message) {
        DUEL_CHANNEL.sendMessage(message).queue()
    }

    static void privateMessage(User user,String message) {
        user.openPrivateChannel().flatMap {
            it.sendMessage(message)
        }.queue()
    }

    static Player getPlayer(User user) {
        players[user]
    }

    static Player getPlayer(String name) {
        players.find {k,v -> v.name == name }.value
    }
}
