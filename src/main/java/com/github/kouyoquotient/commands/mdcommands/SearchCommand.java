package com.github.kouyoquotient.commands.mdcommands;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.event.interaction.SlashCommandCreateEvent;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.listener.interaction.SlashCommandCreateListener;
import org.javacord.api.listener.message.MessageCreateListener;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class SearchCommand implements SlashCommandCreateListener, MessageCreateListener {
    private static final Logger logger = LogManager.getRootLogger();

    @Override
    public void onSlashCommandCreate(SlashCommandCreateEvent slashCommandCreateEvent) {
        DiscordApi api = slashCommandCreateEvent.getApi();

        if (slashCommandCreateEvent.getSlashCommandInteraction().getFullCommandName().equalsIgnoreCase("search")) {
            /*
             /  Executes only if the "Lista" value on the slash command is either empty (true by default) or true (by user input).
             */

            if (slashCommandCreateEvent.getSlashCommandInteraction().getArgumentBooleanValueByName("Lista").isEmpty()
                    || slashCommandCreateEvent.getSlashCommandInteraction().getArgumentBooleanValueByName("Lista").orElseThrow()){
                logger.info("Received SlashCommand search");

                String titleToSearch = slashCommandCreateEvent.getSlashCommandInteraction()
                        .getArgumentStringValueByName("Nombre")
                        .orElseThrow();
                // To avoid issues with especial characters, we encode the value on titleToSearch into UTF-8.
                String encodedTitletoSearch = java.net.URLEncoder.encode(titleToSearch, StandardCharsets.UTF_8);

                Unirest.config().defaultBaseUrl("https://api.mangadex.org");
                HttpResponse<String> titleSearch = Unirest.get("/manga/" +
                                "?title=" +
                                encodedTitletoSearch +
                                "&order[relevance]=desc")
                        .asString();
                if (!titleSearch.isSuccess()) {
                    // If the response from the API isn't 200 we return an error message to the user.
                    slashCommandCreateEvent.getInteraction().createImmediateResponder()
                            .setContent(
                                    "<:selwarning:1061074946526289982> Ha ocurrido un error al realizar esta petici\u00F3n: (MangaDexAPI) - " + titleSearch.getStatusText()
                            ).respond();
                }

                // We store the results from titleSearch into titleSearchResult
                Object titleSearchResult = Configuration.defaultConfiguration().jsonProvider().parse(titleSearch.getBody());
                // Then we create a List for storing the titles and another for storing the IDs of each title
                List<String> listOfMangaTitlesFromSearch = JsonPath.read(titleSearchResult, "$.data[*].attributes.title.en");
                List<String> listOfIDsFromSearch = JsonPath.read(titleSearchResult, "$.data[*].id");

                /*
                /  Then we format the titles from listOfMangaTitlesFromSearch to form a list.
                /  On Discord will appear as:
                /  1. [title]
                /  2. [title]
                */
                StringBuilder formattedTitles = new StringBuilder();
                for (int i = 0; i < listOfMangaTitlesFromSearch.size(); i++) {
                    formattedTitles.append((i + 1)).append(". ").append(listOfMangaTitlesFromSearch.get(i)).append("\n");
                }
                String searchResultFormatted = formattedTitles.toString();

                /*
                /  Then we prompt the user with a selection.
                /  This current implementation doesn't allow removing the listener after the user inputs a number.
                /  This is probably due to a limitation of the library.
                /  As far as I know there's a workaround, I'll look it up later on.
                /  TODO: https://github.com/Javacord/Javacord/issues/363
                /  For now the listener removes itself after 15 seconds, but if the user does another search, the last
                /  listener will still be active, so the user will receive two replies.
                */
                slashCommandCreateEvent.getSlashCommandInteraction().respondLater().thenCompose(interactionOriginalResponseUpdater -> {
                    logger.info("Prompting user with search selection");

                    interactionOriginalResponseUpdater
                            .setContent(
                                    "<:arrowdown:1185274262412611714>  Escribe el n\u00FAmero del manga que deseas *(tienes 15 segundos)*. \n" + searchResultFormatted
                            ).update();

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

                        String selectedTitleID = listOfIDsFromSearch.get(index);

                        try {
                            HttpResponse<String> searchSelectedTitle = Unirest.get("/manga/" +
                                            selectedTitleID +
                                            "?&includes[]=cover_art" +
                                            "&includes[]=author" +
                                            "&includes[]=artist")
                                    .asString();
                            HttpResponse<String> titleStatistics = Unirest.get("/statistics/manga/" + selectedTitleID).asString();
                            Object titleStatisticsJson = Configuration.defaultConfiguration().jsonProvider().parse(titleStatistics.getBody());
                            Object selectedTitleJson = Configuration.defaultConfiguration().jsonProvider().parse(searchSelectedTitle.getBody());

                            Object getTitle = JsonPath.read(selectedTitleJson, ("$.data.attributes.title.en"));
                            if (getTitle == null) {
                                getTitle = JsonPath.read(selectedTitleJson, "$.data.attributes.title.*[0]");
                            }

                            String getDescription = "";
                            try {
                                Map<String, Object> descriptionMap = JsonPath.read(selectedTitleJson, "$.data.attributes.description");
                                if (descriptionMap.isEmpty()) {
                                    getDescription = "__*Esta obra no tiene ninguna descripci\u00F3n*__";
                                } else {
                                    if (descriptionMap.containsKey("es-la")) {
                                        getDescription = descriptionMap.get("es-la").toString();
                                    } else if (descriptionMap.containsKey("es")) {
                                        getDescription = descriptionMap.get("es").toString();
                                    } else if (descriptionMap.containsKey("en")){
                                        getDescription = descriptionMap.get("en").toString();
                                    }
                                }
                            } catch (PathNotFoundException e) {
                                logger.error(e);
                            }

                            List<Map<String, Object>> tags = JsonPath.read(selectedTitleJson, "$.data.attributes.tags[?(@.type == 'tag')].attributes");
                            List<String> genreTags = new ArrayList<>();
                            List<String> themeTags = new ArrayList<>();
                            List<String> formatTags = new ArrayList<>();
                            List<String> contentWarningTags = new ArrayList<>();

                            for (Map<String, Object> tag : tags) {
                                String name = (String) ((Map<String, Object>) tag.get("name")).get("en");
                                String group = (String) tag.get("group");

                                switch (group) {
                                    case "genre":
                                        genreTags.add(name);
                                        break;
                                    case "theme":
                                        themeTags.add(name);
                                        break;
                                    case "format":
                                        formatTags.add(name);
                                        break;
                                    case "content":
                                        contentWarningTags.add(name);
                                        break;
                                }
                            }


                            Object coverArtObject = JsonPath.read(selectedTitleJson, "$.data.relationships[?(@.type == 'cover_art')].attributes.fileName");
                            Object mangaAuthorObject = JsonPath.read(selectedTitleJson, "$.data.relationships[?(@.type == 'author')].attributes.name");
                            Object mangaArtistObject = JsonPath.read(selectedTitleJson, "$.data.relationships[?(@.type == 'artist')].attributes.name");
                            String pubStatus = JsonPath.read(selectedTitleJson, "$.data.attributes.status");
                            String contentRating = JsonPath.read(selectedTitleJson, "$.data.attributes.contentRating");
                            String publicationDemographic = JsonPath.read(selectedTitleJson, "$.data.attributes.publicationDemographic");

                            if (publicationDemographic == null) {
                                publicationDemographic = "__*Esta obra no tiene demograf\u00EDa.*__";
                            }

                            Integer yearPublication = JsonPath.read(selectedTitleJson, "$.data.attributes.year");
                            String mangaYearPublication = yearPublication != null ? yearPublication.toString() : "__*Esta obra no tiene a\u00F1o de publicaci\u00F3n*__";
                            String mangaCoverArtUUID = coverArtObject.toString().replaceAll("[\\[\\]\"]", "");
                            String mangaAuthorNames = mangaAuthorObject.toString().replaceAll("[\\[\\]\"]", "");
                            String mangaArtistNames = mangaArtistObject.toString().replaceAll("[\\[\\]\"]", "");
                            String mangaTitle = getTitle.toString().replaceAll("[\\[\\]\"]", "");
                            String titleDescription = getDescription;
                            String mangaThemeTags = !themeTags.isEmpty() ? themeTags.toString().replaceAll("[\\[\\]\"]", "") : "__*Esta obra no tiene etiquetas de temas*__";
                            String mangaGenreTags = !genreTags.isEmpty() ? genreTags.toString().replaceAll("[\\[\\]\"]", "") : "__*Esta obra no tiene etiquetas de genero*__";
                            String mangaFormatTags = !formatTags.isEmpty() ? formatTags.toString().replaceAll("[\\[\\]\"]", "") : "__*Esta obra no tiene etiquetas de formato*__";
                            String mangaContentWarning = !contentWarningTags.isEmpty() ? contentWarningTags.toString().replaceAll("[\\[\\]\"]", "") : "__*Esta obra no tiene advertencias de contenido*__";
                            String mangaPubStatus = pubStatus.substring(0, 1).toUpperCase() + pubStatus.substring(1);
                            String mangaContentRating = contentRating.substring(0, 1).toUpperCase() + contentRating.substring(1);
                            String mangaPubDemographic = publicationDemographic.substring(0, 1).toUpperCase() + publicationDemographic.substring(1);
                            int followCount = JsonPath.read(titleStatisticsJson, "$.statistics." + selectedTitleID + ".follows");

                            Object ratingAverageObject = JsonPath.read(titleStatisticsJson, "$.statistics." + selectedTitleID + ".rating.average");
                            String mangaRatingAverage = getRating(ratingAverageObject);

                            String mangaFollowCount = String.format("%,d", followCount);

                            String coverArtURL = "https://uploads.mangadex.org/covers/" + selectedTitleID + "/" + mangaCoverArtUUID + ".256.jpg";
                            URI uri = new URI(coverArtURL);
                            URL coverArtURItoURL = uri.toURL();

                            EmbedBuilder responseEmbed = new EmbedBuilder()
                                    .setTitle(mangaTitle)
                                    .setUrl("https://mangadex.org/title/" + selectedTitleID)
                                    .setDescription(titleDescription)
                                    .setAuthor("MangaDex", "", "https://cdn.discordapp.com/attachments/1000809614377500832/1169690488564088972/mangadex-logo.png")
                                    .addInlineField("<:iconautor:1185278365981216830>  Autor(es):", mangaAuthorNames)
                                    .addInlineField("<:iconartist:1185278332019953746>  Artista(s):", mangaArtistNames)
                                    .addInlineField("<:bookbookmark:1171482171777757204> Estado de publicaci\u00F3n:", mangaPubStatus)
                                    .addInlineField("<:shieldexclamation:1171506222579589272> Clasificaci\u00F3n de contenido:", mangaContentRating)
                                    .addInlineField("<:18:1171482170615926845> Advertencias de contenido:", mangaContentWarning)
                                    .addInlineField("<:usersalt:1171482198893940888> Demograf\u00EDa:", mangaPubDemographic)
                                    .addField("<:format:1192283519486001272> Formatos:", mangaFormatTags)
                                    .addField("<:tags:1171482235732504668> G\u00E9neros:", mangaGenreTags)
                                    .addField("<:folderopen:1171482185371500684> Temas:", mangaThemeTags)
                                    .addInlineField("<:star:1171482195932745769> Calificaci\u00F3n:", mangaRatingAverage)
                                    .addInlineField("<:users:1171482238387507312> Seguidores:", mangaFollowCount)
                                    .addInlineField("<:calendar:1171507628980064306> A\u00F1o:", mangaYearPublication)
//                                    .addInlineField("Leelo o compralo:", "")
//                                    .addInlineField("Rastreo:", ""))
                                    .setColor(new Color(253, 102, 63))
                                    .setThumbnail(coverArtURItoURL.openStream());

                            event.getMessage().reply(responseEmbed).join();

                        } catch (IOException | URISyntaxException e) {
                            throw new RuntimeException(e);
                        }
                    }).removeAfter(15, TimeUnit.SECONDS);
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

                String encodedSearchValue = java.net.URLEncoder.encode(searchValue, StandardCharsets.UTF_8);

                Unirest.config().defaultBaseUrl("https://api.mangadex.org");
                HttpResponse<String> httpResponse = Unirest.get("/manga/" +
                                "?title=" +
                                encodedSearchValue +
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
                    HttpResponse<String> titleStatistics = Unirest.get("/statistics/manga/" + relevantTitleID).asString();
                    Object titleStatisticsJson = Configuration.defaultConfiguration().jsonProvider().parse(titleStatistics.getBody());
                    Object newParsedJson = Configuration.defaultConfiguration().jsonProvider().parse(searchSelectedTitle.getBody());

                    DocumentContext context = JsonPath.parse(parsedJson);

                    Object getTitle = context.read("$.data[0].attributes.title.en");
                    if (getTitle == null) {
                        getTitle = context.read("$.data[0].attributes.title.*[0]");
                    }

                    Object getDescription = "__*Esta obra no tiene descripci\u00F3n*__";
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

                    if (themeTags.isEmpty()) {
                        themeTags = Collections.singletonList("__*Esta obra no etiquetas para temas.*__");
                    }

                    if (genreTags.isEmpty()) {
                        genreTags = Collections.singletonList("__*Esta obra no tiene etiquetas para generos.*__");
                    }

                    if (contentWarningTags.isEmpty()) {
                        contentWarningTags = Collections.singletonList("__*Esta obra no tiene advertencias de contenido*__");
                    }

                    Object coverArtObject = JsonPath.read(newParsedJson, "$.data.relationships[?(@.type == 'cover_art')].attributes.fileName");
                    Object mangaAuthorObject = JsonPath.read(newParsedJson, "$.data.relationships[?(@.type == 'author')].attributes.name");
                    Object mangaArtistObject = JsonPath.read(newParsedJson, "$.data.relationships[?(@.type == 'artist')].attributes.name");
                    String pubStatus = JsonPath.read(newParsedJson, "$.data.attributes.status");
                    String contentRating = JsonPath.read(newParsedJson, "$.data.attributes.contentRating");
                    String publicationDemographic = JsonPath.read(newParsedJson, "$.data.attributes.publicationDemographic");

                    if (publicationDemographic == null) {
                        publicationDemographic = "Esta obra no tiene demograf\u00EDa";
                    }

                    Integer yearPublication = JsonPath.read(newParsedJson, "$.data.attributes.year");
                    String mangaYearPublication = yearPublication != null ? yearPublication.toString() : "__*Esta obra no tiene a\u00F1o de publicaci\u00F3n*__";
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
                    int followCount = JsonPath.read(titleStatisticsJson, "$.statistics." + relevantTitleID + ".follows");

                    Object ratingAverageObject = JsonPath.read(titleStatisticsJson, "$.statistics." + relevantTitleID + ".rating.average");
                    String mangaRatingAverage = getRating(ratingAverageObject);

                    String mangaFollowCount = String.format("%,d", followCount);

                    String coverArtURL = "https://uploads.mangadex.org/covers/" + relevantTitleID + "/" + mangaCoverArtUUID + ".256.jpg";
                    URI uri = new URI(coverArtURL);
                    URL coverArtURItoURL = uri.toURL();

                    EmbedBuilder responseEmbed = new EmbedBuilder()
                            .setTitle(mangaTitle)
                            .setUrl("https://mangadex.org/title/" + relevantTitleID)
                            .setDescription(titleDescription)
                            .setAuthor("MangaDex", "", "https://cdn.discordapp.com/attachments/1000809614377500832/1169690488564088972/mangadex-logo.png")
                            .addInlineField("<:iconautor:1185278365981216830>  Autor(es):", mangaAuthorNames)
                            .addInlineField("<:iconartist:1185278332019953746>  Artista(s):", mangaArtistNames)
                            .addInlineField("<:bookbookmark:1171482171777757204> Estado de publicaci\u00F3n:", mangaPubStatus)
                            .addInlineField("<:shieldexclamation:1171506222579589272> Clasificaci\u00F3n de contenido:", mangaContentRating)
                            .addInlineField("<:18:1171482170615926845> Advertencias de contenido:", mangaContentWarning)
                            .addInlineField("<:usersalt:1171482198893940888> Demograf\u00EDa:", mangaPubDemographic)
                            .addField("<:tags:1171482235732504668> G\u00E9neros:", mangaGenreTags)
                            .addField("<:folderopen:1171482185371500684> Temas:", mangaThemeTags)
                            .addInlineField("<:star:1171482195932745769> Calificaci\u00F3n:", mangaRatingAverage)
                            .addInlineField("<:users:1171482238387507312> Seguidores:", mangaFollowCount)
                            .addInlineField("<:calendar:1171507628980064306> A\u00F1o:", mangaYearPublication)
//                                    .addInlineField("Leelo o compralo:", "")
//                                    .addInlineField("Rastreo:", ""))
                            .setColor(new Color(253, 102, 63))
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

    private static String getRating(Object ratingAverageObject) {
        String mangaRatingAverage;
        if (ratingAverageObject != null) {
            if (ratingAverageObject instanceof Integer) {
                mangaRatingAverage = String.valueOf(ratingAverageObject);
            } else if (ratingAverageObject instanceof Double) {
                mangaRatingAverage = String.format("%.1f", ratingAverageObject);
            } else {
                mangaRatingAverage = "__*Esta obra a\u00FAn no ha sido calificada*__";
            }
        } else {
            mangaRatingAverage = "__*Esta obra a\u00FAn no ha sido calificada*__";
        }
        return mangaRatingAverage;
    }
}
