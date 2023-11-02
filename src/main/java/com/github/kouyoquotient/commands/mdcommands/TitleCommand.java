package com.github.kouyoquotient.commands.mdcommands;

import com.google.gson.*;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.event.interaction.SlashCommandCreateEvent;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.listener.interaction.SlashCommandCreateListener;
import org.javacord.api.listener.message.MessageCreateListener;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

public class TitleCommand implements MessageCreateListener, SlashCommandCreateListener {
    private static final Logger logger = LogManager.getRootLogger();

    @Override
    public void onMessageCreate(MessageCreateEvent event) {
    }

    @Override
    public void onSlashCommandCreate(SlashCommandCreateEvent event) {
        if(event.getSlashCommandInteraction().getFullCommandName().equalsIgnoreCase("title")){
            logger.info("Received SlashCommand title");

            String UUIDValue = event.getSlashCommandInteraction()
                    .getArgumentStringValueByName("UUID")
                    .orElseThrow();

            Unirest.config().defaultBaseUrl("https://api.mangadex.org");
            HttpResponse<String> httpResponse = Unirest.get("/manga/" +
                            UUIDValue +
                            "?&includes[]=cover_art" +
                            "&includes[]=author" +
                            "&includes[]=artist")
                    .asString();

            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            JsonElement jsonElement = JsonParser.parseString(httpResponse.getBody());

            try {
                Path pathToJsonFile = Path.of("response.json");
                Files.write(pathToJsonFile, gson.toJson(jsonElement).getBytes());
                Object parsedJson = Configuration.defaultConfiguration().jsonProvider().parse(httpResponse.getBody());

                String mainTitle = JsonPath.read(parsedJson, "$.data.attributes.title.en");
                String description = JsonPath.read(parsedJson, "$.data.attributes.description.en");
                Object coverArtObject = JsonPath.read(parsedJson, "$.data.relationships[?(@.type == 'cover_art')].attributes.fileName");
                Object mangaAuthorObject = JsonPath.read(parsedJson, "$.data.relationships[?(@.type == 'author')].attributes.name");
                Object mangaArtistObject = JsonPath.read(parsedJson, "$.data.relationships[?(@.type == 'artist')].attributes.name");

                String coverArt = coverArtObject.toString().replaceAll("[\\[\\]\"]", "");
                String authorNames = mangaAuthorObject.toString().replaceAll("[\\[\\]\"]", "");
                String artistNames = mangaArtistObject.toString().replaceAll("[\\[\\]\"]", "");

                URL imageURL = new URL("https://uploads.mangadex.org/covers/"+UUIDValue+"/"+coverArt+".256.jpg");
                InputStream inputStream = imageURL.openStream();
                Path imagePath = Path.of("cover.jpg");
                Files.copy(inputStream, imagePath);
                inputStream.close();

                if (Files.exists(imagePath)) {
                    try {
                        logger.info("Leftover files found, deleting...");
                        Files.delete(imagePath);
                        logger.info("Files deleted.");
                    } catch (IOException e) {
                        logger.error(e);
                    }
                }


                EmbedBuilder responseEmbed = new EmbedBuilder()
                        .setTitle(mainTitle)
                        .setDescription(description)
                        .setAuthor("MangaDex", "https://mangadex.org/manga/"+UUIDValue,"https://cdn.discordapp.com/attachments/1000809614377500832/1169690488564088972/mangadex-logo.png")
                        .addField("Autores:", authorNames)
                        .addInlineField("Artistas:", artistNames)
                        .setThumbnail(new URL("https://uploads.mangadex.org/covers/"+UUIDValue+"/"+coverArt+".256.jpg").openStream());


                event.getInteraction()
                        .respondLater().thenAccept(interactionOriginalResponseUpdater -> {
                            interactionOriginalResponseUpdater.addEmbed(responseEmbed).update();
                        });

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
