package com.github.kouyoquotient.commands.supportchannel;

import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.message.mention.AllowedMentions;
import org.javacord.api.entity.message.mention.AllowedMentionsBuilder;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.listener.message.MessageCreateListener;

import static com.github.kouyoquotient.Main.logger;
import static com.github.kouyoquotient.utils.Constants.SUPPORT_CHANNEL;

public class HelpCommand implements MessageCreateListener {

    @Override
    public void onMessageCreate(MessageCreateEvent event) {
        if (event.getMessageContent().equalsIgnoreCase("!help")) {
            // Command is restricted to the support channel
            if (event.getChannel().getId() != SUPPORT_CHANNEL) {
                return;
            }

            logger.info("Received instruction for command help");
            long authorId = event.getMessageAuthor().getId();

            AllowedMentions allowedMentions = new AllowedMentionsBuilder()
                    .setMentionEveryoneAndHere(false)
                    .setMentionUsers(true)
                    .setMentionRoles(false)
                    .build();

            new MessageBuilder()
                    .setAllowedMentions(allowedMentions)
                    .append("> <@" + authorId + ">")
                    .appendNewLine()
                    .append("""
                            Si necesitas ayuda, prueba revisando estos comandos:
                            
                            > **!diagnostico**: Tabla de diagn\u00F3stico sobre problemas de acceso al sitio.
                            > **!feedback**: Informaci\u00F3n sobre la implementaci\u00F3n de sugerencias.
                            > **!funciones**: Caracter\u00EDsticas que est\u00E1n o no disponibles ahora mismo en el sitio.
                            > **!isrgx1**: Instrucciones sobre problemas con certificados al acceder al sitio.
                            
                            Si ning\u00FAn comando te fue de ayuda, considera consultar con un moderador.
                            """)
                    .send(event.getChannel());
        }
    }
}
