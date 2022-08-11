package com.github.kouyoquotient.commands.support;

import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.message.mention.AllowedMentions;
import org.javacord.api.entity.message.mention.AllowedMentionsBuilder;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.listener.message.MessageCreateListener;

import static com.github.kouyoquotient.Main.logger;

public class DiagnosticoCommand implements MessageCreateListener {

    @Override
    public void onMessageCreate(MessageCreateEvent event) {
        long supportChannel = 864252859296907294L;

        if (event.getChannel().getId() != supportChannel) {
            return;
        }

        if (event.getMessageContent().equalsIgnoreCase("!diagnostico")) {
            logger.info("Received diagnostico command instruction");
            long authorId = event.getMessageAuthor().getId();

            AllowedMentions allowedMentions = new AllowedMentionsBuilder()
                    .setMentionEveryoneAndHere(false)
                    .setMentionUsers(true)
                    .setMentionRoles(false)
                    .build();

            EmbedBuilder embed = new EmbedBuilder()
                    .setTitle("Tabla de diagn\u00F3stico sobre problemas de acceso al sitio")
                    .setDescription("""
                            \u00BFMangaDex est\u00E1 ca\u00EDdo o soy yo? Probablemente seas t\u00FA, pero comprueba https://status.mangadex.org/ antes de preguntar.
                                                        
                            En caso de que solo te est\u00E9 afectando a ti, hay muchas causas posibles, por lo que es importante saber exactamente cu\u00E1l es la que te afecta. Por favor se espec\u00EDfico.
                                                        
                            **0. Animaci\u00F3n de carga infinita en medio de la p\u00E1gina (o p\u00E1gina completamente en blanco)**
                            Probablemente est\u00E9 ocurriendo una de las siguientes cosas:
                            a. La cach\u00E9 de tu navegador es muy antigua.
                            b. Es culpa de una de tus extensiones.
                            c. Tu navegador es demasiado antiguo.
                            d. No puedes acceder al sitio de forma segura.
                                                        
                            Primero, prueba en modo incognito.
                            Si se soluciona, es la cach\u00E9 del navegador o una de tus extensiones: borra la cach\u00E9 de tu navegador, luego intenta abrir el sitio, si sigue sin cargar, prueba deshabilitando tus extensiones del navegador una por una hasta que funcione la p\u00E1gina para saber cu\u00E1l es.
                                                        
                            Si lo anterior no funcion\u00F3, proporciona lo siguiente:
                            - Entra a https://www.whatsmybrowser.org/ y comparte el enlace que te dan.
                            - Una captura de pantalla de la consola de tu navegador (F12 -> Consola) si est\u00E1s en un PC.
                            
                            **1. Error de certificado (o SSL_CERT...)**
                            Tu antivirus (normalmente McAfee) o tu router (a menudo en los routers TrendMicro/Asus) est\u00E1n marcando MangaDex como peligroso e intentando quitarte el acceso.
                            Tienes dos opciones:
                            a. Poner en la lista blanca https://mangadex.org/ y https://*.mangadex.org en la configuraci\u00F3n de tu antivirus/router
                            b. No uses McAfee y/o desactiva la seguridad de tu router (que bloquee MangaDex demuestra que es una basura)
                            
                            **2. Dominio no encontrado (o NXDOMAIN, ...)**
                            Lo m\u00E1s probable es que tu proveedor de internet est\u00E9 mintiendo sobre nuestra direcci\u00F3n IP.
                            Ejecuta el siguiente comando en el CMD o la Terminal de tu PC nslookup mangadex.org y compara la IP con la de https://dns.google/query?name=mangadex.org. Si no coinciden, tu proveedor de internet est\u00E1 mintiendo.
                                                        
                            Tendr\u00E1s que: cambiar tus DNS (usa los de Google o Cloudflare) o usar una VPN (Cloudflare Warp y ProtonVPN son excelentes y gratuitos).
                            
                            **3. Tiempo de espera agotado / Conexi\u00F3n rechazada**
                            Ve primero al paso 2. Si eso no se funciona, prueba con una VPN (ver paso 2).
                            Pueden ser muchas cosas, comparte los resultados de los pasos anteriores aqu\u00ED.""");
            new MessageBuilder()
                    .setAllowedMentions(allowedMentions)
                    .append("> <@" + authorId + ">")
                    .addEmbed(embed)
                    .send(event.getChannel());
        }
    }
}
