package com.github.kouyoquotient.listeners;

import com.vdurmont.emoji.EmojiParser;
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
                 * Staff is able to manually apply roles to the users,
                 * so here we check if for some reason the user has both Lector and Scanlator role.
                 * If true we notify the user about it
                 */
                if(api.getRoleById(SCANLATOR_ROLE).orElseThrow().hasUser(getUser) && api.getRoleById(LECTOR_ROLE).orElseThrow().hasUser(getUser)){
                    componentInteraction.createImmediateResponder()
                            .setContent(EmojiParser.parseToUnicode(":warning: **\u00A1Tienes dos roles seleccionados!**\n Eso no deber\u00EDa ser posible, por favor selecciona la opci\u00F3n de remover tus roles o no podr\u00E1s seleccionar ninguna opci\u00F3n usando el men\u00FA."))
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
                            .setContent(EmojiParser.parseToUnicode(":warning: Ya has seleccionado este rol"))
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
                                            .setContent(EmojiParser.parseToUnicode(":white_check_mark: Has seleccionado el rol <@&" + SCANLATOR_ROLE + ">."))
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
                                                        .setContent(EmojiParser.parseToUnicode(":white_check_mark: Has seleccionado el rol <@&" + SCANLATOR_ROLE + ">."))
                                                        .update();
                                                logger.info("Successfully applied role Scanlator to user with ID: " + getUser.getId());
                                            }));
                        });
            }

            case "two" -> {
                /*
                 * This case has the same logic as the previous one
                 */
                logger.info("Select Menu interaction detected, running instructions for two");

                if(api.getRoleById(SCANLATOR_ROLE).orElseThrow().hasUser(getUser) && api.getRoleById(LECTOR_ROLE).orElseThrow().hasUser(getUser)){
                    componentInteraction.createImmediateResponder()
                            .setContent(EmojiParser.parseToUnicode(":warning: **\u00A1Tienes dos roles seleccionados!**\n Eso no deber\u00EDa ser posible, por favor selecciona la opci\u00F3n de remover tus roles o no podr\u00E1s seleccionar ninguna opci\u00F3n usando el men\u00FA."))
                            .setFlags(MessageFlag.EPHEMERAL).respond();
                    logger.info("Invoker had two roles selected, exiting");
                    return;
                }

                if (api.getRoleById(LECTOR_ROLE).orElseThrow().hasUser(getUser)) {
                    componentInteraction.createImmediateResponder()
                            .setContent(EmojiParser.parseToUnicode(":warning: Ya has seleccionado este rol"))
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
                                            .setContent(EmojiParser.parseToUnicode(":white_check_mark: Has seleccionado el rol <@&" + LECTOR_ROLE + ">."))
                                            .update();
                                    logger.info("Successfully applied role Lector to user with ID: " + getUser.getId());
                                });
                                return;
                            }

                            api.getRoleById(SCANLATOR_ROLE).orElseThrow().removeUser(getUser)
                                    .thenRun((Runnable) api.getRoleById(LECTOR_ROLE).orElseThrow().addUser(getUser)
                                            .thenAcceptAsync(replyUser -> {
                                                interactionOriginalResponseUpdater
                                                        .setContent(EmojiParser.parseToUnicode(":white_check_mark: Has seleccionado el rol <@&" + LECTOR_ROLE + ">."))
                                                        .update();
                                                logger.info("Successfully applied role Lector to user with ID: " + getUser.getId());
                                            }));
                        });
            }

            case "three" -> {
                logger.info("Select Menu interaction detected, running instructions for three");

                /*
                 * Here we check if the user doesn't have any role,
                 * reply with a message notifying the user if true
                 */
                if (!api.getRoleById(SCANLATOR_ROLE).orElseThrow().hasUser(getUser) && !api.getRoleById(LECTOR_ROLE).orElseThrow().hasUser(getUser)) {
                    componentInteraction.createImmediateResponder()
                            .setContent(EmojiParser.parseToUnicode(":warning: No has seleccionado ning\u00FAn rol"))
                            .setFlags(MessageFlag.EPHEMERAL).respond();
                    logger.info("Invoker had no roles, exiting");
                    return;
                }

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
                                            .setContent(EmojiParser.parseToUnicode(":no_entry: Has removido tu rol <@&" + SCANLATOR_ROLE + ">"))
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
                                            .setContent(EmojiParser.parseToUnicode(":no_entry: Has removido tu rol <@&" + LECTOR_ROLE + ">"))
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
                                                        .setContent(EmojiParser.parseToUnicode("Has removido tus roles <@&"+SCANLATOR_ROLE+"> y <@&"+LECTOR_ROLE+">\n\n :warning: **Esta acci\u00F3n no deber\u00EDa estar ejecut\u00E1ndose. Por favor notificalo mediante el canal de soporte.**"))
                                                        .update();
                                                logger.info("Successfully removed all roles for user with ID: " + getUser.getId());
                                            }));
                        });
            }

            case "options" -> {
                logger.warn("Select Menu interaction detected, running instructions for fallback reply");

                componentInteraction
                        .createImmediateResponder()
                        .setContent(EmojiParser.parseToUnicode("Respuesta default\n\n :warning: **Si est\u00E1s viendo esto por favor notificalo mediante el canal de soporte cuanto antes.**"))
                        .setFlags(MessageFlag.EPHEMERAL)
                        .respond();
            }
        }
    }
}
