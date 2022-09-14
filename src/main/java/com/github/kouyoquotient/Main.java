package com.github.kouyoquotient;

import com.github.kouyoquotient.commands.BuildRoleSelectorCommand;
import com.github.kouyoquotient.commands.Poke;
import com.github.kouyoquotient.commands.supportchannel.*;
import com.github.kouyoquotient.listeners.AnnouncementsListener;
import com.github.kouyoquotient.listeners.RoleSelectorListener;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Main {
    public static final Logger logger = LogManager.getRootLogger();

    public static void main(String[] args) {
        Path pathToJsonFile = Path.of("response.json");
        final String token = args[0];

        DiscordApi api = new DiscordApiBuilder()
                .setToken(token)
                .login()
                .join();

        // Register commands
        api.addMessageCreateListener(new HelpCommand());
        api.addMessageCreateListener(new FuncionesCommand());
        api.addMessageCreateListener(new FeedbackCommand());
        api.addMessageCreateListener(new DiagnosticoCommand());
        api.addMessageCreateListener(new Isrgx1Command());

        api.addMessageCreateListener(new Poke());
        api.addMessageCreateListener(new BuildRoleSelectorCommand());

        // Register Listeners
        api.addSelectMenuChooseListener(new RoleSelectorListener());
        /*
         * WARNING:
         * I strongly recommend not enabling the announcements' listener,
         * it's functionally WILL BE awful since it lacks proper testing.
         */
//        api.addMessageCreateListener(new AnnouncementsListener());

        if (Files.exists(pathToJsonFile)) {
            try {
                logger.info("API response file exits, deleting...");
                Files.deleteIfExists(pathToJsonFile);
                logger.info("File deleted.");
            } catch (IOException e) {
                logger.error(e);
            }
        }

        logger.info("Bot is now running!");
    }
}
