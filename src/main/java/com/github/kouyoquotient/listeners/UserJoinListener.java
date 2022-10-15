package com.github.kouyoquotient.listeners;

import org.javacord.api.DiscordApi;
import org.javacord.api.entity.message.WebhookMessageBuilder;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.webhook.IncomingWebhook;
import org.javacord.api.event.server.member.ServerMemberJoinEvent;
import org.javacord.api.listener.server.member.ServerMemberJoinListener;

import static com.github.kouyoquotient.Main.logger;
import static com.github.kouyoquotient.utils.Constants.*;

public class UserJoinListener implements ServerMemberJoinListener {

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
                        .setTitle("Nuevo miembro")
                        .setAuthor(event.getUser().getDiscriminatedName(), "", event.getUser().getAvatar())
                        .setDescription("<:joinemoji:1030913139627282472> \u00A1 <@" + event.getUser().getId() + "> es el usuario #" + event.getServer().getMemberCount() + " en el servidor!")
                        .addInlineField("<:id:1030914893643268157> ID:", String.valueOf(event.getUser().getId()))
                        .addInlineField("<:clock:1030915302323650590> Fecha de llegada:", "<t:" + getTimestamp + ":R>")
                        .setColor(USER_JOIN_COLOR))
                .send(webhook);
    }
}
