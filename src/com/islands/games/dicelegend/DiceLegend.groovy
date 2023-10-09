package com.islands.games.dicelegend

import org.pircbotx.Configuration
import org.pircbotx.PircBotX
import org.pircbotx.hooks.ListenerAdapter
import org.pircbotx.hooks.types.GenericMessageEvent


class DiceLegend {

    static def listener = new ListenerAdapter() {
        @Override
        void onGenericMessage(GenericMessageEvent event) throws Exception {
            if(event.message.startsWith("?test")) {
                event.respond("Success!")
            }
        }
    }

    static void main(args) {
        startBot()
    }

    static void startBot() {
        def conf = new Configuration.Builder()
                .setName("stab-you-bot")
                .addServer("irc.libera.chat")
                .addAutoJoinChannel("#legendsoflinux")
                .addListener(listener)
                .buildConfiguration()

        def bot = new PircBotX(conf)
        Thread.start {
            bot.startBot()
        }
        println "The bot was started!"
    }
}
