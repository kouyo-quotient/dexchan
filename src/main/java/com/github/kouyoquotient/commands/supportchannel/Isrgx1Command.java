package com.github.kouyoquotient.commands.supportchannel;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.message.mention.AllowedMentions;
import org.javacord.api.entity.message.mention.AllowedMentionsBuilder;
import org.javacord.api.event.interaction.SlashCommandCreateEvent;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.listener.interaction.SlashCommandCreateListener;
import org.javacord.api.listener.message.MessageCreateListener;

import static com.github.kouyoquotient.utils.Constants.SUPPORT_CHANNEL;
import static org.javacord.api.entity.message.MessageFlag.EPHEMERAL;

public class Isrgx1Command implements MessageCreateListener, SlashCommandCreateListener {
    private static final Logger logger = LogManager.getRootLogger();

    AllowedMentions allowedMentions = new AllowedMentionsBuilder()
            .setMentionEveryoneAndHere(false)
            .setMentionUsers(true)
            .setMentionRoles(false)
            .build();

    String message = String.valueOf(new MessageBuilder()
            .setAllowedMentions(allowedMentions)
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
            .getStringBuilder());

    @Override
    public void onMessageCreate(MessageCreateEvent event) {
        if (event.getMessageContent().equalsIgnoreCase("!isrgx1")) {
            logger.info("Received legacy command isrgx1");

            // Command is restricted to the support channel
            if (event.getChannel().getId() != SUPPORT_CHANNEL) {
                event.getMessageAuthor().getMessage().reply("No puedes usar ese comando aqu\u00ED, pero puedes probar usando el comando de barra diagonal. \n`/isrgx1`");
                logger.info("Invoker not in support channel, exiting");
                return;
            }

            event.getChannel().sendMessage(message);
        }
    }

    @Override
    public void onSlashCommandCreate(SlashCommandCreateEvent event) {
        if (event.getSlashCommandInteraction().getFullCommandName().equalsIgnoreCase("isrgx1")) {
            logger.info("Received SlashCommand isrgx1");

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
