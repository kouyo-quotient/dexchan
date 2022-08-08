package com.github.kouyoquotient.commands;

import kong.unirest.Unirest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.listener.message.MessageCreateListener;

public class Poke implements MessageCreateListener {
    static final Logger logger = LogManager.getRootLogger();

    /*
     * TODO: Catch the chapters ID, then build the URL and send them into Discord.
     */
    @Override
    public void onMessageCreate(MessageCreateEvent event) {
        if (event.getMessageContent().equalsIgnoreCase("!poke")) {
            if (!event.getMessageAuthor().isBotOwner()) {
                event.getChannel().sendMessage("Comando restringido.");
                return;
            }

            logger.info("Received poke command instruction");

            Unirest.config().defaultBaseUrl("https://api.mangadex.org");
            String ping = String.valueOf(Unirest.get("/chapter?limit=32&offset=0&translatedLanguage[]=es-la&translatedLanguage[]=es&contentRating[]=safe&contentRating[]=suggestive&contentRating[]=erotica&contentRating[]=pornographic&order[readableAt]=desc")
                    .header("accept", "application/json")
                    .asJson().getBody());


            System.out.println(ping);
            Unirest.shutDown();
        }
    }
}
