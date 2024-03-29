package com.github.kouyoquotient.listeners;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.listener.message.MessageCreateListener;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TitleLinkListener implements MessageCreateListener {
    private static final Logger logger = LogManager.getRootLogger();

    @Override
    public void onMessageCreate(MessageCreateEvent event) {

        /* Link format starts with "https://mangadex.org/title/" followed by a UUID and optional extra text (slug).
         * The URL should be surrounded by angle brackets.
         *
         * <https://mangadex.org/title/{UUID}/{Slug}>
         *
         * Examples that will match the pattern:
         * <https://mangadex.org/title/efb4278c-a761-406b-9d69-19603c5e4c8b/the-100-girlfriends-who-really-really-really-really-really-love-you>
         * or
         * <https://mangadex.org/title/efb4278c-a761-406b-9d69-19603c5e4c8b/>
         *
         * If this type of link is found, the code stops.
         */

        Pattern noEmbed = Pattern.compile("<(https://mangadex\\.org/title/([0-9a-fA-F\\\\-]+)(.+))?>");
        Matcher matchNoEmbedUUID = noEmbed.matcher(event.getMessageContent());

        if (matchNoEmbedUUID.find()) {
            return;
        }

        /* Link format starts with "https://mangadex.org/title/" followed by a UUID and optional extra text (slug).
         * https://mangadex.org/title/{UUID}/{Slug}
         *
         * Examples that will match the pattern:
         * https://mangadex.org/title/efb4278c-a761-406b-9d69-19603c5e4c8b
         * or
         * https://mangadex.org/title/efb4278c-a761-406b-9d69-19603c5e4c8b/the-100-girlfriends-who-really-really-really-really-really-love-you
         */

        Pattern linkUUID = Pattern.compile("https://mangadex\\.org/title/([0-9a-fA-F\\-]+)");
        Matcher matchUUID = linkUUID.matcher(event.getMessageContent());

        if (matchUUID.find()) {
            try {

                String uuid = matchUUID.group(1);

                Unirest.config().defaultBaseUrl("https://api.mangadex.org");
                HttpResponse<String> titleLookup = Unirest.get("/manga/" +
                                uuid +
                                "?&includes[]=cover_art")
                        .asString();
                HttpResponse<String> titleStatistics = Unirest.get("/statistics/manga/" + uuid).asString();

                if(!titleLookup.isSuccess()){
                    event.getMessage().getChannel().sendMessage("<:selwarning:1061074946526289982> Ha ocurrido un error al realizar esta petici\u00F3n: (MangaDexAPI) - "+titleLookup.getStatusText());
                }

                Object titleJson = Configuration.defaultConfiguration().jsonProvider().parse(titleLookup.getBody());
                Object titleStatisticsJson = Configuration.defaultConfiguration().jsonProvider().parse(titleStatistics.getBody());
                DocumentContext context = JsonPath.parse(titleJson);

                String getDescription = "__*No se encontr\u00F3 descripci\u00F3n en espa\u00F1ol para esta obra*__";
                try {
                    Map<String, Object> descriptionMap = JsonPath.read(titleJson, "$.data.attributes.description");
                    if (descriptionMap.isEmpty()) {
                        getDescription = "__*Esta obra no tiene ninguna descripci\u00F3n*__";
                    } else {
                        if (descriptionMap.containsKey("es-la")) {
                            getDescription = descriptionMap.get("es-la").toString();
                        } else if (descriptionMap.containsKey("es")) {
                            getDescription = descriptionMap.get("es").toString();
                        }
                    }
                } catch (PathNotFoundException e) {
                    logger.error(e);
                }

                String contentRating = JsonPath.read(titleJson, "$.data.attributes.contentRating");
                if (contentRating.equals("pornographic")) {
                    event.getMessage().reply("As\u00ED te quer\u00EDa agarrar, puerco. :pig2:");
                    return;
                }

                String getTitle = context.read("$.data.attributes.title.en");
                if (getTitle == null) {
                    getTitle = context.read("$.data.attributes.title.*[0]");
                }

                List<Map<String, Object>> tags = JsonPath.read(titleJson, "$.data.attributes.tags[?(@.type == 'tag')].attributes");
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

                String publicationDemographic = JsonPath.read(titleJson, "$.data.attributes.publicationDemographic");

                String pubStatus = JsonPath.read(titleJson, "$.data.attributes.status");
                Object coverArtObject = JsonPath.read(titleJson, "$.data.relationships[?(@.type == 'cover_art')].attributes.fileName");
                Integer yearPublication = JsonPath.read(titleJson, "$.data.attributes.year");
                int followCount = JsonPath.read(titleStatisticsJson, "$.statistics." + uuid + ".follows");

                String mangaFollowCount = String.format("%,d", followCount);

                String titleDescription = getDescription.replaceAll("[\\[\\]\"]", "");
                String mangaThemeTags = !themeTags.isEmpty() ? themeTags.toString().replaceAll("[\\[\\]\"]", "") : "__*Esta obra no tiene etiquetas de temas*__";
                String mangaGenreTags = !genreTags.isEmpty() ? genreTags.toString().replaceAll("[\\[\\]\"]", "") : "__*Esta obra no tiene etiquetas de genero*__";
                String mangaFormatTags = !formatTags.isEmpty() ? formatTags.toString().replaceAll("[\\[\\]\"]", "") : "__*Esta obra no tiene etiquetas de formato*__";
                String mangaContentWarning = !contentWarningTags.isEmpty() ? contentWarningTags.toString().replaceAll("[\\[\\]\"]", "") : "__*Esta obra no tiene advertencias de contenido*__";
                String mangaContentRating = contentRating.substring(0, 1).toUpperCase() + contentRating.substring(1);
                String mangaTitle = getTitle.replaceAll("[\\[\\]\"]", "");
                String mangaCoverArtUUID = coverArtObject.toString().replaceAll("[\\[\\]\"]", "");
                String mangaPubStatus = pubStatus.substring(0, 1).toUpperCase() + pubStatus.substring(1);

                String mangaYearPublication = yearPublication != null ? yearPublication.toString() : "__*Esta obra no tiene a\u00F1o de publicaci\u00F3n*__";
                String mangaPubDemographic = publicationDemographic != null ? publicationDemographic.substring(0, 1).toUpperCase() + publicationDemographic.substring(1) : "__*Esta obra no tiene demograf\u00EDa*__";
                Object ratingAverageObject = JsonPath.read(titleStatisticsJson, "$.statistics." + uuid + ".rating.average");
                String mangaRatingAverage = getRating(ratingAverageObject);


                String coverArtURL = "https://uploads.mangadex.org/covers/" + uuid + "/" + mangaCoverArtUUID + ".256.jpg";
                URI uri = new URI(coverArtURL);
                URL coverArtURItoURL = uri.toURL();

                EmbedBuilder titleEmbed = new EmbedBuilder()
                        .setTitle("Descripci\u00F3n en espa\u00F1ol:")
                        .setDescription(titleDescription)
                        .addInlineField("<:star:1171482195932745769> Calificaci\u00F3n:", mangaRatingAverage)
                        .addInlineField("<:users:1171482238387507312> Seguidores:", mangaFollowCount)
                        .addInlineField("<:calendar:1171507628980064306> A\u00F1o:", mangaYearPublication)
                        .addInlineField("<:bookbookmark:1171482171777757204> Estado de publicaci\u00F3n:", mangaPubStatus)
                        .addInlineField("<:usersalt:1171482198893940888> Demograf\u00EDa:", mangaPubDemographic)
                        .addInlineField("<:shieldexclamation:1171506222579589272> Clasificaci\u00F3n de contenido:", mangaContentRating)
                        .addInlineField("<:18:1171482170615926845> Advertencias de contenido:", mangaContentWarning)
                        .addField("<:format:1192283519486001272> Formatos:", mangaFormatTags)
                        .addField("<:tags:1171482235732504668> G\u00E9neros:", mangaGenreTags)
                        .addField("<:folderopen:1171482185371500684> Temas:", mangaThemeTags)
                        .setColor(new Color( 253,102,63))
                        .setThumbnail(coverArtURItoURL.openStream());

                new MessageBuilder()
                        .append("Aqu\u00ED tienes informaci\u00F3n adicional para: ***" + mangaTitle + "***")
                        .addEmbed(titleEmbed)
                        .send(event.getChannel());
            } catch (URISyntaxException | IOException e) {
                throw new RuntimeException(e);
            }
        }
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
