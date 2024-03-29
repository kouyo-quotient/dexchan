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

public class FuncionesCommand implements MessageCreateListener, SlashCommandCreateListener {
    private static final Logger logger = LogManager.getRootLogger();

    AllowedMentions allowedMentions = new AllowedMentionsBuilder()
            .setMentionEveryoneAndHere(false)
            .setMentionUsers(true)
            .setMentionRoles(false)
            .build();

    String message = String.valueOf(new MessageBuilder()
            .setAllowedMentions(allowedMentions)
            .append("""
                    Lo que puedes hacer ahora mismo:
                    1. Buscar obras.
                    2. Leer cap\u00EDtulos.
                    3. Subir cap\u00EDtulos.
                    4. Registrar una cuenta. (Una vez por hora)
                    5. Seguir/dejar de seguir una obra. (Teniendo una sesi\u00F3n iniciada)
                    6. Ver tus pendientes, leyendo, completados y abandonados
                    7. Gestionar tus listas personales. (Piensa en ellas como listas tem\u00E1ticas de mangas que puedes compartir con otros)
                    8. Crear/gestionar/buscar grupos
                    9. Crear/editar obras.
                    10. Crear/editar/buscar autores
                    11. Subir/editar/borrar cap\u00EDtulos
                    12. Calificar una obra.
                                                
                    Lo que no puedes hacer (de momento):
                    1. Usar los foros
                    2. Comentar en las obras/cap\u00EDtulos/etc.
                    3. Cualquier otra cosa que no este en la lista anterior.
                                                
                    Si necesitas que se arregle algo, utiliza los botones de reporte situados en varios lugares del sitio, los moderadores se encargaran de ello. Si tienes problemas con tu cuenta, envia un correo a **support@mangadex__.org__** para obtener ayuda.""").getStringBuilder());

    @Override
    public void onMessageCreate(MessageCreateEvent event) {

        if (event.getMessageContent().equalsIgnoreCase("!funciones")) {
            logger.info("Received legacy command funciones");

            // Command is restricted to the support channel
            if (event.getChannel().getId() != SUPPORT_CHANNEL) {
                event.getMessageAuthor().getMessage().reply("No puedes usar ese comando aqu\u00ED, pero puedes probar usando el comando de barra diagonal. \n`/funciones`");
                logger.info("Invoker not in support channel, exiting");
                return;
            }

            event.getChannel().sendMessage(message);
        }
    }

    @Override
    public void onSlashCommandCreate(SlashCommandCreateEvent event) {
        if (event.getSlashCommandInteraction().getFullCommandName().equalsIgnoreCase("funciones")) {
            logger.info("Received SlashCommand funciones");

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
