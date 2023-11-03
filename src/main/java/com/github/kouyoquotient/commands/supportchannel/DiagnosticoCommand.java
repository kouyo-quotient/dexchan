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

public class DiagnosticoCommand implements MessageCreateListener, SlashCommandCreateListener {
    private static final Logger logger = LogManager.getRootLogger();

    AllowedMentions allowedMentions = new AllowedMentionsBuilder()
            .setMentionEveryoneAndHere(false)
            .setMentionUsers(true)
            .setMentionRoles(false)
            .build();

    String message = String.valueOf(
            new MessageBuilder()
                    .setAllowedMentions(allowedMentions)
                    .appendNewLine()
                    .append("""
                            **Diagn\u00F3stico de problemas**

                            \u00BFMangaDex est\u00E1 ca\u00EDdo? Primero mira <https://status.mangadex.org/> antes de preguntar.

                            Hay muchas causas posibles de tu problema, por lo que es importante ser espec\u00EDfico.

                            **0. Animaci\u00F3n de carga infinita (o p\u00E1gina en blanco)**
                            a. La cach\u00E9 de tu navegador es muy antigua.
                            b. Es culpa de una de tus extensiones.
                            c. Tu navegador es demasiado antiguo/est\u00E1 desactualizado.

                            Primero, prueba en modo inc\u00F3gnito.
                            Si se soluciona, es la cach\u00E9 del navegador o una de tus extensiones: borra la cach\u00E9 de tu navegador, luego intenta abrir el sitio, si sigue sin cargar, deshabilita las extensiones de tu navegador una por una hasta saber cu\u00E1l es la causante del problema.

                            Si lo anterior no funcion\u00F3, proporciona lo siguiente:
                            - Entra a <https://www.whatsmybrowser.org/> y comparte el enlace que te dan.
                            - Toma una captura de la consola de tu navegador (F12 -> Consola) si est\u00E1s en PC.

                            **1. Error de certificado (SSL_CERT...)**
                            Tu antivirus (normalmente McAfee) o tu router (los que tienen tecnolog\u00EDa TrendMicro) est\u00E1n marcando MangaDex como un sitio peligroso y est\u00E1n intentando quitarte el acceso a el.

                            Tienes dos opciones:
                            a. Poner en la lista blanca los dominios `https://mangadex.org/` y `https://*.mangadex.org` en la configuraci\u00F3n de tu antivirus/router.
                            b. No uses McAfee y/o desactiva la seguridad de tu router (que est\u00E9n bloqueando MangaDex demuestra que son basura).

                            **2. Dominio no encontrado (NXDOMAIN...)**
                            Lo m\u00E1s probable es que tu proveedor de Internet est\u00E9 intentando quitarte el acceso al sitio.
                            Ejecuta lo siguiente en el CMD o la Terminal de tu PC: `nslookup mangadex.org` y compara la IP con la de <https://dns.google/query?name=mangadex.org>

                            a. Cambia tus DNS (usa los de Google o Cloudflare).
                            b. Usa una VPN (Cloudflare Warp y ProtonVPN son excelentes y gratuitos).

                            **3. Tiempo de espera agotado / Conexi\u00F3n rechazada**
                            Ve al paso 2. Si no te funciona prueba con una VPN.
                            Pueden ser muchas cosas, comparte los resultados de los pasos anteriores aqu\u00ED.
                            """).getStringBuilder());

    @Override
    public void onMessageCreate(MessageCreateEvent event) {
        if (event.getMessageContent().equalsIgnoreCase("!diagnostico")) {
            logger.info("Received legacy command diagnostico");

            // Command is restricted to the support channel
            if (event.getChannel().getId() != SUPPORT_CHANNEL) {
                event.getMessageAuthor().getMessage().reply("No puedes usar ese comando aqu\u00ED, pero puedes probar usando el comando de barra diagonal. \n`/diagnostico`");
                logger.info("Invoker not in support channel, exiting");
                return;
            }

            event.getChannel().sendMessage(message);
        }
        return;
    }

    @Override
    public void onSlashCommandCreate(SlashCommandCreateEvent event) {
        if (event.getSlashCommandInteraction().getFullCommandName().equalsIgnoreCase("diagnostico")) {
            logger.info("Received SlashCommand diagnostico");

            // Send as ephemeral if not in support channel
            if (event.getInteraction().getChannel().orElseThrow().getId() != SUPPORT_CHANNEL) {
                logger.info("Running instructions for not-in-support-channel");
                event.getInteraction().createImmediateResponder().setContent(message).setFlags(EPHEMERAL).respond();
                return;
            }

            event.getInteraction().createImmediateResponder().setContent(message).respond();
        }
    }
}
