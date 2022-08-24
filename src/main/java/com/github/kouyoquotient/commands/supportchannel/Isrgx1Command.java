package com.github.kouyoquotient.commands.supportchannel;

import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.message.mention.AllowedMentions;
import org.javacord.api.entity.message.mention.AllowedMentionsBuilder;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.listener.message.MessageCreateListener;

import static com.github.kouyoquotient.Main.logger;
import static com.github.kouyoquotient.utils.Constants.SUPPORT_CHANNEL;

public class Isrgx1Command implements MessageCreateListener {

    @Override
    public void onMessageCreate(MessageCreateEvent event) {
        if (event.getMessageContent().equalsIgnoreCase("!isrgx1")) {
            // Command is restricted to the support channel
            if (event.getChannel().getId() != SUPPORT_CHANNEL) {
                return;
            }

            logger.info("Received instruction for command isrgx1");
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
                            Los usuarios de Chrome en versiones muy antiguas de Android (menor a la 7.1.1) pueden experimentar problemas con los certificados SSL debido a la falta del certificado ra\u00EDz X1 de Letsencrypt.
                                                        
                            Intenta abrir <https://valid-isrgrootx1.letsencrypt.org/>. Si te sale un error de SSL, tienes 3 opciones:
                            - Actualizar tu versi\u00F3n de Android
                            - Utilizar otro navegador
                                                        
                            Como \u00FAltimo recurso, puedes seguir siguientes pasos:
                            1. Descarga el certificado de <https://letsencrypt.org/es/certificates/>
                            (el que est\u00E1 en: `Activo \u2192 ISRG Root X1 \u2192 Autofirmado \u2192 pem`)
                            2. Instala el certificado siguiendo las instrucciones en <https://support.google.com/pixelphone/answer/2844832?hl=es>
                            """)
                    .send(event.getChannel());
        }
    }
}
