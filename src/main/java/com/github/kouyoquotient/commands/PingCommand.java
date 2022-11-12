package com.github.kouyoquotient.commands;

import org.javacord.api.event.interaction.SlashCommandCreateEvent;
import org.javacord.api.listener.interaction.SlashCommandCreateListener;

public class PingCommand implements SlashCommandCreateListener {

    @Override
    public void onSlashCommandCreate(SlashCommandCreateEvent event) {
        if(event.getSlashCommandInteraction().getFullCommandName().equalsIgnoreCase("ping")){
            event.getInteraction().createImmediateResponder().setContent("pong!").respond();
        }
    }
}
