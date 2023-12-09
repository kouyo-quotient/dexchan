package com.github.kouyoquotient.commands;

import kong.unirest.Empty;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.event.interaction.SlashCommandCreateEvent;
import org.javacord.api.listener.interaction.SlashCommandCreateListener;

public class PingCommand implements SlashCommandCreateListener {

    @Override
    public void onSlashCommandCreate(SlashCommandCreateEvent event) {
        if(event.getSlashCommandInteraction().getFullCommandName().equalsIgnoreCase("ping")){
            Unirest.config().defaultBaseUrl("https://api.mangadex.org");
            long startMilis = System.currentTimeMillis();
            HttpResponse<Empty> emptyRequest = Unirest.get("/ping").asEmpty();
            long endMilis = System.currentTimeMillis();

            long elapsedTime = endMilis - startMilis;

            String pingReply = String.valueOf(new MessageBuilder()
                    .append("Response summary:")
                    .append("\n- (MangaDexAPI) Status: "+emptyRequest.getStatus())
                    .append("\n- (MangaDexAPI) Approx. response time: "+elapsedTime+"ms").getStringBuilder());

            event.getInteraction().createImmediateResponder().setContent(pingReply).respond();
        }
    }
}
