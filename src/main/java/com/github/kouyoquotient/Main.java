package com.github.kouyoquotient;

import com.github.kouyoquotient.commands.BuildRoleSelectorCommand;
import com.github.kouyoquotient.commands.PingCommand;
import com.github.kouyoquotient.commands.Pull;
import com.github.kouyoquotient.commands.supportchannel.*;
import com.github.kouyoquotient.listeners.RoleSelectorListener;
import com.github.kouyoquotient.listeners.UserJoinListener;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.interaction.SlashCommand;
import redis.clients.jedis.JedisPooled;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Main {
    public static final Logger logger = LogManager.getRootLogger();
    public static final JedisPooled jedis = new JedisPooled("localhost", 6379);

    public static void main(String[] args) {
        Path pathToJsonFile = Path.of("response.json");
        final String token = args[0];
        DiscordApi api = new DiscordApiBuilder()
                .setToken(token)
                .setAllIntents()
                .login()
                .join();

        // Register commands
        api.addMessageCreateListener(new HelpCommand());
        api.addMessageCreateListener(new FuncionesCommand());
        api.addMessageCreateListener(new FeedbackCommand());
        api.addMessageCreateListener(new DiagnosticoCommand());
        api.addMessageCreateListener(new Isrgx1Command());

        api.addMessageCreateListener(new Pull());
        api.addMessageCreateListener(new BuildRoleSelectorCommand());

        SlashCommand.with("ping", "Test command").createGlobal(api).join();
        api.addSlashCommandCreateListener(new PingCommand());

        // Register Listeners
        api.addSelectMenuChooseListener(new RoleSelectorListener());
        api.addServerMemberJoinListener(new UserJoinListener());
        /*
         * WARNING:
         * I strongly recommend not enabling the announcements' listener,
         * it's functionally WILL BE awful since it lacks proper testing.
         */
//        api.addMessageCreateListener(new AnnouncementsListener());

        if (Files.exists(pathToJsonFile)) {
            try {
                logger.info("API response file exits, deleting...");
                Files.delete(pathToJsonFile);
                logger.info("File deleted.");
            } catch (IOException e) {
                logger.error(e);
            }
        }

        logger.info("Bot is now running!");
    }
}
