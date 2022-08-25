package com.github.kouyoquotient.listeners;

import org.javacord.api.DiscordApi;
import org.javacord.api.entity.message.MessageAttachment;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.message.mention.AllowedMentions;
import org.javacord.api.entity.message.mention.AllowedMentionsBuilder;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.listener.message.MessageCreateListener;

import java.util.List;

import static com.github.kouyoquotient.utils.Constants.announceReceiveChannel;
import static com.github.kouyoquotient.utils.Constants.announceSendChannel;

public class AnnouncementsListener implements MessageCreateListener {

    /*
     * Is not recommended to use this in any server yet,
     * it hasn't been properly tested
     */
    @Override
    public void onMessageCreate(MessageCreateEvent event) {
        DiscordApi api = event.getApi();
        List<MessageAttachment> msgAttach = event.getMessageAttachments();
        String announceMSG = event.getMessageContent();
        String cutAnnounceMSG1 = announceMSG.substring(0, Math.min(1930, announceMSG.length()));
        String cutAnnounceMSG2 = announceMSG.substring(cutAnnounceMSG1.length(), Math.min(2000, announceMSG.length()));

        if (event.getChannel().getId() != announceReceiveChannel) {
            return;
        }
        if (event.getMessageAuthor().isBotUser()) {
            return;
        }

        AllowedMentions allowedMentions = new AllowedMentionsBuilder()
                .setMentionRoles(false)
                .setMentionUsers(false)
                .setMentionEveryoneAndHere(false)
                .build();

        if (!cutAnnounceMSG1.isEmpty()) {
            if (msgAttach.listIterator().hasNext()) {
                new MessageBuilder()
                        .setAllowedMentions(allowedMentions)
                        .append(cutAnnounceMSG1)
                        .addAttachment(msgAttach.listIterator().next().getUrl())
                        .send(api.getChannelById(announceSendChannel).orElseThrow().asTextChannel().orElseThrow());
                new MessageBuilder()
                        .setAllowedMentions(allowedMentions)
                        .append(cutAnnounceMSG2)
                        .addAttachment(msgAttach.listIterator().next().getUrl())
                        .send(api.getChannelById(announceSendChannel).orElseThrow().asTextChannel().orElseThrow());
            }
            new MessageBuilder()
                    .setAllowedMentions(allowedMentions)
                    .append("> Anuncio reenviado desde: **MangaDex Announcements #announcements**")
                    .appendNewLine()
                    .append("\n" + cutAnnounceMSG1)
                    .send(api.getChannelById(announceSendChannel).orElseThrow().asTextChannel().orElseThrow());
            new MessageBuilder()
                    .setAllowedMentions(allowedMentions)
                    .appendNewLine()
                    .append(cutAnnounceMSG2)
                    .send(api.getChannelById(announceSendChannel).orElseThrow().asTextChannel().orElseThrow());
        }

        if (msgAttach.listIterator().hasNext()) {
            new MessageBuilder()
                    .setAllowedMentions(allowedMentions)
                    .append(announceMSG)
                    .addAttachment(msgAttach.listIterator().next().getUrl())
                    .send(api.getChannelById(announceSendChannel).orElseThrow().asTextChannel().orElseThrow());
        }
        new MessageBuilder()
                .setAllowedMentions(allowedMentions)
                .append("> Anuncio reenviado desde: **MangaDex Announcements #announcements**")
                .appendNewLine()
                .append("\n" + announceMSG)
                .send(api.getChannelById(announceSendChannel).orElseThrow().asTextChannel().orElseThrow());
    }
}
