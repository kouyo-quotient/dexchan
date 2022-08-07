package com.github.kouyoquotient.listeners;

import org.javacord.api.DiscordApi;
import org.javacord.api.entity.message.MessageFlag;
import org.javacord.api.event.interaction.MessageComponentCreateEvent;
import org.javacord.api.interaction.MessageComponentInteraction;
import org.javacord.api.interaction.SelectMenuInteraction;
import org.javacord.api.listener.interaction.MessageComponentCreateListener;

import java.util.Optional;

public class RoleSelectorListener implements MessageComponentCreateListener {

    @Override
    public void onComponentCreate(MessageComponentCreateEvent event) {
        DiscordApi api = event.getApi();

        api.addSelectMenuChooseListener(componentListener -> {
            /*
             * TODO: There should be a less confusing way to find the valueIDs of the SelectMenu options,
             *  as right now I only want this thing to work, I'm not changing it.
             *  I'm also not sure if this is any efficient, but I guess is not.
             */
            Optional<MessageComponentInteraction> componentInteraction = componentListener.getInteraction()
                    .asMessageComponentInteraction();
            SelectMenuInteraction menuInteraction = componentListener.getSelectMenuInteraction();
            String valueId = menuInteraction.getChosenOptions().get(
                            componentListener.getSelectMenuInteraction().getChosenOptions().size()-1)
                    .getValue();

            /*
             * TODO: Find a better way to replace user's roles,
             *  the current one sends too many requests to Discord's API.
             *  It works, but due to Javacord ratelimit measures, it might get really slow.
             */

            switch (valueId) {
                case "one" -> {
                    if(api.getRoleById("1003423707252928603").orElseThrow().hasUser(menuInteraction.getUser())){
                        return;
                    }

                    componentInteraction.orElseThrow()
                            .respondLater(true)
                            .thenAcceptAsync(interactionOriginalResponseUpdater -> {
                                api.getRoleById("1003423715964493954").orElseThrow().removeUser(menuInteraction.getUser()).thenAcceptAsync(roleChange -> {
                                    api.getRoleById("1003423707252928603").orElseThrow().addUser(menuInteraction.getUser()).join();
                                    interactionOriginalResponseUpdater.setContent("Seleccionado rol <@&1003423707252928603>.").update();
                                });
                            });
                }
                case "two" -> {
                    if(api.getRoleById("1003423715964493954").orElseThrow().hasUser(menuInteraction.getUser())){
                        return;
                    }

                    componentInteraction.orElseThrow()
                            .respondLater(true)
                            .thenAcceptAsync(interactionOriginalResponseUpdater -> {
                                api.getRoleById("1003423707252928603").orElseThrow().removeUser(menuInteraction.getUser()).thenAcceptAsync(roleChange ->{
                                    api.getRoleById("1003423715964493954").orElseThrow().addUser(menuInteraction.getUser());
                                    interactionOriginalResponseUpdater.setContent("Seleccionado rol <@&1003423715964493954>.").update();
                                });
                            });
                }
                case "three" -> {
                    if(!api.getRoleById("1003423715964493954").orElseThrow().hasUser(menuInteraction.getUser()) && !api.getRoleById("1003423707252928603").orElseThrow().hasUser(menuInteraction.getUser())){
                        return;
                    }

                    componentInteraction.orElseThrow()
                            .respondLater(true)
                            .thenAcceptAsync(interactionOriginalResponseUpdater -> {
                                api.getRoleById("1003423707252928603").orElseThrow().removeUser(menuInteraction.getUser()).thenAcceptAsync(roleChange ->{
                                    api.getRoleById("1003423715964493954").orElseThrow().removeUser(menuInteraction.getUser());
                                    interactionOriginalResponseUpdater.setContent("Se reiniciaron tus roles.").update();
                                });
                            });
                }
                case "options" -> componentInteraction.orElseThrow()
                        .createImmediateResponder()
                        .setContent("Default fallback reply message for customId options. You shouldn't be seeing this")
                        .setFlags(MessageFlag.EPHEMERAL)
                        .respond();
            }
        });
    }
}
