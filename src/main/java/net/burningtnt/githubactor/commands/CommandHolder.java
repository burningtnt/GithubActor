package net.burningtnt.githubactor.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.burningtnt.githubactor.github.GithubAPI;
import net.burningtnt.githubactor.github.TokenHolder;
import org.slf4j.Logger;
import snw.jkook.message.Message;
import snw.jkook.message.component.MarkdownComponent;

public final class CommandHolder {
    private CommandHolder() {
    }

    private static final CommandDispatcher<Message> dispatcher = new CommandDispatcher<>();

    public static void register(Logger logger) {
        dispatcher.register(
                LiteralArgumentBuilder.<Message>literal("github").then(
                        LiteralArgumentBuilder.<Message>literal("do").then(
                                RequiredArgumentBuilder.<Message, String>argument("repository", StringArgumentType.string()).then(
                                        LiteralArgumentBuilder.<Message>literal("issues").then(
                                                RequiredArgumentBuilder.<Message, Integer>argument("issueID", IntegerArgumentType.integer(0)).then(
                                                        LiteralArgumentBuilder.<Message>literal("status").then(
                                                                LiteralArgumentBuilder.<Message>literal("close-true").executes(context -> {
                                                                    CommandAction.updateIssueStatus(context, logger, GithubAPI.IssueStatus.CLOSE_TRUE);
                                                                    return Command.SINGLE_SUCCESS;
                                                                })
                                                        ).then(
                                                                LiteralArgumentBuilder.<Message>literal("close-false").executes(context -> {
                                                                    CommandAction.updateIssueStatus(context, logger, GithubAPI.IssueStatus.CLOSE_FALSE);
                                                                    return Command.SINGLE_SUCCESS;
                                                                })
                                                        ).then(
                                                                LiteralArgumentBuilder.<Message>literal("reopen").executes(context -> {
                                                                    CommandAction.updateIssueStatus(context, logger, GithubAPI.IssueStatus.REOPEN);
                                                                    return Command.SINGLE_SUCCESS;
                                                                })
                                                        )
                                                )
                                        )
                                )
                        )
                ).then(
                        LiteralArgumentBuilder.<Message>literal("tokens").then(
                                RequiredArgumentBuilder.<Message, String>argument("repository", StringArgumentType.string()).then(
                                        LiteralArgumentBuilder.<Message>literal("put").then(
                                                LiteralArgumentBuilder.<Message>literal("close-issues").then(
                                                        RequiredArgumentBuilder.<Message, String>argument("token", StringArgumentType.string()).executes(context -> {
                                                            TokenHolder.put(context.getArgument("repository", String.class), context.getArgument("token", String.class));
                                                            context.getSource().sendToSource(new MarkdownComponent(String.format("Token for %s is added.", context.getArgument("repository", String.class))));

                                                            context.getSource().delete();
                                                            return Command.SINGLE_SUCCESS;
                                                        })
                                                )
                                        )
                                )
                        )
                )
        );
    }

    public static void execute(String command, Message message) throws CommandSyntaxException {
        dispatcher.execute(command.substring(1), message);
    }
}