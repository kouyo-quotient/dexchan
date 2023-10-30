package com.github.kouyoquotient;

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
import java.util.Objects;

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

        if(args.length == 1){
            if(Objects.equals(args[0], "-command-register")){
                logger.info("Registering commands");
                // Register commands
                SlashCommand.with("help", "Lista completa de comandos de ayuda").createGlobal(api).join();
                SlashCommand.with("funciones", "Características que están o no disponibles ahora mismo en el sitio").createGlobal(api).join();
                SlashCommand.with("feedback", "Información sobre la implementación de sugerencias").createGlobal(api).join();
                SlashCommand.with("diagnostico", "Tabla de diagnóstico sobre problemas de acceso al sitio").createGlobal(api).join();
                SlashCommand.with("isrgx1", "Instrucciones sobre problemas con certificados al acceder al sitio").createGlobal(api).join();
                SlashCommand.with("aprobacion", "Información sobre la cola de aprobación de capítulos").createGlobal(api).join();
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

        api.addMessageCreateListener(new Pull());

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
