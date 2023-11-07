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

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
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

                LinkedHashMap<String, Object> data = JsonPath.read(titleJson, "$.data");

                List<LinkedHashMap<String, Object>> tags = (List<LinkedHashMap<String, Object>>) ((LinkedHashMap<String, Object>) data.get("attributes")).get("tags");

                List<String> themeTags = new ArrayList<>();
                List<String> genreTags = new ArrayList<>();
                List<String> contentWarningTags = new ArrayList<>();

                for (LinkedHashMap<String, Object> tag : tags) {
                    LinkedHashMap<String, Object> attributes = (LinkedHashMap<String, Object>) tag.get("attributes");
                    LinkedHashMap<String, Object> nameObject = (LinkedHashMap<String, Object>) attributes.get("name");
                    String name = (String) nameObject.get("en");

                    if ("theme".equals(attributes.get("group"))) {
                        themeTags.add(name);
                    } else if ("genre".equals(attributes.get("group"))) {
                        genreTags.add(name);
                    } else if ("content".equals(attributes.get("group"))) {
                        contentWarningTags.add(name);
                    }
                }

                Set<String> themeTagsSet = new HashSet<>(themeTags);
                Set<String> genreTagsSet = new HashSet<>(genreTags);
                Set<String> contentWarningTagsSet = new HashSet<>(contentWarningTags);

                themeTags = new ArrayList<>(themeTagsSet);
                genreTags = new ArrayList<>(genreTagsSet);
                contentWarningTags = new ArrayList<>(contentWarningTagsSet);

                String publicationDemographic = JsonPath.read(titleJson, "$.data.attributes.publicationDemographic");
                Double ratingAverage = JsonPath.read(titleStatisticsJson, "$.statistics." + uuid + ".rating.average");

                String pubStatus = JsonPath.read(titleJson, "$.data.attributes.status");
                Object coverArtObject = JsonPath.read(titleJson, "$.data.relationships[?(@.type == 'cover_art')].attributes.fileName");
                Integer yearPublication = JsonPath.read(titleJson, "$.data.attributes.year");
                int followCount = JsonPath.read(titleStatisticsJson, "$.statistics." + uuid + ".follows");

                String mangaFollowCount = String.format("%,d", followCount);

                String titleDescription = getDescription.replaceAll("[\\[\\]\"]", "");
                String mangaThemeTags = themeTags.toString().replaceAll("[\\[\\]\"]", "");
                String mangaGenreTags = genreTags.toString().replaceAll("[\\[\\]\"]", "");
                String mangaContentWarning = contentWarningTags.toString().replaceAll("[\\[\\]\"]", "");
                String mangaContentRating = contentRating.substring(0, 1).toUpperCase() + contentRating.substring(1);
                String mangaTitle = getTitle.replaceAll("[\\[\\]\"]", "");
                String mangaCoverArtUUID = coverArtObject.toString().replaceAll("[\\[\\]\"]", "");
                String mangaPubStatus = pubStatus.substring(0, 1).toUpperCase() + pubStatus.substring(1);

                String mangaYearPublication = yearPublication != null ? yearPublication.toString() : "__*Esta obra no tiene a\u00F1o de publicaci\u00F3n*__";
                String mangaPubDemographic = publicationDemographic != null ? publicationDemographic.substring(0, 1).toUpperCase() + publicationDemographic.substring(1) : "__*Esta obra no tiene demograf\u00EDa*__";
                String mangaRatingAverage = ratingAverage != null ? ratingAverage.toString().substring(0, 3) : "__*Esta obra a\u00FAn no ha sido calificada*__";

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
                        .addInlineField("<:tags:1171482235732504668> G\u00E9neros:", mangaThemeTags)
                        .addInlineField("<:folderopen:1171482185371500684> Temas:", mangaGenreTags)
                        .addInlineField("<:18:1171482170615926845> Advertencias de contenido:", mangaContentWarning)
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
}
