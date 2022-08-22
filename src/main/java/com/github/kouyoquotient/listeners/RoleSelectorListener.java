package com.github.kouyoquotient.listeners;

import org.javacord.api.DiscordApi;
import org.javacord.api.entity.message.MessageFlag;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.interaction.SelectMenuChooseEvent;
import org.javacord.api.interaction.SelectMenuInteraction;
import org.javacord.api.listener.interaction.SelectMenuChooseListener;

import static com.github.kouyoquotient.utils.Constants.LECTOR_ROLE;
import static com.github.kouyoquotient.utils.Constants.SCANLATOR_ROLE;

public class RoleSelectorListener implements SelectMenuChooseListener {

    @Override
    public void onSelectMenuChoose(SelectMenuChooseEvent event) {
        DiscordApi api = event.getApi();

        SelectMenuInteraction componentInteraction = (SelectMenuInteraction) event.getInteraction();
        SelectMenuInteraction menuInteraction = event.getSelectMenuInteraction();
        String valueId = menuInteraction.getChosenOptions().listIterator().next().getValue();
        User getUser = event.getInteraction().getUser();

        /*
         * TODO: Find a better way to replace user's roles, alas is there any?
         *  4 request per case isn't that much I guess.
         */

        switch (valueId) {
            case "one" -> {
                if (api.getRoleById(SCANLATOR_ROLE).orElseThrow().hasUser(getUser)) {
                    return;
                }

                componentInteraction
                        .respondLater(true)
                        .thenAcceptAsync(interactionOriginalResponseUpdater -> {
                            api.getRoleById(LECTOR_ROLE).orElseThrow().removeUser(getUser);
                            api.getRoleById(SCANLATOR_ROLE).orElseThrow().addUser(getUser);
                            interactionOriginalResponseUpdater.setContent("Has seleccionado el rol <@&" + SCANLATOR_ROLE + ">.").update();
                        });
            }
            case "two" -> {
                if (api.getRoleById(LECTOR_ROLE).orElseThrow().hasUser(getUser)) {
                    return;
                }

                componentInteraction
                        .respondLater(true)
                        .thenAcceptAsync(interactionOriginalResponseUpdater -> {
                            api.getRoleById(SCANLATOR_ROLE).orElseThrow().removeUser(getUser);
                            api.getRoleById(LECTOR_ROLE).orElseThrow().addUser(getUser);
                            interactionOriginalResponseUpdater.setContent("Has seleccionado el rol <@&" + LECTOR_ROLE + ">.").update();
                        });
            }
            case "three" -> {
                if (!api.getRoleById(SCANLATOR_ROLE).orElseThrow().hasUser(getUser) && !api.getRoleById(LECTOR_ROLE).orElseThrow().hasUser(getUser)) {
                    return;
                }

                componentInteraction
                        .respondLater(true)
                        .thenAcceptAsync(interactionOriginalResponseUpdater -> {
                            api.getRoleById(LECTOR_ROLE).orElseThrow().removeUser(getUser);
                            api.getRoleById(SCANLATOR_ROLE).orElseThrow().removeUser(getUser);
                            interactionOriginalResponseUpdater.setContent("Has removido todos tus roles.").update();
                        });
            }
            case "options" -> componentInteraction
                    .createImmediateResponder()
                    .setContent("Default fallback reply message for customId options. You shouldn't be seeing this")
                    .setFlags(MessageFlag.EPHEMERAL)
                    .respond();
        }
    }
}
