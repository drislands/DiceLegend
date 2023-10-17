package com.islands.games.dicelegend.connectors

import com.islands.games.dicelegend.Player
import com.islands.games.dicelegend.meta.Printable
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.requests.GatewayIntent
import net.dv8tion.jda.api.utils.ChunkingFilter
import net.dv8tion.jda.api.utils.cache.CacheFlag

/**
 * Dedicated class for connecting as a configured Discord bot, and for facilitating the game loop.
 */
class DiscordBot implements Printable {
    // Discord bot stats
    static String token
    static JDA jda
    static String channel
    static TextChannel DUEL_CHANNEL
    static def admins

    // Duel stats
    static List<Player> players = []
    static Map<Player,Player> challenges = [:]

    //////////////////////////

    static def listener = new ListenerAdapter() {
        @Override
        void onMessageReceived(MessageReceivedEvent event) {
            if(event.message.contentRaw == "?SHUTDOWN") {
                if(event.author.name in admins) {
                    messageChannel("Bye!")
                    privateMessage(event.author, "Shutdown initiated.")
                    sleep(2500)
                    jda.shutdown()
                } else {
                    messageChannel("You aren't an admin!")
                }
            }
        }
    }

    //////////////////////////

    static void initBot(ConfigObject conf) {
        token = conf.token
        channel = conf.channel
        admins = conf.admins
    }

    static void lightBot() {
        jda = JDABuilder.createLight(token,
                GatewayIntent.GUILD_MESSAGES,
                GatewayIntent.MESSAGE_CONTENT,
                GatewayIntent.GUILD_MEMBERS)
                .addEventListeners(listener)
                .build()

        debug "Waiting til ready..."
        jda.awaitReady()
        debug "Ready!"

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

            addEventListeners(listener)

            setActivity(Activity.customStatus("Channeling the elements"))
        }

        jda = builder.build()

        debug "Waiting til ready..."
        jda.awaitReady()
        debug "Ready!"

        DUEL_CHANNEL = jda.getTextChannelsByName(channel,true)[0]
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
}
