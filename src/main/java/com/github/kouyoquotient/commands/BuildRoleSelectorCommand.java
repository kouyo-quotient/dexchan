package com.github.kouyoquotient.commands;

import com.vdurmont.emoji.EmojiParser;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.message.component.ActionRow;
import org.javacord.api.entity.message.component.SelectMenu;
import org.javacord.api.entity.message.component.SelectMenuOption;
import org.javacord.api.entity.message.mention.AllowedMentions;
import org.javacord.api.entity.message.mention.AllowedMentionsBuilder;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.listener.message.MessageCreateListener;

import java.util.ArrayList;

import static com.github.kouyoquotient.Main.logger;

public class BuildRoleSelectorCommand implements MessageCreateListener {
    AllowedMentions allowedMentions = new AllowedMentionsBuilder()
            .setMentionEveryoneAndHere(false)
            .setMentionUsers(true)
            .setMentionRoles(false)
            .build();

    String message = String.valueOf(new MessageBuilder()
            .setAllowedMentions(allowedMentions)
            .append("""
                    __**Selector de roles**__
                    Todos los roles aqu\u00ED son gratis y opcionales. Elige el que quieras:
                                        
                    :writing_hand: <@&968318880927850566>: Miembro de uno, dos o m\u00E1s grupos scanlation/fansub.
                    :books: <@&968318841430085772>: Lector activo y aficionado.
                    
                    <:twitter:1068974812229288027> **Twitter Posts**: __Rol no disponible__
                    <:mangadex:1003474388315803710> **MangaDex Posts**: __Rol no disponible__
                    """).getStringBuilder());

    @Override
    public void onMessageCreate(MessageCreateEvent event) {
        if (event.getMessageContent().equalsIgnoreCase("!birl")) {
            DiscordApi api = event.getApi();
            // Command is restricted to the bot owner.
            if (!event.getMessageAuthor().isBotOwner()) {
                return;
            }

            logger.info("Received command for buildroleselector instruction");

            ArrayList<SelectMenuOption> iSelectMenuOptions = new ArrayList<>();
            iSelectMenuOptions.add(SelectMenuOption.create("Scanlator", "scanlator", "Seleccionar rol Scanlator", EmojiParser.parseToUnicode(":writing_hand:")));
            iSelectMenuOptions.add(SelectMenuOption.create("Lector", "lector", "Seleccionar rol Lector", EmojiParser.parseToUnicode(":books:")));
            // You can't add custom emojis like above, so I'll leave this one like this for now.
            iSelectMenuOptions.add(SelectMenuOption.create("Limpiar roles", "neutral", "Esta opci\u00F3n remover\u00E1 tus roles seleccionados", api.getKnownCustomEmojiOrCreateCustomEmoji(1068975870338924625L, "delete", false)));

            new MessageBuilder()
                    .setAllowedMentions(allowedMentions)
                    .append(message)
                    .addComponents(ActionRow.of(
                            SelectMenu.createStringMenu(
                                    "iroleselector",
                                    "Click aqu\u00ED para seleccionar tu rol",
                                    iSelectMenuOptions)))
                    .send(event.getChannel());
            event.getMessage().delete();

            logger.info("Successfully ran instructions and deleted message");
        }
    }
}
