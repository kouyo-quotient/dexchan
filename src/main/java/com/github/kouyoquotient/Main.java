package com.github.kouyoquotient;

import com.github.kouyoquotient.commands.BuildRoleSelectorCommand;
import com.github.kouyoquotient.commands.Poke;
import com.github.kouyoquotient.commands.support.DiagnosticoCommand;
import com.github.kouyoquotient.commands.support.FeedbackCommand;
import com.github.kouyoquotient.commands.support.FuncionesCommand;
import com.github.kouyoquotient.listeners.RoleSelectorListener;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;

public class Main {
    public static final Logger logger = LogManager.getRootLogger();
    static String token = System.getenv("TOKEN");

    public static void main(String[] args) {
        DiscordApi api = new DiscordApiBuilder()
                .setToken(token)
                .login()
                .join();

        // Register commands
        api.addMessageCreateListener(new FuncionesCommand());
        api.addMessageCreateListener(new FeedbackCommand());
        api.addMessageCreateListener(new DiagnosticoCommand());
        api.addMessageCreateListener(new Poke());
        api.addMessageCreateListener(new BuildRoleSelectorCommand());

        // Register Listeners
        api.addMessageComponentCreateListener(new RoleSelectorListener());
        logger.info("Bot is now running!");


    }
}
