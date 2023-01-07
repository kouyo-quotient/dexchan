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
        SlashCommand.with("help", "Lista completa de comandos de ayuda").createGlobal(api).join();
        api.addMessageCreateListener(new HelpCommand());
        api.addSlashCommandCreateListener(new HelpCommand());

        SlashCommand.with("funciones", "Caracter\u00EDsticas que est\u00E1n o no disponibles ahora mismo en el sitio").createGlobal(api).join();
        api.addMessageCreateListener(new FuncionesCommand());
        api.addSlashCommandCreateListener(new FuncionesCommand());

        SlashCommand.with("feedback", "Informaci\u00F3n sobre la implementaci\u00F3n de sugerencias").createGlobal(api).join();
        api.addMessageCreateListener(new FeedbackCommand());
        api.addSlashCommandCreateListener(new FeedbackCommand());

        SlashCommand.with("diagnostico", "Tabla de diagn\u00F3stico sobre problemas de acceso al sitio").createGlobal(api).join();
        api.addMessageCreateListener(new DiagnosticoCommand());
        api.addSlashCommandCreateListener(new DiagnosticoCommand());

        SlashCommand.with("isrgx1", "Instrucciones sobre problemas con certificados al acceder al sitio").createGlobal(api).join();
        api.addMessageCreateListener(new Isrgx1Command());
        api.addSlashCommandCreateListener(new Isrgx1Command());

        SlashCommand.with("aprobacion", "Informaci\u00F3n sobre la cola de aprobaci\u00F3n de cap\u00EDtulos").createGlobal(api).join();
        api.addMessageCreateListener(new AprobacionCommand());
        api.addSlashCommandCreateListener(new AprobacionCommand());

        SlashCommand.with("ping", "Test command").createGlobal(api).join();
        api.addSlashCommandCreateListener(new PingCommand());

        api.addMessageCreateListener(new Pull());
        api.addMessageCreateListener(new BuildRoleSelectorCommand());

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
