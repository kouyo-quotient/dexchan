package com.github.kouyoquotient.listeners;

import com.vdurmont.emoji.EmojiParser;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.message.MessageFlag;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.interaction.SelectMenuChooseEvent;
import org.javacord.api.interaction.SelectMenuInteraction;
import org.javacord.api.listener.interaction.SelectMenuChooseListener;

import static com.github.kouyoquotient.Main.logger;
import static com.github.kouyoquotient.utils.Constants.*;

public class RoleSelectorListener implements SelectMenuChooseListener {

    @Override
    public void onSelectMenuChoose(SelectMenuChooseEvent event) {
        DiscordApi api = event.getApi();

        SelectMenuInteraction componentInteraction = (SelectMenuInteraction) event.getInteraction();
        String valueId = componentInteraction.getChosenOptions().listIterator().next().getValue();
        User getUser = event.getInteraction().getUser();

        switch (valueId) {
            case "scanlator" -> {
                logger.info("SelectMenu interaction detected, running instructions for one");

                /*
                 * Staff is able to manually apply roles to the users,
                 * so here we check if for some reason the user has both Lector and Scanlator role.
                 * If true we notify the user about it
                 */
                if (api.getRoleById(SCANLATOR_ROLE).orElseThrow().hasUser(getUser) && api.getRoleById(LECTOR_ROLE).orElseThrow().hasUser(getUser)) {
                    componentInteraction.createImmediateResponder()
                            .setContent(EmojiParser.parseToUnicode("<:selwarning:1061074946526289982> **\u00A1Tu perfil tiene dos roles establecidos!** <:selwarning:1061074946526289982> \nNo podr\u00E1s usar este men\u00FA si tienes dos roles seleccionados, por favor haz click la opci\u00F3n \"Neutral\", \u00E9sta opci\u00F3n remover\u00E1 tus roles."))
                            .setFlags(MessageFlag.EPHEMERAL).respond();
                    logger.info("Invoker had two roles selected, exiting");
                    return;
                }

                /*
                 * Verify if the user already have the selected the Scanlator role,
                 * reply with a message notifying the user if true
                 */
                if (api.getRoleById(SCANLATOR_ROLE).orElseThrow().hasUser(getUser)) {
                    componentInteraction.createImmediateResponder()
                            .setContent(EmojiParser.parseToUnicode("<:selwarning:1061074946526289982> \u00A1Ya has seleccionado este rol! <:selwarning:1061074946526289982>"))
                            .setFlags(MessageFlag.EPHEMERAL).respond();
                    logger.info("Invoker had already selected the role, exiting");
                    return;
                }

                componentInteraction
                        .respondLater(true)
                        .thenAcceptAsync(interactionOriginalResponseUpdater -> {

                            /*
                             * Check if the user doesn't have any role to prevent
                             * sending unnecessary requests to Discord,
                             * if true only apply the scanlator role
                             */
                            if (!api.getRoleById(SCANLATOR_ROLE).orElseThrow().hasUser(getUser) && !api.getRoleById(LECTOR_ROLE).orElseThrow().hasUser(getUser)) {
                                api.getRoleById(SCANLATOR_ROLE).orElseThrow().addUser(getUser).thenAcceptAsync(replyUser -> {
                                    interactionOriginalResponseUpdater
                                            .setContent(EmojiParser.parseToUnicode("<:success:1061077709419204718> Roles establecidos correctamente.\n\u2022 [<@&"+SCANLATOR_ROLE+">]"))
                                            .update();
                                    logger.info("Successfully applied role Scanlator to user with ID: " + getUser.getId());
                                });
                                return;
                            }

                            /*
                             * If the previous checks returned false,
                             * then that means the user only has the Lector role,
                             * so we will remove it and then apply the Scanlator role
                             */
                            api.getRoleById(LECTOR_ROLE).orElseThrow().removeUser(getUser)
                                    .thenRun((Runnable) api.getRoleById(SCANLATOR_ROLE).orElseThrow().addUser(getUser)
                                            .thenAcceptAsync(replyUser -> {
                                                interactionOriginalResponseUpdater
                                                        .setContent(EmojiParser.parseToUnicode("<:success:1061077709419204718> Roles establecidos correctamente.\n\u2022 [<@"+SCANLATOR_ROLE+">]"))
                                                        .update();
                                                logger.info("Successfully applied role Scanlator to user with ID: " + getUser.getId());
                                            }));
                        });
            }

            case "lector" -> {
                /*
                 * This case has the same logic as the previous one
                 */
                logger.info("Select Menu interaction detected, running instructions for two");

                if (api.getRoleById(SCANLATOR_ROLE).orElseThrow().hasUser(getUser) && api.getRoleById(LECTOR_ROLE).orElseThrow().hasUser(getUser)) {
                    componentInteraction.createImmediateResponder()
                            .setContent(EmojiParser.parseToUnicode("<:selwarning:1061074946526289982> **\u00A1Tu perfil tiene dos roles establecidos!** <:selwarning:1061074946526289982> \nNo podr\u00E1s usar este men\u00FA si tienes dos roles seleccionados, por favor haz click la opci\u00F3n \"Neutral\", \u00E9sta opci\u00F3n remover\u00E1 tus roles."))
                            .setFlags(MessageFlag.EPHEMERAL).respond();
                    logger.info("Invoker had two roles selected, exiting");
                    return;
                }

                if (api.getRoleById(LECTOR_ROLE).orElseThrow().hasUser(getUser)) {
                    componentInteraction.createImmediateResponder()
                            .setContent(EmojiParser.parseToUnicode("<:selwarning:1061074946526289982> \u00A1Ya has seleccionado este rol! <:selwarning:1061074946526289982>"))
                            .setFlags(MessageFlag.EPHEMERAL).respond();
                    logger.info("Invoker had already selected the role, exiting");
                    return;
                }

                componentInteraction
                        .respondLater(true)
                        .thenAcceptAsync(interactionOriginalResponseUpdater -> {

                            if (!api.getRoleById(SCANLATOR_ROLE).orElseThrow().hasUser(getUser) && !api.getRoleById(LECTOR_ROLE).orElseThrow().hasUser(getUser)) {
                                api.getRoleById(LECTOR_ROLE).orElseThrow().addUser(getUser).thenAcceptAsync(replyUser -> {
                                    interactionOriginalResponseUpdater
                                            .setContent(EmojiParser.parseToUnicode("<:success:1061077709419204718> Roles establecidos correctamente.\n\u2022 [<@&"+LECTOR_ROLE+">]"))
                                            .update();
                                    logger.info("Successfully applied role Lector to user with ID: " + getUser.getId());
                                });
                                return;
                            }

                            api.getRoleById(SCANLATOR_ROLE).orElseThrow().removeUser(getUser)
                                    .thenRun((Runnable) api.getRoleById(LECTOR_ROLE).orElseThrow().addUser(getUser)
                                            .thenAcceptAsync(replyUser -> {
                                                interactionOriginalResponseUpdater
                                                        .setContent(EmojiParser.parseToUnicode("<:success:1061077709419204718> Roles establecidos correctamente.\n\u2022 [<@&"+LECTOR_ROLE+">]"))
                                                        .update();
                                                logger.info("Successfully applied role Lector to user with ID: " + getUser.getId());
                                            }));
                        });
            }

            case "neutral" -> {
                logger.info("Select Menu interaction detected, running instructions for four");

                componentInteraction
                        .respondLater(true)
                        .thenAcceptAsync(interactionOriginalResponseUpdater -> {
                            /*
                             * Check which of the roles the user has to avoid unnecessary requests to Discord
                             * this one checks if the user has the Scanlator role,
                             * if true then we remove it
                             */
                            if (api.getRoleById(SCANLATOR_ROLE).orElseThrow().hasUser(getUser) && !api.getRoleById(LECTOR_ROLE).orElseThrow().hasUser(getUser)) {
                                api.getRoleById(SCANLATOR_ROLE).orElseThrow().removeUser(getUser).thenAcceptAsync(replyUser -> {
                                    interactionOriginalResponseUpdater
                                            .setContent(EmojiParser.parseToUnicode("<:remove:1061080329051447409> Roles removidos correctamente. \n\u2022 [<@&"+SCANLATOR_ROLE+">]"))
                                            .update();
                                    logger.info("Successfully removed role Scanlator from user with ID: " + getUser.getId());
                                });
                                return;
                            }
                            /*
                             * Same as above, but in here we check if the user has the Lector role,
                             * if true then we remove it
                             */
                            if (api.getRoleById(LECTOR_ROLE).orElseThrow().hasUser(getUser) && !api.getRoleById(SCANLATOR_ROLE).orElseThrow().hasUser(getUser)) {
                                api.getRoleById(LECTOR_ROLE).orElseThrow().removeUser(getUser).thenAcceptAsync(replyUser -> {
                                    interactionOriginalResponseUpdater
                                            .setContent(EmojiParser.parseToUnicode("<:remove:1061080329051447409> Roles removidos correctamente. \n\u2022 [<@&"+LECTOR_ROLE+">]"))
                                            .update();
                                    logger.info("Successfully removed role Lector from user with ID: " + getUser.getId());
                                });
                                return;
                            }
                            /*
                             * Fallback call in case the previous checks (for some reason) returns false,
                             * in here we will remove both roles from the user, but we will log the action
                             * as a warning since this call shouldn't be executing.
                             */
                            logger.warn("Called fallback logic for removing all roles from the invoker");
                            api.getRoleById(SCANLATOR_ROLE).orElseThrow().removeUser(getUser)
                                    .thenRun((Runnable) api.getRoleById(LECTOR_ROLE).orElseThrow().removeUser(getUser)
                                            .thenAcceptAsync(replyUser -> {
                                                interactionOriginalResponseUpdater
                                                        .setContent(EmojiParser.parseToUnicode("<:remove:1061080329051447409> Roles removidos correctamente. \n\u2022 [<@&"+SCANLATOR_ROLE+">] \n\u2022 [<@&"+LECTOR_ROLE+">]"))
                                                        .update();
                                                logger.info("Successfully removed all roles for user with ID: " + getUser.getId());
                                            }));
                        });
            }
        }
    }
}
