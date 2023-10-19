package com.islands.games.dicelegend

import com.islands.games.dicelegend.connectors.ConnectionMode
import com.islands.games.dicelegend.connectors.DiscordBot
import com.islands.games.dicelegend.connectors.IrcBot
import com.islands.games.dicelegend.meta.PrintManager
import com.islands.games.dicelegend.meta.Printable
import com.islands.games.dicelegend.moves.MoveParser
import groovy.cli.commons.CliBuilder


/**
 * The primary class for the project, initializing and configuring various elements and starting it all up.
 */
class DiceLegend implements Printable {
    static ConnectionMode CONNECTION_MODE = null

    static void main(args) {
        def conf = parseArgs(args)

        baseInit(conf)

        switch (CONNECTION_MODE) {
            case ConnectionMode.IRC:
                IrcBot.initBot(conf.irc)
                setPrintForIrc()
                IrcBot.startBot()
                break
            case ConnectionMode.DISCORD:
                DiscordBot.initBot(conf.discord)
                setPrintForDiscord()
                DiscordBot.lightBot()
                break
        }
    }

    //////////////////////////

    /**
     * Parse command-line arguments for various use.
     * @param args The args passed to the {@link #main} method.
     * @return A parsed ConfigObject to be subdivided as needed.
     */
    static ConfigObject parseArgs(args) {
        CliBuilder cli = new CliBuilder()

        cli.with {
            _(longOpt:'moves-list',args:1,
                    'Path to the file containing the list of moves.')
            c(longOpt:'conf',args:1,
                    'Path to the config file.')
            ///////
            // Any run must include exactly one of the following options.
            _(longOpt:'irc',
                    'Run the bot in IRC mode. Requires an irc block in the config file.')
            _(longOpt:'discord',
                    'Run the bot in Discord mode. Requires a discord block in the config file.')
            ///////
        }
        def opt = cli.parse(args)
        String movesPath = opt.'moves-list'
        String confPath  = opt.c
        MoveParser.parseMoveList(movesPath)
        if(opt.irc && opt.discord) {
            println "Specify IRC or Discord, one or the other. Cannot be both."
            cli.usage()
            System.exit(1)
        }
        if(opt.irc) {
            CONNECTION_MODE = ConnectionMode.IRC
        }
        if(opt.discord) {
            CONNECTION_MODE = ConnectionMode.DISCORD
        }

        if(!CONNECTION_MODE) {
            println "You must choose one of the connection types: --irc or --discord."
            cli.usage()
            System.exit(1)
        }

        def conf = new ConfigSlurper().parse(new File(confPath).text)

        conf
    }

    /**
     * Modify the {@link PrintManager#printOperation print operation} to send to the IRC channel.
     */
    static void setPrintForIrc() {
        PrintManager.printOperation = { String msg ->
            IrcBot.messageChannel(msg)
        }
    }

    static void setPrintForDiscord() {
        PrintManager.printOperation = { String msg ->
            DiscordBot.duelUpdate += "\n$msg"
        }
    }

    /**
     * Configure all classes that are not dependent on connection type.
     * @param conf The base {@link ConfigObject} read from the conf file.
     */
    static void baseInit(ConfigObject conf) {
        RNGesus.init(conf.rng)
    }

}
