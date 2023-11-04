package com.github.kouyoquotient.commands.mdcommands;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
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
import java.util.*;
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

                List<String> mainTitles = JsonPath.read(parsedJson, "$.data[*].attributes.title.en");
                Object titlesID = JsonPath.read(parsedJson, "$.data[*].id");

                JSONArray jsonArray = (JSONArray) titlesID;

                int arraySize = jsonArray.size();
                String[] titlesArray = new String[arraySize];

                for (int i = 0; i < arraySize; i++) {
                    titlesArray[i] = jsonArray.get(i).toString();
                }

                StringBuilder formattedTitles = new StringBuilder();
                for (int i = 0; i < mainTitles.size(); i++) {
                    formattedTitles.append((i + 1)).append(". ").append(mainTitles.get(i)).append("\n");
                }
                String resultFormat = formattedTitles.toString();
                String result = StringEscapeUtils.unescapeJava(resultFormat);

                slashCommandCreateEvent.getSlashCommandInteraction().respondLater().thenCompose(interactionOriginalResponseUpdater -> {
                    logger.info("Prompting user with search selection");
                    interactionOriginalResponseUpdater.setContent("Escribe el n\u00FAmero del manga que deseas \n" + result).update();

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
                            DocumentContext context = JsonPath.parse(parsedJson);

                            Object getTitle = context.read("$.data[0].attributes.title.en");
                            if (getTitle == null) {
                                getTitle = context.read("$.data[0].attributes.title.*[0]");
                            }

                            Object getDescription = "";
                            try {
                                List<Map<String, Object>> dataList = JsonPath.read(parsedJson, "$.data[*].attributes.description");
                                for (Map<String, Object> descriptions : dataList) {
                                    if (descriptions.containsKey("en")) {
                                        getDescription = descriptions.get("en");
                                        break;
                                    } else if (!descriptions.isEmpty()) {
                                        getDescription = descriptions.values().iterator().next();
                                        break;
                                    }
                                }
                            } catch (PathNotFoundException e) {
                                logger.error(e);
                            }

                            List<LinkedHashMap<String, Object>> dataList = JsonPath.read(parsedJson, "$.data[*]");

                            List<String> themeTags = new ArrayList<>();
                            List<String> genreTags = new ArrayList<>();
                            List<String> contentWarningTags = new ArrayList<>();

                            for (LinkedHashMap<String, Object> data : dataList) {
                                List<LinkedHashMap<String, Object>> tags = (List<LinkedHashMap<String, Object>>) ((LinkedHashMap<String, Object>) data.get("attributes")).get("tags");
                                for (LinkedHashMap<String, Object> tag : tags) {
                                    LinkedHashMap<String, Object> attributes = (LinkedHashMap<String, Object>) tag.get("attributes");
                                    LinkedHashMap<String, String> name = (LinkedHashMap<String, String>) attributes.get("name");

                                    if ("theme".equals(attributes.get("group"))) {
                                        themeTags.add(name.get("en"));
                                    } else if ("genre".equals(attributes.get("group"))) {
                                        genreTags.add(name.get("en"));
                                    } else if ("content".equals(attributes.get("group"))) {
                                        contentWarningTags.add(name.get("en"));
                                    }
                                }
                            }


                            Set<String> themeTagsSet = new HashSet<>(themeTags);
                            Set<String> genreTagsSet = new HashSet<>(genreTags);
                            Set<String> contentWarningTagsSet = new HashSet<>(contentWarningTags);

                            themeTags = new ArrayList<>(themeTagsSet);
                            genreTags = new ArrayList<>(genreTagsSet);
                            contentWarningTags = new ArrayList<>(contentWarningTagsSet);


                            Object coverArtObject = JsonPath.read(newParsedJson, "$.data.relationships[?(@.type == 'cover_art')].attributes.fileName");
                            Object mangaAuthorObject = JsonPath.read(newParsedJson, "$.data.relationships[?(@.type == 'author')].attributes.name");
                            Object mangaArtistObject = JsonPath.read(newParsedJson, "$.data.relationships[?(@.type == 'artist')].attributes.name");
                            String pubStatus = JsonPath.read(newParsedJson, "$.data.attributes.status");
                            String contentRating = JsonPath.read(newParsedJson, "$.data.attributes.contentRating");
                            String publicationDemographic = JsonPath.read(newParsedJson, "$.data.attributes.publicationDemographic");

                            if (publicationDemographic == null) {
                                publicationDemographic = "Esta obra no tiene demografía";
                            }

                            String mangaCoverArtUUID = coverArtObject.toString().replaceAll("[\\[\\]\"]", "");
                            String mangaAuthorNames = mangaAuthorObject.toString().replaceAll("[\\[\\]\"]", "");
                            String mangaArtistNames = mangaArtistObject.toString().replaceAll("[\\[\\]\"]", "");
                            String mangaTitle = getTitle.toString().replaceAll("[\\[\\]\"]", "");
                            String titleDescription = getDescription.toString().replaceAll("[\\[\\]\"]", "");
                            String mangaThemeTags = themeTags.toString().replaceAll("[\\[\\]\"]", "");
                            String mangaGenreTags = genreTags.toString().replaceAll("[\\[\\]\"]", "");
                            String mangaContentWarning = contentWarningTags.toString().replaceAll("[\\[\\]\"]", "");
                            String mangaPubStatus = pubStatus.substring(0, 1).toUpperCase() + pubStatus.substring(1);
                            String mangaContentRating = contentRating.substring(0, 1).toUpperCase() + contentRating.substring(1);
                            String mangaPubDemographic = publicationDemographic.substring(0, 1).toUpperCase() + publicationDemographic.substring(1);

                            String coverArtURL = "https://uploads.mangadex.org/covers/" + selectedTitle + "/" + mangaCoverArtUUID + ".256.jpg";
                            URI uri = new URI(coverArtURL);
                            URL coverArtURItoURL = uri.toURL();

                            EmbedBuilder responseEmbed = new EmbedBuilder()
                                    .setTitle(mangaTitle)
                                    .setUrl("https://mangadex.org/title/" + selectedTitle)
                                    .setDescription(titleDescription)
                                    .setAuthor("MangaDex", "", "https://cdn.discordapp.com/attachments/1000809614377500832/1169690488564088972/mangadex-logo.png")
                                    .addInlineField("Autor(es):", mangaAuthorNames)
                                    .addInlineField("Artista(s):", mangaArtistNames)
                                    .addInlineField("Estado de publicaci\u00F3n:", mangaPubStatus)
                                    .addInlineField("Clasificaci\u00F3n de contenido:", mangaContentRating)
                                    .addInlineField("Advertencias de contenido:", mangaContentWarning)
                                    .addInlineField("Demograf\u00EDa", mangaPubDemographic)
                                    .addInlineField("G\u00E9neros:", mangaThemeTags)
                                    .addInlineField("Temas:", mangaGenreTags)
//                                    .addInlineField("Leelo o compralo:", "")
//                                    .addInlineField("Rastreo:", ""))
                                    .setThumbnail(coverArtURItoURL.openStream());

                            event.getMessage().reply(responseEmbed).join();

                        } catch (IOException | URISyntaxException e) {
                            throw new RuntimeException(e);
                        }
                    }).removeAfter(10, TimeUnit.SECONDS);
                    return null;
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
                    DocumentContext context = JsonPath.parse(parsedJson);

                    Object getTitle = context.read("$.data[0].attributes.title.en");
                    if (getTitle == null) {
                        getTitle = context.read("$.data[0].attributes.title.*[0]");
                    }

                    Object getDescription = "";
                    try {
                        List<Map<String, Object>> dataList = JsonPath.read(parsedJson, "$.data[*].attributes.description");
                        for (Map<String, Object> descriptions : dataList) {
                            if (descriptions.containsKey("en")) {
                                getDescription = descriptions.get("en");
                                break;
                            } else if (!descriptions.isEmpty()) {
                                getDescription = descriptions.values().iterator().next();
                                break;
                            }
                        }
                    } catch (PathNotFoundException e) {
                        logger.error(e);
                    }

                    List<LinkedHashMap<String, Object>> dataList = JsonPath.read(parsedJson, "$.data[*]");

                    List<String> themeTags = new ArrayList<>();
                    List<String> genreTags = new ArrayList<>();
                    List<String> contentWarningTags = new ArrayList<>();

                    for (LinkedHashMap<String, Object> data : dataList) {
                        List<LinkedHashMap<String, Object>> tags = (List<LinkedHashMap<String, Object>>) ((LinkedHashMap<String, Object>) data.get("attributes")).get("tags");
                        for (LinkedHashMap<String, Object> tag : tags) {
                            LinkedHashMap<String, Object> attributes = (LinkedHashMap<String, Object>) tag.get("attributes");
                            LinkedHashMap<String, String> name = (LinkedHashMap<String, String>) attributes.get("name");

                            if ("theme".equals(attributes.get("group"))) {
                                themeTags.add(name.get("en"));
                            } else if ("genre".equals(attributes.get("group"))) {
                                genreTags.add(name.get("en"));
                            } else if ("content".equals(attributes.get("group"))) {
                                contentWarningTags.add(name.get("en"));
                            }
                        }
                    }

                    Set<String> themeTagsSet = new HashSet<>(themeTags);
                    Set<String> genreTagsSet = new HashSet<>(genreTags);
                    Set<String> contentWarningTagsSet = new HashSet<>(contentWarningTags);

                    themeTags = new ArrayList<>(themeTagsSet);
                    genreTags = new ArrayList<>(genreTagsSet);
                    contentWarningTags = new ArrayList<>(contentWarningTagsSet);


                    Object coverArtObject = JsonPath.read(newParsedJson, "$.data.relationships[?(@.type == 'cover_art')].attributes.fileName");
                    Object mangaAuthorObject = JsonPath.read(newParsedJson, "$.data.relationships[?(@.type == 'author')].attributes.name");
                    Object mangaArtistObject = JsonPath.read(newParsedJson, "$.data.relationships[?(@.type == 'artist')].attributes.name");
                    String pubStatus = JsonPath.read(newParsedJson, "$.data.attributes.status");
                    String contentRating = JsonPath.read(newParsedJson, "$.data.attributes.contentRating");
                    String publicationDemographic = JsonPath.read(newParsedJson, "$.data.attributes.publicationDemographic");

                    if (publicationDemographic == null) {
                        publicationDemographic = "Esta obra no tiene demograf\u00EDa";
                    }


                    String mangaCoverArtUUID = coverArtObject.toString().replaceAll("[\\[\\]\"]", "");
                    String mangaAuthorNames = mangaAuthorObject.toString().replaceAll("[\\[\\]\"]", "");
                    String mangaArtistNames = mangaArtistObject.toString().replaceAll("[\\[\\]\"]", "");
                    String mangaTitle = getTitle.toString().replaceAll("[\\[\\]\"]", "");
                    String titleDescription = getDescription.toString().replaceAll("[\\[\\]\"]", "");
                    String mangaThemeTags = themeTags.toString().replaceAll("[\\[\\]\"]", "");
                    String mangaGenreTags = genreTags.toString().replaceAll("[\\[\\]\"]", "");
                    String mangaContentWarning = contentWarningTags.toString().replaceAll("[\\[\\]\"]", "");
                    String mangaPubStatus = pubStatus.substring(0, 1).toUpperCase() + pubStatus.substring(1);
                    String mangaContentRating = contentRating.substring(0, 1).toUpperCase() + contentRating.substring(1);
                    String mangaPubDemographic = publicationDemographic.substring(0, 1).toUpperCase() + publicationDemographic.substring(1);

                    String coverArtURL = "https://uploads.mangadex.org/covers/" + relevantTitleID + "/" + mangaCoverArtUUID + ".256.jpg";
                    URI uri = new URI(coverArtURL);
                    URL coverArtURItoURL = uri.toURL();

                    EmbedBuilder responseEmbed = new EmbedBuilder()
                            .setTitle(mangaTitle)
                            .setUrl("https://mangadex.org/title/" + relevantTitleID)
                            .setDescription(titleDescription)
                            .setAuthor("MangaDex", "", "https://cdn.discordapp.com/attachments/1000809614377500832/1169690488564088972/mangadex-logo.png")
                            .addInlineField("Autor(es):", mangaAuthorNames)
                            .addInlineField("Artista(s):", mangaArtistNames)
                            .addInlineField("Estado de publicaci\u00F3n:", mangaPubStatus)
                            .addInlineField("Clasificaci\u00F3n de contenido:", mangaContentRating)
                            .addInlineField("Advertencias de contenido:", mangaContentWarning)
                            .addInlineField("Demograf\u00EDa", mangaPubDemographic)
                            .addInlineField("G\u00E9neros:", mangaThemeTags)
                            .addInlineField("Temas:", mangaGenreTags)
//                                    .addInlineField("Leelo o compralo:", "")
//                                    .addInlineField("Rastreo:", ""))
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
