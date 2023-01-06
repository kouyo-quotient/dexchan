package com.github.kouyoquotient.commands;

import com.vdurmont.emoji.EmojiParser;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.message.component.ActionRow;
import org.javacord.api.entity.message.component.SelectMenu;
import org.javacord.api.entity.message.component.SelectMenuOption;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.listener.message.MessageCreateListener;

import java.util.ArrayList;

import static com.github.kouyoquotient.Main.logger;

public class BuildRoleSelectorCommand implements MessageCreateListener {

    @Override
    public void onMessageCreate(MessageCreateEvent event) {
        if (event.getMessageContent().equalsIgnoreCase("!birl")) {
            DiscordApi api = event.getApi();
            // Command is restricted to the bot owner.
            if (!event.getMessageAuthor().isBotOwner()) {
                return;
            }

            logger.info("Received command for buildroleselector instruction");

            EmbedBuilder embed = new EmbedBuilder()
                    .setTitle("Elige tu destino")
                    .setDescription(
                            EmojiParser.parseToUnicode(
                                    """
                                            En nuestra versi\u00F3n del mundo existe una facci\u00F3n en una constante guerra de caos y destrucci\u00F3n contra un enemigo com\u00FAn. Los miembros de esta facci\u00F3n son...
                                                            
                                            :writing_hand: <@&968318880927850566>: Criaturas del abismo de las traducciones. Todos son venerados por sus subordinados y seguidores debido a que son capaces de entender lenguajes at\u00E1vicos.
                                            :books: <@&968318841430085772>: Son conocidos en el abismo de las traducciones por fungir como catalizador y poseer vastos conocimientos de los manuscritos de [    ].
                                            <:neutral:1003474388315803710> **Neutral:** Seres sin ning\u00FAn tipo de poder especial. Veneran a las criaturas del abismo de las traducciones.
                                                            
                                            \u00BFParticipar\u00E1s en esta guerra?
                                            """))
                    .setFooter("", "https://cdn.discordapp.com/attachments/864265368447746098/864274483664388136/84dc32a3-355b-4f6d-865c-c08d0c2ec6c4.png");
//                    .setColor(DISCORD_BG_COLOR);

            ArrayList<SelectMenuOption> iSelectMenuOptions = new ArrayList<>();
            iSelectMenuOptions.add(SelectMenuOption.create("Scanlator", "scanlator", "Seleccionar rol Scanlator", EmojiParser.parseToUnicode(":writing_hand:")));
            iSelectMenuOptions.add(SelectMenuOption.create("Lector", "lector", "Seleccionar rol Lector", EmojiParser.parseToUnicode(":books:")));
            // You can't add custom emojis like above, so I'll leave this one like this for now.
            iSelectMenuOptions.add(SelectMenuOption.create("Neutral", "neutral", "Esta opci\u00F3n remover\u00E1 tus roles seleccionados", api.getKnownCustomEmojiOrCreateCustomEmoji(1003474388315803710L, "neutral", false)));

            new MessageBuilder()
                    .addEmbed(embed)
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
