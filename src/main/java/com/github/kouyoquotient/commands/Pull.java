package com.github.kouyoquotient.commands;

import com.github.kouyoquotient.utils.JSONRead;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.listener.message.MessageCreateListener;
import org.json.JSONArray;
import org.json.JSONObject;
import redis.clients.jedis.JedisPooled;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Objects;

public class Pull implements MessageCreateListener {
    private static final Logger logger = LogManager.getRootLogger();
    public static final JedisPooled jedis = new JedisPooled("localhost", 6379);

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

                String value;
                int latestChapterIndex = 0;

                JSONRead jsonRead = gson.fromJson(niceJson, JSONRead.class);
                ArrayList<Object> arrayList = jsonRead.getData();
                JSONArray objects = new JSONArray(arrayList);

//                if (jedis.llen("chapterID") == 32) {
//                    event.getChannel().sendMessage("List contains 32 elements");
//                    int j = 0;
//                    while (j < jedis.llen("chapterID")) {
//                        if (Objects.equals(jedis.lindex("chapterID", j), jedis.get("lastChapter"))) {
//                            return;
//                        }
//                        jedis.lset("chapterID", j, "DELETED");
//                        jedis.lrem("chapterID", j, "DELETED");
//                        j++;
//                    }
//                    latestChapter = jedis.lindex("chapterID", j + 1);
//                    latestChapterIndex = j + 1;
//                    event.getChannel().sendMessage("Set latestChapter as" + latestChapter + " and latestChapterIndex as" + latestChapterIndex);
//                }

                if (jedis.llen("chapterID") == 0) {
                    event.getChannel().sendMessage("Writing...");
                    jedis.lpush("chapterID", "");
                    for (int i = 0; i < objects.length(); i++) {
                        JSONObject jsonObject = objects.getJSONObject(i);
                        value = jsonObject.optString("id");
                        jedis.lpush("chapterID", value);
                    }
                    jedis.lrem("chapterID", 0, "");
                    event.getChannel().sendMessage("Successfully written IDs to redis");
                }

                event.getChannel().sendMessage("Now looking for index for " + jedis.lindex("chapterID", 31));
                for (int i = 0; i < jedis.llen("chapterID"); i++) {
                    if (Objects.equals(jedis.lindex("chapterID", i), jedis.lindex("chapterID", 31))) {
                        event.getChannel().sendMessage("Found " + jedis.lindex("chapterID", 31) + " on index " + i);
                        return;
                    }
                    latestChapterIndex = i;
                }

                /*
                 * TODO:
                 *  For some reason jedis doesn't set the keys and values from below,
                 *  I don't see what I'm doing wrong, so I'll leave it for later
                 */
                jedis.set("lastChapter", jedis.lindex("chapterID", 31));
                jedis.set("lastChapterIndex", String.valueOf(latestChapterIndex));
                event.getChannel().sendMessage("Set lastChapter as "+ jedis.get("lastChapter"));
                event.getChannel().sendMessage("Set lastChapterIndex as "+ jedis.get("lastChapterIndex"));

            } catch (IOException e) {
                logger.error(e);
            }

            Unirest.shutDown();
        }
    }
}
