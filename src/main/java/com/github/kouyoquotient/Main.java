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

public class Main {
    public static final Logger logger = LogManager.getRootLogger();

    public static void main(String[] args) {
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
         * Do not enable the announcements' listener, it hasn't been properly tested
         */
//        api.addMessageCreateListener(new AnnouncementsListener());

        logger.info("Bot is now running!");
    }
}
