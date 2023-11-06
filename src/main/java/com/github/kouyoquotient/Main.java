package com.github.kouyoquotient;

import com.github.kouyoquotient.commands.PingCommand;
import com.github.kouyoquotient.commands.Pull;
import com.github.kouyoquotient.commands.mdcommands.SearchCommand;
import com.github.kouyoquotient.commands.mdcommands.TitleCommand;
import com.github.kouyoquotient.commands.supportchannel.*;
import com.github.kouyoquotient.listeners.TitleLinkListener;
import com.github.kouyoquotient.listeners.UserJoinListener;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.interaction.SlashCommand;
import org.javacord.api.interaction.SlashCommandOption;
import org.javacord.api.interaction.SlashCommandOptionType;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.Objects;

public class Main {
    private static final Logger logger = LogManager.getRootLogger();

    public static void main(String[] args) {
        final String token = args[0];
        DiscordApi api = new DiscordApiBuilder()
                .setToken(token)
                .setAllIntents()
                .login()
                .join();

        if(args.length > 1) {
            if (Objects.equals(args[1], "-command-register")) {
                logger.info("Registering commands");
                // Register commands
                SlashCommand.with("help", "Lista completa de comandos de ayuda").createGlobal(api).join();
                SlashCommand.with("funciones", "Caracter\u00EDsticas que est\u00E1n o no disponibles ahora mismo en el sitio").createGlobal(api).join();
                SlashCommand.with("feedback", "Informaci\u00F3n sobre la implementaci\u00F3n de sugerencias").createGlobal(api).join();
                SlashCommand.with("diagnostico", "Tabla de diagn\u00F3stico sobre problemas de acceso al sitio").createGlobal(api).join();
                SlashCommand.with("isrgx1", "Instrucciones sobre problemas con certificados al acceder al sitio").createGlobal(api).join();
                SlashCommand.with("aprobacion", "Informaci\u00F3n sobre la cola de aprobaci\u00F3n de cap\u00EDtulos").createGlobal(api).join();
                SlashCommand.with("title", "Informaci\u00F3n sobre un titulo en MangaDex",
                        Collections.singletonList(
                                SlashCommandOption.create(SlashCommandOptionType.STRING, "UUID", "UUID del t\u00EDtulo", true)
                        )).createGlobal(api).join();
                SlashCommand.with("search", "Busca un t\u00EDtulo en MangaDex",
                        Arrays.asList(
                                SlashCommandOption.create(SlashCommandOptionType.STRING, "Nombre", "Nombre del t\u00EDtulo", true),
                                SlashCommandOption.create(SlashCommandOptionType.BOOLEAN, "Lista", "Muestra los resultados en una lista para elegir", false)
                        )).createGlobal(api).join();
                SlashCommand.with("ping", "Test command").createGlobal(api).join();
            }
        }

        logger.info("Registering listeners");
        api.addSlashCommandCreateListener(new PingCommand());

        api.addMessageCreateListener(new AprobacionCommand());
        api.addSlashCommandCreateListener(new AprobacionCommand());

        api.addMessageCreateListener(new Isrgx1Command());
        api.addSlashCommandCreateListener(new Isrgx1Command());

        api.addMessageCreateListener(new DiagnosticoCommand());
        api.addSlashCommandCreateListener(new DiagnosticoCommand());

        api.addMessageCreateListener(new FeedbackCommand());
        api.addSlashCommandCreateListener(new FeedbackCommand());

        api.addMessageCreateListener(new FuncionesCommand());
        api.addSlashCommandCreateListener(new FuncionesCommand());

        api.addMessageCreateListener(new HelpCommand());
        api.addSlashCommandCreateListener(new HelpCommand());

        api.addMessageCreateListener(new TitleCommand());
        api.addSlashCommandCreateListener(new TitleCommand());

        api.addMessageCreateListener(new SearchCommand());
        api.addSlashCommandCreateListener(new SearchCommand());

        api.addMessageCreateListener(new Pull());
        api.addMessageCreateListener(new TitleLinkListener());

        api.addServerMemberJoinListener(new UserJoinListener());
        /*
         * WARNING:
         * I strongly recommend not enabling the announcements' listener,
         * it's functionally WILL BE awful since it lacks proper testing.
         */
//        api.addMessageCreateListener(new AnnouncementsListener());

        Path pathToJsonFile = Path.of("response.json");
        if (Files.exists(pathToJsonFile)) {
            try {
                logger.info("Leftover files found, deleting...");
                Files.delete(pathToJsonFile);
                logger.info("Files deleted.");
            } catch (IOException e) {
                logger.error(e);
            }
        }

        logger.info("Bot is now running!");
    }
}
