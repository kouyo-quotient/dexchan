package com.github.kouyoquotient.listeners;

import org.javacord.api.DiscordApi;
import org.javacord.api.entity.Icon;
import org.javacord.api.entity.message.MessageAttachment;
import org.javacord.api.entity.message.WebhookMessageBuilder;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.message.mention.AllowedMentions;
import org.javacord.api.entity.message.mention.AllowedMentionsBuilder;
import org.javacord.api.entity.webhook.IncomingWebhook;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.listener.message.MessageCreateListener;

import java.awt.image.BufferedImage;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static com.github.kouyoquotient.Main.logger;
import static com.github.kouyoquotient.utils.Constants.*;

public class AnnouncementsListener implements MessageCreateListener {

    /*
     * WARNING:
     * I strongly recommend not enabling the announcements' listener,
     * it's functionally WILL BE awful since it lacks proper testing.
     */

    @Override
    public void onMessageCreate(MessageCreateEvent event) {
        if (event.getChannel().getId() != announceReceiveChannel) {
            return;
        }

        DiscordApi api = event.getApi();
        List<MessageAttachment> getAttachments = event.getMessageAttachments();

        String announceMSG = event.getMessageContent();
        CompletableFuture<IncomingWebhook> webhook = api.getIncomingWebhookByUrl(ANNOUNCEMENT_WEBHOOK_URL);


        if (event.getMessageAuthor().isBotUser()) {
            return;
        }

        AllowedMentions allowedMentions = new AllowedMentionsBuilder()
                .setMentionRoles(false)
                .setMentionUsers(false)
                .setMentionEveryoneAndHere(false)
                .build();

        if (getAttachments.listIterator().hasNext()) {
            BufferedImage fileAttachment;
            try {
                fileAttachment = getAttachments.listIterator().next().asImage().get();
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }

            new WebhookMessageBuilder()
                    .setAllowedMentions(allowedMentions)
                    .setDisplayAvatar(event.getMessageAuthor().getAvatar())
                    .setDisplayName(event.getMessageAuthor().getDisplayName())
                    .addEmbed(new EmbedBuilder()
                            .setDescription(announceMSG))
                    .addAttachment((Icon) fileAttachment)
                    .send(webhook.join());
            logger.info("Called case 1");
            return;
        }

        new WebhookMessageBuilder()
                .setAllowedMentions(allowedMentions)
                .setDisplayAvatar(event.getMessageAuthor().getAvatar())
                .setDisplayName(event.getMessageAuthor().getDisplayName())
                .addEmbed(new EmbedBuilder()
                        .setDescription(announceMSG))
                .send(webhook.join());
        logger.info("Called case 2");
    }
}
