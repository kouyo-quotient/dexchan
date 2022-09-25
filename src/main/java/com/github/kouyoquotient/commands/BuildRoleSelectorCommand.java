package com.github.kouyoquotient.commands;

import com.vdurmont.emoji.EmojiParser;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.emoji.Emoji;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.message.component.ActionRow;
import org.javacord.api.entity.message.component.SelectMenu;
import org.javacord.api.entity.message.component.SelectMenuOption;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.listener.message.MessageCreateListener;

import java.util.ArrayList;

import static com.github.kouyoquotient.Main.logger;
import static com.github.kouyoquotient.utils.Constants.*;

public class BuildRoleSelectorCommand implements MessageCreateListener {
    @Override
    public void onMessageCreate(MessageCreateEvent event) {
        if (event.getMessageContent().equalsIgnoreCase("!buildroleselector")) {
            DiscordApi api = event.getApi();
            // Command is restricted to the bot owner.
            if (!event.getMessageAuthor().isBotOwner()) {
                return;
            }

            logger.info("Received command for buildroleselector instruction");

            EmbedBuilder embed = new EmbedBuilder()
                    .setTitle("Elige tu destino")
                    .setDescription(EmojiParser.parseToUnicode("""
                            En esta tierra no tan inh\u00F3spita pero vasta; hay incontables senderos por los que se pueden recorrer, esto ha llevado a que tres facciones sean creadas:
                                            
                            :writing_hand: <@&968318880927850566>: Miembros del abismo de las traducciones, las leyendas cuentan que estos seres apenas ven la luz del d\u00EDa y que carecen de un alma propia.
                            :books: <@&968318841430085772>: Entidades formidables con un inmenso conocimiento, miembros de la cumbre. Se cree que es gracias a estos que las entidades del abismo de las traducciones obtienen su poder.
                            <:dex:1003474388315803710> **Neutral:** Merodeadores que eligieron no apoyar a ninguna facci\u00F3n. Posiblemente sean aquellos quienes traigan paz a la guerra.
                                            
                            \u00BFCu\u00E1l camino deseas seguir?"""))
                    .setFooter(
                            "Los roles de Scanlator y Lector podr\u00E1n recibir pings ocasionales sobre anuncios dependiendo del caso. Si seleccionas el rol de Scanlator se te preguntar\u00E1 por el scan del que formas parte.",
                            "https://cdn.discordapp.com/attachments/864265368447746098/864274483664388136/84dc32a3-355b-4f6d-865c-c08d0c2ec6c4.png")
                    .setColor(DISCORD_BG_COLOR);

            ArrayList<Emoji> selectMenuEmojis = new ArrayList<>();
            selectMenuEmojis.add(api.getKnownCustomEmojiOrCreateCustomEmoji(LECTOR_EMOJI_ID, "lector", false));
            selectMenuEmojis.add(api.getKnownCustomEmojiOrCreateCustomEmoji(SCANLATOR_EMOJI_ID, "scanlator", false));
            selectMenuEmojis.add(api.getKnownCustomEmojiOrCreateCustomEmoji(NEUTRAL_EMOJI_ID, "neutral", false));

            ArrayList<SelectMenuOption> selectMenuOptions = new ArrayList<>();
            selectMenuOptions.add(SelectMenuOption.create("Scanlator", "one", "Seleccionar rol Scanlator", selectMenuEmojis.get(1)));
            selectMenuOptions.add(SelectMenuOption.create("Lector", "two", "Seleccionar rol Lector", selectMenuEmojis.get(0)));
            selectMenuOptions.add(SelectMenuOption.create("Neutral", "three", "Esta opci\u00F3n remover\u00E1 tus roles", selectMenuEmojis.get(2)));

            new MessageBuilder()
                    .addEmbed(embed)
                    .addComponents(
                            ActionRow.of(
                                    SelectMenu.create
                                            ("options",
                                                    "Click aqu\u00ED para seleccionar tu facci\u00F3n",
                                                    1,
                                                    1,
                                                    selectMenuOptions)))
                    .send(event.getChannel());
            event.getMessage().delete();

            logger.info("Successfully ran instructions and deleted message");
        }
    }
}
