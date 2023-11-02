package com.github.kouyoquotient.listeners;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.message.WebhookMessageBuilder;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.webhook.IncomingWebhook;
import org.javacord.api.event.server.member.ServerMemberJoinEvent;
import org.javacord.api.listener.server.member.ServerMemberJoinListener;

import java.awt.*;

import static com.github.kouyoquotient.utils.Constants.CURRENT_SERVER_ID;
import static com.github.kouyoquotient.utils.Constants.JOIN_EVENT_WEBHOOK_URL;

public class UserJoinListener implements ServerMemberJoinListener {
    private static final Logger logger = LogManager.getRootLogger();

    @Override
    public void onServerMemberJoin(ServerMemberJoinEvent event) {
        if(event.getServer().getId() != CURRENT_SERVER_ID){
            return;
        }

        logger.info("Called ServerMemberJoinEvent");
        DiscordApi api = event.getApi();

        IncomingWebhook webhook = api.getIncomingWebhookByUrl(JOIN_EVENT_WEBHOOK_URL).join();

        long getTimestamp = event.getUser().getJoinedAtTimestamp(event.getServer()).orElseThrow().getEpochSecond();

        new WebhookMessageBuilder()
                .setDisplayAvatar(api.getYourself().getAvatar())
                .setDisplayName(api.getYourself().getName())
                .addEmbed(new EmbedBuilder()
                        .setTitle("<:newmember:1061067766616031283> Nuevo miembro")
                        .setAuthor(event.getUser().getDiscriminatedName(), "", event.getUser().getAvatar())
                        .setDescription("<:joinemoji:1030913139627282472> \u00A1Bienvenido <@" + event.getUser().getId() + ">! \n Eres el usuario #" + event.getServer().getMemberCount() + " en el servidor.")
                        .addInlineField("<:id:1030914893643268157> ID:", String.valueOf(event.getUser().getId()))
                        .addInlineField("<:clock:1030915302323650590> Fecha de llegada:", "<t:" + getTimestamp + ":F>")
                        .setColor(new Color(58, 145, 90)))
                .send(webhook);

    }
}
