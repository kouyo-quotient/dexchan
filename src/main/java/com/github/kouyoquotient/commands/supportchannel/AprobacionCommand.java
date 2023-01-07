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

public class AprobacionCommand implements MessageCreateListener, SlashCommandCreateListener {
    AllowedMentions allowedMentions = new AllowedMentionsBuilder()
            .setMentionEveryoneAndHere(false)
            .setMentionUsers(true)
            .setMentionRoles(false)
            .build();

    String message = String.valueOf(new MessageBuilder()
            .setAllowedMentions(allowedMentions)
            .append("""
                    Para evitar el trolling:
                    - El primer cap\u00EDtulo que subas con tu cuenta (por idioma) estar\u00E1 sujeto a aprobaci\u00F3n. Las subidas sucesivas tras ser aprobado ser\u00E1n instant\u00E1neas.
                    - Publicar una ficha est\u00E1 sujeto a aprobaci\u00F3n.
                                        
                    La aprobaci\u00F3n suele tardar menos de 2 d\u00EDas. El tiempo exacto depende de lo ocupados que est\u00E9n los moderadores.
                    Si tu cap\u00EDtulo/ficha sigue sin ser visible despu\u00E9s de 2 d\u00EDas, env\u00EDa un mensaje en el canal <#864252859296907294> explicando el problema. Tambi\u00E9n puedes enviar DM a un moderador.
                    """).getStringBuilder());

    @Override
    public void onSlashCommandCreate(SlashCommandCreateEvent event) {
        if(event.getSlashCommandInteraction().getFullCommandName().equalsIgnoreCase("aprobacion")){
            logger.info("Received SlashCommand aprobacion");

            // Send as ephemeral if not in support channel
            if (event.getInteraction().getChannel().orElseThrow().getId() != SUPPORT_CHANNEL) {
                logger.info("Running instructions for not-in-support-channel");
                event.getInteraction().createImmediateResponder().setFlags(EPHEMERAL).setContent(message).respond();
                return;
            }

            event.getInteraction().createImmediateResponder().setContent(message).respond();
        }
    }

    @Override
    public void onMessageCreate(MessageCreateEvent event) {
        if (event.getMessageContent().equalsIgnoreCase("!aprobacion")) {
            logger.info("Received legacy command aprobacion");

            // Command is restricted to the support channel
            if (event.getChannel().getId() != SUPPORT_CHANNEL) {
                event.getMessageAuthor().getMessage().reply("No puedes usar ese comando aqu\u00ED, pero puedes probar usando el comando de barra diagonal. \n`/aprobacion`");
                logger.info("Invoker not in support channel, exiting");
                return;
            }

            event.getChannel().sendMessage(message);
        }
    }
}
