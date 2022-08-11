package com.github.kouyoquotient.commands;

import com.vdurmont.emoji.EmojiParser;
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
        if (event.getMessageContent().equalsIgnoreCase("!buildroleselector")) {
            if (!event.getMessageAuthor().isBotOwner()) {
                event.getChannel().sendMessage("Comando restringido.");
                return;
            }

            logger.info("Received command component instruction");

            EmbedBuilder embed = new EmbedBuilder()
                    .setTitle("Elige tu destino")
                    .setDescription(EmojiParser.parseToUnicode("""
                            En esta tierra no tan inh\u00F3spita pero vasta; hay incontables senderos por los que se pueden recorrer, esto ha llevado a que tres facciones sean creadas:
                                            
                            :printer: <@&968318880927850566>: Miembros del abismo de las traducciones, las leyendas cuentan que estos seres apenas ven la luz del d\u00EDa y que carecen de un alma propia.
                            :books: <@&968318841430085772>: Entidades formidables con un inmenso conocimiento, miembros de la cumbre. Se cree que es gracias a estos que las entidades del abismo de las traducciones obtienen su poder.
                            <:dex:1003474388315803710> **Neutral:** Merodeadores que eligeron no apoyar a ninguna facci\u00F3n. Posiblemente sean aquellos quienes traigan paz a la guerra.
                                            
                            \u00BFCu\u00E1l camino deseas seguir?"""))
                    .setFooter(
                            "Los roles de Scanlator y Lector podr\u00E1n recibir pings ocasionales sobre anuncios dependiendo del caso. Si seleccionas el rol de Scanlator se te preguntar\u00E1 por el scan del que formas parte.",
                            "https://cdn.discordapp.com/attachments/864265368447746098/864274483664388136/84dc32a3-355b-4f6d-865c-c08d0c2ec6c4.png");
            /*
             * TODO: Search about how the fuck insert emojis into this shit
             */
            ArrayList<SelectMenuOption> menuOptions = new ArrayList<>();
            menuOptions.add(SelectMenuOption.create("Scanlator", "one", "Seleccionar rol Scanlator"));
            menuOptions.add(SelectMenuOption.create("Lector", "two", "Seleccionar rol Lector"));
            menuOptions.add(SelectMenuOption.create("Neutral", "three", "Seleccionar rol Neutral"));

            new MessageBuilder()
                    .addEmbed(embed)
                    .addComponents(
                            ActionRow.of(SelectMenu.create("options", "Click aqu\u00ED para seleccionar tu facci\u00F3n", 1, 1, menuOptions))
                    ).send(event.getChannel());
        }
    }
}
