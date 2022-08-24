package com.github.kouyoquotient.listeners;

import org.javacord.api.DiscordApi;
import org.javacord.api.entity.message.MessageFlag;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.interaction.SelectMenuChooseEvent;
import org.javacord.api.interaction.SelectMenuInteraction;
import org.javacord.api.listener.interaction.SelectMenuChooseListener;

import static com.github.kouyoquotient.Main.logger;
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

        switch (valueId) {
            case "one" -> {
                logger.info("Select Menu interaction detected, running instructions for one");

                /*
                 * Verify if the user already have the selected role,
                 * reply with a message notifying the user if true
                 */
                if (api.getRoleById(SCANLATOR_ROLE).orElseThrow().hasUser(getUser)) {
                    componentInteraction.createImmediateResponder()
                            .setContent("Ya has seleccionado este rol")
                            .setFlags(MessageFlag.EPHEMERAL).respond();
                    logger.info("Invoker had already selected the role, returning");
                    return;
                }

                componentInteraction
                        .respondLater(true)
                        .thenAcceptAsync(interactionOriginalResponseUpdater ->
                                api.getRoleById(LECTOR_ROLE).orElseThrow().removeUser(getUser)
                                .thenRun((Runnable) api.getRoleById(SCANLATOR_ROLE).orElseThrow().addUser(getUser)
                                        .thenAcceptAsync(replyUser -> {
                                            interactionOriginalResponseUpdater
                                                    .setContent("Has seleccionado el rol <@&" + SCANLATOR_ROLE + ">.")
                                                    .update();
                                            logger.info("Successfully applied roles for user with ID: " + getUser.getId());
                                        })));
            }

            case "two" -> {
                logger.info("Select Menu interaction detected, running instructions for two");

                if (api.getRoleById(LECTOR_ROLE).orElseThrow().hasUser(getUser)) {
                    componentInteraction.createImmediateResponder()
                            .setContent("Ya has seleccionado este rol")
                            .setFlags(MessageFlag.EPHEMERAL).respond();
                    logger.info("Invoker had already selected the role, returning");
                    return;
                }

                componentInteraction
                        .respondLater(true)
                        .thenAcceptAsync(interactionOriginalResponseUpdater ->
                                api.getRoleById(SCANLATOR_ROLE).orElseThrow().removeUser(getUser)
                                .thenRun((Runnable) api.getRoleById(LECTOR_ROLE).orElseThrow().addUser(getUser)
                                        .thenAcceptAsync(replyUser -> {
                                            interactionOriginalResponseUpdater
                                                    .setContent("Has seleccionado el rol <@&" + LECTOR_ROLE + ">.")
                                                    .update();
                                            logger.info("Successfully applied roles for user with ID: " + getUser.getId());
                                        })));
            }

            case "three" -> {
                logger.info("Select Menu interaction detected, running instructions for three");

                if (!api.getRoleById(SCANLATOR_ROLE).orElseThrow().hasUser(getUser) && !api.getRoleById(LECTOR_ROLE).orElseThrow().hasUser(getUser)) {
                    componentInteraction.createImmediateResponder()
                            .setContent("No has seleccionado ning\u00FAn rol")
                            .setFlags(MessageFlag.EPHEMERAL).respond();
                    logger.info("Invoker had already selected the role, returning");
                    return;
                }

                componentInteraction
                        .respondLater(true)
                        .thenAcceptAsync(interactionOriginalResponseUpdater ->
                                api.getRoleById(SCANLATOR_ROLE).orElseThrow().removeUser(getUser)
                                .thenRun((Runnable) api.getRoleById(LECTOR_ROLE).orElseThrow().removeUser(getUser)
                                        .thenAcceptAsync(replyUser -> {
                                            interactionOriginalResponseUpdater
                                                    .setContent("Has removido todos tus roles")
                                                    .update();
                                            logger.info("Successfully removed roles for user with ID: " + getUser.getId());
                                        })));
            }

            case "options" -> {
                logger.warn("Select Menu interaction detected, running instructions for fallback reply");

                componentInteraction
                        .createImmediateResponder()
                        .setContent("Default fallback reply message for customId options. You shouldn't be seeing this")
                        .setFlags(MessageFlag.EPHEMERAL)
                        .respond();
            }
        }
    }
}
