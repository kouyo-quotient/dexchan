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

public class DiagnosticoCommand implements MessageCreateListener, SlashCommandCreateListener {

    AllowedMentions allowedMentions = new AllowedMentionsBuilder()
            .setMentionEveryoneAndHere(false)
            .setMentionUsers(true)
            .setMentionRoles(false)
            .build();

    String messageFirst = String.valueOf(
            new MessageBuilder()
                    .setAllowedMentions(allowedMentions)
                    .appendNewLine()
                    .append("""
                            **__Tabla de diagn\u00F3stico sobre problemas de acceso al sitio__**
                                                        
                            \u00BFMangaDex est\u00E1 ca\u00EDdo o soy yo? Probablemente seas t\u00FA, pero comprueba <https://status.mangadex.org/> antes de preguntar.
                                                        
                            En caso de que solo te est\u00E9 afectando a ti, hay muchas causas posibles, por lo que es importante saber exactamente cu\u00E1l es la que te afecta. Por favor se espec\u00EDfico.
                                                        
                            **__0. Animaci\u00F3n de carga infinita en medio de la p\u00E1gina (o p\u00E1gina completamente en blanco)__**
                            Probablemente est\u00E9 ocurriendo una de las siguientes cosas:
                            **a.** La cach\u00E9 de tu navegador es muy antigua.
                            **b.** Es culpa de una de tus extensiones.
                            **c.** Tu navegador es demasiado antiguo/est\u00E1 desactualizado.
                            **d.** No puedes acceder al sitio de forma segura.
                                                        
                            Primero, prueba en modo incognito.
                            Si se soluciona, es la cach\u00E9 del navegador o una de tus extensiones: borra la cach\u00E9 de tu navegador, luego intenta abrir el sitio, si sigue sin cargar, prueba deshabilitando las extensiones de tu navegador una por una hasta que funcione la p\u00E1gina para saber cu\u00E1l es la causante del problema.
                                                        
                            Si lo anterior no funcion\u00F3, proporciona **todo** lo siguiente:
                            - Entra a <https://www.whatsmybrowser.org/> y comparte el enlace que te dan.
                            - Toma una captura de pantalla de la consola de tu navegador (F12 -> Consola) si est\u00E1s en un PC.
                            """).getStringBuilder());
    String messageFinal = String.valueOf(
            new MessageBuilder()
                    .setAllowedMentions(allowedMentions)
                    .append("""
                            **__1. Error de certificado (o SSL_CERT...)__**
                            Tu antivirus (normalmente McAfee) o tu router (a menudo los routers con tecnolog\u00EDa TrendMicro y los de Asus) est\u00E1n marcando MangaDex como un sitio peligroso y est\u00E1n intentando quitarte el acceso a el.
                                                        
                            Tienes dos opciones:
                            **a.** Poner en la lista blanca los dominios `https://mangadex.org/` y `https://*.mangadex.org` en la configuraci\u00F3n de tu antivirus/router.
                            **b.** No uses McAfee y/o desactiva la seguridad de tu router (que est\u00E9 bloqueando MangaDex demuestra que son basura).
                                                        
                            **__2. Dominio no encontrado (o NXDOMAIN, ...)__**
                            Lo m\u00E1s probable es que tu proveedor de internet est\u00E9 mintiendo sobre nuestra direcci\u00F3n IP.
                            Ejecuta el siguiente comando en el CMD o la Terminal de tu PC: `nslookup mangadex.org` y compara la IP con la de <https://dns.google/query?name=mangadex.org>. Si no coinciden, tu proveedor de internet est\u00E1 intentando quitarte el acceso al sitio.
                                                        
                            Tendr\u00E1s que:
                            **a.** Cambiar tus DNS (usa los de Google o Cloudflare).
                            **b.** Usar una VPN (Cloudflare Warp y ProtonVPN son excelentes y gratuitos).
                                                        
                            **__3. Tiempo de espera agotado / Conexi\u00F3n rechazada__**
                            Ve al paso 2. Si no te funciona prueba con una VPN (ver paso 2).
                            Pueden ser muchas cosas, comparte los resultados de los pasos anteriores aqu\u00ED.""").getStringBuilder());

    @Override
    public void onMessageCreate(MessageCreateEvent event) {
        if (event.getMessageContent().equalsIgnoreCase("!diagnostico")) {
            // Command is restricted to the support channel
            if (event.getChannel().getId() != SUPPORT_CHANNEL) {
                return;
            }

            logger.info("Received instruction for command diagnostico");

            event.getChannel().sendMessage(messageFirst);
            event.getChannel().sendMessage(messageFinal);
        }
    }

    @Override
    public void onSlashCommandCreate(SlashCommandCreateEvent event) {
        // Command is restricted to the support channel
        if (event.getInteraction().getChannel().orElseThrow().getId() != SUPPORT_CHANNEL) {
            return;
        }

        if (event.getSlashCommandInteraction().getFullCommandName().equalsIgnoreCase("diagnostico")) {
            event.getInteraction().createImmediateResponder().setContent(messageFirst).respond();
            event.getInteraction().createFollowupMessageBuilder().setContent(messageFinal).send();
        }
    }
}
