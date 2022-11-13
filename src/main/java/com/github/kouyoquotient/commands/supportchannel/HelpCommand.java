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
                            
                            > **!diagnostico**: Tabla de diagn\u00F3stico sobre problemas de acceso al sitio.
                            > **!feedback**: Informaci\u00F3n sobre la implementaci\u00F3n de sugerencias.
                            > **!funciones**: Caracter\u00EDsticas que est\u00E1n o no disponibles ahora mismo en el sitio.
                            > **!isrgx1**: Instrucciones sobre problemas con certificados al acceder al sitio.
                            
                            Si ning\u00FAn comando te fue de ayuda, considera consultar con un moderador.
                            """)
                    .getStringBuilder());

    @Override
    public void onMessageCreate(MessageCreateEvent event) {
        if (event.getMessageContent().equalsIgnoreCase("!help")) {
            // Command is restricted to the support channel
            if (event.getChannel().getId() != SUPPORT_CHANNEL) {
                return;
            }

            logger.info("Received instruction for command help");
            event.getChannel().sendMessage(message);
        }
    }

    @Override
    public void onSlashCommandCreate(SlashCommandCreateEvent event) {
        // Command is restricted to the support channel
        if (event.getInteraction().getChannel().orElseThrow().getId() != SUPPORT_CHANNEL) {
            return;
        }

        if(event.getSlashCommandInteraction().getFullCommandName().equalsIgnoreCase("help")){
            event.getInteraction().createImmediateResponder().setContent(message).respond();
        }
    }
}
