package com.github.kouyoquotient.commands.mdcommands;

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
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

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

            try {
                Object parsedJson = Configuration.defaultConfiguration().jsonProvider().parse(httpResponse.getBody());

                String mainTitle = JsonPath.read(parsedJson, "$.data.attributes.title.en");
                String description = JsonPath.read(parsedJson, "$.data.attributes.description.en");
                Object coverArtObject = JsonPath.read(parsedJson, "$.data.relationships[?(@.type == 'cover_art')].attributes.fileName");
                Object mangaAuthorObject = JsonPath.read(parsedJson, "$.data.relationships[?(@.type == 'author')].attributes.name");
                Object mangaArtistObject = JsonPath.read(parsedJson, "$.data.relationships[?(@.type == 'artist')].attributes.name");

                String coverArtUUID = coverArtObject.toString().replaceAll("[\\[\\]\"]", "");
                String authorNames = mangaAuthorObject.toString().replaceAll("[\\[\\]\"]", "");
                String artistNames = mangaArtistObject.toString().replaceAll("[\\[\\]\"]", "");

                String coverArtURL = "https://uploads.mangadex.org/covers/"+UUIDValue+"/"+coverArtUUID+".256.jpg";
                URI uri = new URI(coverArtURL);
                URL coverArtURItoURL = uri.toURL();

                EmbedBuilder responseEmbed = new EmbedBuilder()
                        .setTitle(mainTitle)
                        .setDescription(description)
                        .setAuthor("MangaDex", "https://mangadex.org/manga/"+UUIDValue,"https://cdn.discordapp.com/attachments/1000809614377500832/1169690488564088972/mangadex-logo.png")
                        .addField("Autores:", authorNames)
                        .addInlineField("Artistas:", artistNames)
                        .setThumbnail(coverArtURItoURL.openStream());

                event.getInteraction()
                        .respondLater()
                        .thenAccept(interactionOriginalResponseUpdater ->
                                interactionOriginalResponseUpdater.addEmbed(responseEmbed)
                                        .update());

            } catch (IOException | URISyntaxException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
