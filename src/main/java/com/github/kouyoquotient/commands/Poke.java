package com.github.kouyoquotient.commands;

import kong.unirest.Unirest;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.listener.message.MessageCreateListener;

import static com.github.kouyoquotient.Main.logger;

public class Poke implements MessageCreateListener {

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
