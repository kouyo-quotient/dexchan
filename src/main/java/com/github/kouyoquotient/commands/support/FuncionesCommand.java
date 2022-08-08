package com.github.kouyoquotient.commands.support;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.message.mention.AllowedMentions;
import org.javacord.api.entity.message.mention.AllowedMentionsBuilder;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.listener.message.MessageCreateListener;

public class FuncionesCommand implements MessageCreateListener {
    static final Logger logger = LogManager.getRootLogger();

    @Override
    public void onMessageCreate(MessageCreateEvent event) {
        long supportChannel = 864252859296907294L;

        if(event.getChannel().getId() != supportChannel){
            return;
            }

        if (event.getMessageContent().equalsIgnoreCase("!funciones")) {
            logger.info("Received funciones command instruction");
            long authorId = event.getMessageAuthor().getId();

            AllowedMentions allowedMentions = new AllowedMentionsBuilder()
                    .addUser(authorId)
                    .setMentionEveryoneAndHere(false)
                    .build();

            /*
             * TODO: Insert missing unicode characters for proper grammar.
             */
            new MessageBuilder()
                    .setAllowedMentions(allowedMentions)
                    .append("<@"+authorId+">")
                    .appendNewLine()
                    .append("""
                            \nLo que puedes hacer ahora mismo:
                            1. Buscar obras.
                            2. Leer capitulos.
                            3. Subir capitulos.
                            4. Registrar una cuenta. (Una vez por hora)
                            5. Seguir/dejar de seguir una obra. (Teniendo una sesion iniciada)
                            6. Ver tus pendientes, leyendo, completados y abandonados
                            7. Gestionar tus listas customizadas. (Piensa en ellas como listas tematicas de mangas que puedes compartir con otros)
                            8. Crear/gestionar/buscar grupos
                            9. Crear/editar obras.
                            10. Crear/editar/buscar autores
                            11. Subir/editar/borrar capitulos
                            12. Calificar una obra.
                            
                            Lo que no puedes hacer (de momento):
                            1. Usar los foros
                            2. Comentar en las obras/capitulos/etc.
                            3. Cualquier otra cosa que no este en la lista anterior.
                            
                            Si necesitas que se arregle algo, utiliza los botones de reporte situados en varios lugares del sitio, los moderadores se encargaran de ello. Si tienes problemas con tu cuenta, envia un correo a **support@mangadex__.org__** para obtener ayuda.""")

                    .send(event.getChannel());
        }
    }
}
