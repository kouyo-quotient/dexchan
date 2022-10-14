package com.github.kouyoquotient.commands;

import com.github.kouyoquotient.utils.JSONRead;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.listener.message.MessageCreateListener;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

import static com.github.kouyoquotient.Main.logger;

public class Pull implements MessageCreateListener {

    @Override
    public void onMessageCreate(MessageCreateEvent event) {
        if (event.getMessageContent().equalsIgnoreCase("!pull")) {
            if (!event.getMessageAuthor().isBotOwner()) {
                return;
            }

            logger.info("Received pull command instruction");

            Unirest.config().defaultBaseUrl("https://api.mangadex.org");
            HttpResponse<String> httpResponse = Unirest.get(
                            "/chapter?limit=32" +
                                    "&offset=0" +
                                    "&translatedLanguage[]=es-la" +
                                    "&translatedLanguage[]=es" +
                                    "&includes[]=user" +
                                    "&includes[]=scanlation_group" +
                                    "&includes[]=manga" +
                                    "&contentRating[]=safe" +
                                    "&contentRating[]=suggestive" +
                                    "&contentRating[]=erotica" +
                                    "&contentRating[]=pornographic" +
                                    "&order[readableAt]=desc")
                    .asString();


            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            JsonElement jsonElement = JsonParser.parseString(httpResponse.getBody());
            String niceJson = gson.toJson(jsonElement);

            Path pathToJsonFile = Path.of("response.json");

            logger.info("Writing API response to file...");
            try {
                Files.write(pathToJsonFile, niceJson.getBytes());
                logger.info("File successfully written.");
                JSONRead jsonRead = gson.fromJson(niceJson,JSONRead.class);
                ArrayList<Object> arrayList = jsonRead.getData();
                /*
                 * TODO:
                 *  Find a way to print only the chapter id.
                 */
                event.getChannel().sendMessage(pathToJsonFile.toFile());
            } catch (IOException e) {
                logger.error(e);
            }

            Unirest.shutDown();
        }
    }
}
