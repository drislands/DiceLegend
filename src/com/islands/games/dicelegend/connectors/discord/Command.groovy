package com.islands.games.dicelegend.connectors.discord

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData

class Command {
    String name
    String description
    Closure extra

    Closure action

    static Command makeCommand(String name, String description,
            @DelegatesTo(SlashCommandData)Closure extra,
            @DelegatesTo(SlashCommandInteractionEvent)Closure action) {
        def c = new Command()
        c.name = name
        c.description = description
        c.extra = extra
        c.action = action

        c
    }

    static Command makeCommand(String name, String description,
            @DelegatesTo(SlashCommandInteractionEvent)Closure action) {
        makeCommand(name,description,null,action)
    }
}
