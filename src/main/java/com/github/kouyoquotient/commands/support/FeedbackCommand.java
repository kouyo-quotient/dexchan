package com.github.kouyoquotient.commands.support;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.message.mention.AllowedMentions;
import org.javacord.api.entity.message.mention.AllowedMentionsBuilder;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.listener.message.MessageCreateListener;

public class FeedbackCommand implements MessageCreateListener {
    static final Logger logger = LogManager.getRootLogger();

    @Override
    public void onMessageCreate(MessageCreateEvent event) {
        long supportChannel = 864252859296907294L;

        if (event.getChannel().getId() != supportChannel) {
            return;
        }

        if (event.getMessageContent().equalsIgnoreCase("!feedback")) {
            logger.info("Received funciones command instruction");
            long authorId = event.getMessageAuthor().getId();

            AllowedMentions allowedMentions = new AllowedMentionsBuilder()
                    .setMentionEveryoneAndHere(false)
                    .setMentionUsers(true)
                    .setMentionRoles(false)
                    .build();

            new MessageBuilder()
                    .setAllowedMentions(allowedMentions)
                    .append("> <@" + authorId + ">")
                    .appendNewLine()
                    .append("Ya que MangaDex se encuentra actualmente en Accesso Anticipado\u2122\uFE0F, nuestra prioridad actual es implementar caracter\u00EDsticas para que la gente las use. En un futuro pr\u00F3ximo, la apariencia del sitio cambiar\u00E1 dr\u00E1sticamente a medida que nuestro enfoque pase de la implementaci\u00F3n a la presentaci\u00F3n. Por lo tanto, no estamos aceptando muchas sugerencias en este momento. Si tienes alg\u00FAn error que reportar con la implementaci\u00F3n de alguna caracter\u00EDstica actual, no dudes en hac\u00E9rnoslo saber.")

                    .send(event.getChannel());
        }
    }
}
