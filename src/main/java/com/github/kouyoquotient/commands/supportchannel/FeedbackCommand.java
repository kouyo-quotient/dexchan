package com.github.kouyoquotient.commands.supportchannel;

import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.message.mention.AllowedMentions;
import org.javacord.api.entity.message.mention.AllowedMentionsBuilder;
import org.javacord.api.event.interaction.SlashCommandCreateEvent;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.listener.interaction.SlashCommandCreateListener;
import org.javacord.api.listener.message.MessageCreateListener;

import static com.github.kouyoquotient.Main.logger;
import static com.github.kouyoquotient.utils.Constants.SUPPORT_CHANNEL;

public class FeedbackCommand implements MessageCreateListener, SlashCommandCreateListener {

    AllowedMentions allowedMentions = new AllowedMentionsBuilder()
            .setMentionEveryoneAndHere(false)
            .setMentionUsers(true)
            .setMentionRoles(false)
            .build();

    String message = String.valueOf(new MessageBuilder()
            .setAllowedMentions(allowedMentions)
            .append("Ya que MangaDex se encuentra actualmente en Accesso Anticipado\u2122\uFE0F, nuestra prioridad actual es implementar caracter\u00EDsticas para que la gente las use. En un futuro pr\u00F3ximo, la apariencia del sitio cambiar\u00E1 dr\u00E1sticamente a medida que nuestro enfoque pase de la implementaci\u00F3n a la presentaci\u00F3n. Por lo tanto, no estamos aceptando muchas sugerencias en este momento. Si tienes alg\u00FAn error que reportar con la implementaci\u00F3n de alguna caracter\u00EDstica actual, no dudes en hac\u00E9rnoslo saber.").getStringBuilder());

    @Override
    public void onMessageCreate(MessageCreateEvent event) {
        if (event.getMessageContent().equalsIgnoreCase("!feedback")) {
            // Command is restricted to the support channel
            if (event.getChannel().getId() != SUPPORT_CHANNEL) {
                return;
            }

            logger.info("Received instruction for command feedback");
            event.getChannel().sendMessage(message);
        }
    }

    @Override
    public void onSlashCommandCreate(SlashCommandCreateEvent event) {
        // Command is restricted to the support channel
        if (event.getInteraction().getChannel().orElseThrow().getId() != SUPPORT_CHANNEL) {
            return;
        }

        if (event.getSlashCommandInteraction().getFullCommandName().equalsIgnoreCase("feedback")) {
            event.getInteraction().createImmediateResponder().setContent(message).respond();
        }
    }
}
