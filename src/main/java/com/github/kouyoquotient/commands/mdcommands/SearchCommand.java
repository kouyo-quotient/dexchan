package com.github.kouyoquotient.commands.mdcommands;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import net.minidev.json.JSONArray;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.event.interaction.SlashCommandCreateEvent;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.listener.interaction.SlashCommandCreateListener;
import org.javacord.api.listener.message.MessageCreateListener;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.concurrent.TimeUnit;

public class SearchCommand implements SlashCommandCreateListener, MessageCreateListener {
    private static final Logger logger = LogManager.getRootLogger();

    @Override
    public void onSlashCommandCreate(SlashCommandCreateEvent slashCommandCreateEvent) {
        DiscordApi api = slashCommandCreateEvent.getApi();

        if (slashCommandCreateEvent.getSlashCommandInteraction().getFullCommandName().equalsIgnoreCase("search")) {
            if (slashCommandCreateEvent.getSlashCommandInteraction().getArgumentBooleanValueByName("Lista").isEmpty() || slashCommandCreateEvent.getSlashCommandInteraction().getArgumentBooleanValueByName("Lista").orElseThrow()) {
                /*
                /  Executes only if the "Lista" value on the slash command is either empty (true by default) or true (by user input).
                */
                logger.info("Received SlashCommand search");

                String searchValue = slashCommandCreateEvent.getSlashCommandInteraction()
                        .getArgumentStringValueByName("Nombre")
                        .orElseThrow();

                Unirest.config().defaultBaseUrl("https://api.mangadex.org");
                HttpResponse<String> httpResponse = Unirest.get("/manga/" +
                                "?title=" +
                                searchValue +
                                "&order[relevance]=desc")
                        .asString();

                Object parsedJson = Configuration.defaultConfiguration().jsonProvider().parse(httpResponse.getBody());

                Object mainTitles = JsonPath.read(parsedJson, "$.data[*].attributes.title.en");
                String[] values = mainTitles.toString().replaceAll("[\\[\\]\"]", "").split(",");
                Object titlesID = JsonPath.read(parsedJson, "$.data[*].id");

                JSONArray jsonArray = (JSONArray) titlesID;

                int arraySize = jsonArray.size();
                String[] titlesArray = new String[arraySize];

                for (int i = 0; i < arraySize; i++) {
                    titlesArray[i] = jsonArray.get(i).toString();
                }

                StringBuilder formattedResultTitles = new StringBuilder();
                for (int i = 0; i < values.length; i++) {
                    formattedResultTitles.append((i + 1)).append(". ").append(values[i]).append("\n");
                }

                String markdownResult = formattedResultTitles.toString();
                String result = StringEscapeUtils.unescapeJava(markdownResult);

                slashCommandCreateEvent.getSlashCommandInteraction().respondLater().thenAccept(interactionOriginalResponseUpdater -> {
                    logger.info("Prompting user with search selection");
                    interactionOriginalResponseUpdater.setContent("Por favor introduce el n\u00FAmero del manga que deseas \n" + result).update();

                    api.addMessageCreateListener(event -> {
                        if (!event.getMessageContent().matches("[0-9]+")) {
                            return;
                        }

                        if (slashCommandCreateEvent.getInteraction().getUser().getId() != event.getMessageAuthor().getId()) {
                            return;
                        }

                        String userInput = event.getMessage().getContent();
                        int index = Integer.parseInt(userInput) - 1;
                        if (index < 0) {
                            event.getMessage().reply("Introduce un n\u00FAmero mayor a 1");
                            return;
                        }

                        String selectedTitle = titlesArray[index];

                        try {
                            HttpResponse<String> searchSelectedTitle = Unirest.get("/manga/" +
                                            selectedTitle +
                                            "?&includes[]=cover_art" +
                                            "&includes[]=author" +
                                            "&includes[]=artist")
                                    .asString();
                            Object newParsedJson = Configuration.defaultConfiguration().jsonProvider().parse(searchSelectedTitle.getBody());

                            String mainTitle = JsonPath.read(newParsedJson, "$.data.attributes.title.en");
                            String description = JsonPath.read(newParsedJson, "$.data.attributes.description.en");
                            Object coverArtObject = JsonPath.read(newParsedJson, "$.data.relationships[?(@.type == 'cover_art')].attributes.fileName");
                            Object mangaAuthorObject = JsonPath.read(newParsedJson, "$.data.relationships[?(@.type == 'author')].attributes.name");
                            Object mangaArtistObject = JsonPath.read(newParsedJson, "$.data.relationships[?(@.type == 'artist')].attributes.name");

                            String coverArtUUID = coverArtObject.toString().replaceAll("[\\[\\]\"]", "");
                            String authorNames = mangaAuthorObject.toString().replaceAll("[\\[\\]\"]", "");
                            String artistNames = mangaArtistObject.toString().replaceAll("[\\[\\]\"]", "");

                            String coverArtURL = "https://uploads.mangadex.org/covers/" + selectedTitle + "/" + coverArtUUID + ".256.jpg";
                            URI uri = new URI(coverArtURL);
                            URL coverArtURItoURL = uri.toURL();

                            EmbedBuilder responseEmbed = new EmbedBuilder()
                                    .setTitle(mainTitle)
                                    .setDescription(description)
                                    .setAuthor("MangaDex", "https://mangadex.org/manga/" + selectedTitle, "https://cdn.discordapp.com/attachments/1000809614377500832/1169690488564088972/mangadex-logo.png")
                                    .addField("Autores:", authorNames)
                                    .addInlineField("Artistas:", artistNames)
                                    .setThumbnail(coverArtURItoURL.openStream());

                            event.getMessage().reply(responseEmbed).join();

                        } catch (IOException | URISyntaxException e) {
                            throw new RuntimeException(e);
                        }
                    }).removeAfter(20, TimeUnit.SECONDS);
                });
                return;
            }
                /*
                /  Executes if the "Lista" is false.
                */
            logger.info("Received SlashCommand search without list");
            slashCommandCreateEvent.getSlashCommandInteraction().respondLater().thenAccept(interactionOriginalResponseUpdater -> {
                String searchValue = slashCommandCreateEvent.getSlashCommandInteraction()
                        .getArgumentStringValueByName("Nombre")
                        .orElseThrow();

                Unirest.config().defaultBaseUrl("https://api.mangadex.org");
                HttpResponse<String> httpResponse = Unirest.get("/manga/" +
                                "?title=" +
                                searchValue +
                                "&order[relevance]=desc")
                        .asString();

                Object parsedJson = Configuration.defaultConfiguration().jsonProvider().parse(httpResponse.getBody());

                Object relevantTitleID = JsonPath.read(parsedJson, "$.data[0].id");

                try {
                    HttpResponse<String> searchSelectedTitle = Unirest.get("/manga/" +
                                    relevantTitleID +
                                    "?&includes[]=cover_art" +
                                    "&includes[]=author" +
                                    "&includes[]=artist")
                            .asString();
                    Object newParsedJson = Configuration.defaultConfiguration().jsonProvider().parse(searchSelectedTitle.getBody());

                    String mainTitle = JsonPath.read(newParsedJson, "$.data.attributes.title.en");
                    String description = JsonPath.read(newParsedJson, "$.data.attributes.description.en");
                    Object coverArtObject = JsonPath.read(newParsedJson, "$.data.relationships[?(@.type == 'cover_art')].attributes.fileName");
                    Object mangaAuthorObject = JsonPath.read(newParsedJson, "$.data.relationships[?(@.type == 'author')].attributes.name");
                    Object mangaArtistObject = JsonPath.read(newParsedJson, "$.data.relationships[?(@.type == 'artist')].attributes.name");

                    String coverArtUUID = coverArtObject.toString().replaceAll("[\\[\\]\"]", "");
                    String authorNames = mangaAuthorObject.toString().replaceAll("[\\[\\]\"]", "");
                    String artistNames = mangaArtistObject.toString().replaceAll("[\\[\\]\"]", "");

                    String coverArtURL = "https://uploads.mangadex.org/covers/" + relevantTitleID + "/" + coverArtUUID + ".256.jpg";
                    URI uri = new URI(coverArtURL);
                    URL coverArtURItoURL = uri.toURL();

                    EmbedBuilder responseEmbed = new EmbedBuilder()
                            .setTitle(mainTitle)
                            .setDescription(description)
                            .setAuthor("MangaDex", "https://mangadex.org/manga/" + relevantTitleID, "https://cdn.discordapp.com/attachments/1000809614377500832/1169690488564088972/mangadex-logo.png")
                            .addField("Autores:", authorNames)
                            .addInlineField("Artistas:", artistNames)
                            .setThumbnail(coverArtURItoURL.openStream());

                    interactionOriginalResponseUpdater.addEmbed(responseEmbed).update().join();

                } catch (IOException | URISyntaxException e) {
                    throw new RuntimeException(e);
                }
            });
        }
    }

    @Override
    public void onMessageCreate(MessageCreateEvent event) {

    }
}
