package com.github.kouyoquotient.commands.supportchannel;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.message.mention.AllowedMentions;
import org.javacord.api.entity.message.mention.AllowedMentionsBuilder;
import org.javacord.api.event.interaction.SlashCommandCreateEvent;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.listener.interaction.SlashCommandCreateListener;
import org.javacord.api.listener.message.MessageCreateListener;

import static com.github.kouyoquotient.utils.Constants.SUPPORT_CHANNEL;
import static org.javacord.api.entity.message.MessageFlag.EPHEMERAL;

public class FeedbackCommand implements MessageCreateListener, SlashCommandCreateListener {
    private static final Logger logger = LogManager.getRootLogger();

    AllowedMentions allowedMentions = new AllowedMentionsBuilder()
            .setMentionEveryoneAndHere(false)
            .setMentionUsers(true)
            .setMentionRoles(false)
            .build();

    String message = String.valueOf(new MessageBuilder()
            .setAllowedMentions(allowedMentions)
            .append("""
                    Las sugerencias deben ir en los foros respectivos:
                    - Para el sitio principal: <https://forums.mangadex.org/forums/mainsite-suggestions.99/
                    - Para los foros: <https://forums.mangadex.org/forums/forum-suggestions.104/
                    - Para la API: <https://forums.mangadex.org/forums/api-suggestions.144/
                    """).getStringBuilder());

    @Override
    public void onMessageCreate(MessageCreateEvent event) {
        if (event.getMessageContent().equalsIgnoreCase("!feedback")) {
            logger.info("Received legacy command feedback");

            // Command is restricted to the support channel
            if (event.getChannel().getId() != SUPPORT_CHANNEL) {
                event.getMessageAuthor().getMessage().reply("No puedes usar ese comando aqu\u00ED, pero puedes probar usando el comando de barra diagonal. \n`/feedback`");
                logger.info("Invoker not in support channel, exiting");
                return;
            }

            event.getChannel().sendMessage(message);
        }
    }

    @Override
    public void onSlashCommandCreate(SlashCommandCreateEvent event) {
        if (event.getSlashCommandInteraction().getFullCommandName().equalsIgnoreCase("feedback")) {
            logger.info("Received SlashCommand feedback");

            // Send as ephemeral if not in support channel
            if (event.getInteraction().getChannel().orElseThrow().getId() != SUPPORT_CHANNEL) {
                logger.info("Running instructions for not-in-support-channel");
                event.getInteraction().createImmediateResponder().setFlags(EPHEMERAL).setContent(message).respond();
                return;
            }

            event.getInteraction().createImmediateResponder().setContent(message).respond();
        }
    }
}
