package com.github.kouyoquotient.commands;

import kong.unirest.Unirest;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.listener.message.MessageCreateListener;

public class Ping implements MessageCreateListener {

    /*
     * TODO: Catch the chapters ID, then build the URL and send them into Discord.
     */
    @Override
    public void onMessageCreate(MessageCreateEvent event) {
        if (event.getMessageContent().equalsIgnoreCase("!poke")) {
            Unirest.config().defaultBaseUrl("https://api.mangadex.org");
            String ping = String.valueOf(Unirest.get("/chapter?limit=32&offset=0&translatedLanguage[]=es-la&translatedLanguage[]=es&contentRating[]=safe&contentRating[]=suggestive&contentRating[]=erotica&contentRating[]=pornographic&order[readableAt]=desc")
                    .header("accept", "application/json")
                    .asJson().getBody());


            //System.out.println(ping);

            event.getChannel().sendMessage(ping);
            Unirest.shutDown();
        }
    }
}
