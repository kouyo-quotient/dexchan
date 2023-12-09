package com.github.kouyoquotient.commands;

import kong.unirest.Unirest;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.event.interaction.SlashCommandCreateEvent;
import org.javacord.api.listener.interaction.SlashCommandCreateListener;

public class PingCommand implements SlashCommandCreateListener {

    @Override
    public void onSlashCommandCreate(SlashCommandCreateEvent event) {
        if(event.getSlashCommandInteraction().getFullCommandName().equalsIgnoreCase("ping")){
            Unirest.config().instrumentWith(requestSummary -> {
                long startMilis = System.currentTimeMillis();
                return (responseSummary,exception) ->
                        new MessageBuilder()
                                .append("Response summary:")
                                .append("\n- (MangaDexAPI) Status: "+responseSummary.getStatus())
                                .append("\n- (MangaDexAPI) Approx. response time: "+(System.currentTimeMillis()-startMilis)+"ms")
                                .send(event.getSlashCommandInteraction().getChannel().orElseThrow());
            });

            Unirest.config().defaultBaseUrl("https://api.mangadex.org");
            Unirest.get("/ping").asEmpty();
        }
    }
}
