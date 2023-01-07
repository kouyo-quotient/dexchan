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
import static org.javacord.api.entity.message.MessageFlag.EPHEMERAL;

public class HelpCommand implements MessageCreateListener, SlashCommandCreateListener {

    AllowedMentions allowedMentions = new AllowedMentionsBuilder()
            .setMentionEveryoneAndHere(false)
            .setMentionUsers(true)
            .setMentionRoles(false)
            .build();

    String message = String.valueOf(
            new MessageBuilder()
                    .setAllowedMentions(allowedMentions)
                    .append("""
                            Si necesitas ayuda, prueba revisando estos comandos:
                                                        
                            > **/diagnostico**: Tabla de diagn\u00F3stico sobre problemas de acceso al sitio.
                            > **/feedback**: Informaci\u00F3n sobre la implementaci\u00F3n de sugerencias para el sitio.
                            > **/funciones**: Caracter\u00EDsticas que est\u00E1n o no disponibles ahora mismo en el sitio.
                            > **/isrgx1**: Instrucciones sobre problemas con certificados al acceder al sitio.
                            > **/aprobacion**: Informaci\u00F3n sobre la aprobaci\u00F3n de cap\u00EDtulos.
                                                        
                            Si ning\u00FAn comando te fue de ayuda, considera consultar con un moderador.
                            """)
                    .getStringBuilder());

    @Override
    public void onMessageCreate(MessageCreateEvent event) {
        if (event.getMessageContent().equalsIgnoreCase("!help")) {
            logger.info("Received legacy command help");

            // Command is restricted to the support channel
            if (event.getChannel().getId() != SUPPORT_CHANNEL) {
                event.getMessageAuthor().getMessage().reply("No puedes usar ese comando aqu\u00ED, pero puedes probar usando el comando de barra diagonal. \n`/help`");
                logger.info("Invoker not in support channel, exiting");
                return;
            }

            event.getChannel().sendMessage(message);
        }
    }

    @Override
    public void onSlashCommandCreate(SlashCommandCreateEvent event) {
        if (event.getSlashCommandInteraction().getFullCommandName().equalsIgnoreCase("help")) {
            logger.info("Received SlashCommand help");

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
