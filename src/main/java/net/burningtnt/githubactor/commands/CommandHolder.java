package net.burningtnt.githubactor.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.burningtnt.githubactor.github.GithubAPI;
import org.slf4j.Logger;
import snw.jkook.message.Message;
import snw.jkook.message.component.MarkdownComponent;

import java.util.function.Consumer;

public final class CommandHolder {
    private CommandHolder() {
    }

    private static final CommandDispatcher<Message> dispatcher = new CommandDispatcher<>();

    private static final CommandBuilder<Message> commandBuilder = new CommandBuilder<>();

    private static final class CommandBuilder<T> {
        public LiteralArgumentBuilder<T> literal(String name) {
            return LiteralArgumentBuilder.literal(name);
        }

        public <A> RequiredArgumentBuilder<T, A> argument(String name, ArgumentType<A> argumentType) {
            return RequiredArgumentBuilder.argument(name, argumentType);
        }

        public Command<T> box(Consumer<CommandContext<T>> consumer) {
            return context -> {
                consumer.accept(context);
                return Command.SINGLE_SUCCESS;
            };
        }
    }

    public static void register(Logger logger) {
        dispatcher.register(
                commandBuilder.literal("github").then(
                        commandBuilder.literal("do").then(
                                commandBuilder.argument("repository", StringArgumentType.string()).then(
                                        commandBuilder.literal("issues").then(
                                                commandBuilder.argument("issueID", IntegerArgumentType.integer(0)).then(
                                                        commandBuilder.literal("status").then(
                                                                commandBuilder.literal("close-true").executes(commandBuilder.box(context ->
                                                                        CommandAction.updateIssueStatus(context.getSource(), logger, context.getArgument("repository", String.class), context.getArgument("issueID", Integer.class), GithubAPI.IssueStatus.CLOSE_TRUE)))
                                                        ).then(
                                                                commandBuilder.literal("close-false").executes(commandBuilder.box(context ->
                                                                        CommandAction.updateIssueStatus(context.getSource(), logger, context.getArgument("repository", String.class), context.getArgument("issueID", Integer.class), GithubAPI.IssueStatus.CLOSE_FALSE)))
                                                        ).then(
                                                                commandBuilder.literal("reopen").executes(commandBuilder.box(context ->
                                                                        CommandAction.updateIssueStatus(context.getSource(), logger, context.getArgument("repository", String.class), context.getArgument("issueID", Integer.class), GithubAPI.IssueStatus.REOPEN)))
                                                        )
                                                )
                                        )
                                )
                        )
                ).then(
                        commandBuilder.literal("tokens").then(
                                commandBuilder.argument("repository", StringArgumentType.string()).then(
                                        commandBuilder.literal("put").then(
                                                commandBuilder.argument("token", StringArgumentType.string()).executes(commandBuilder.box(context ->
                                                        CommandAction.putToken(context.getSource(), logger, context.getArgument("repository", String.class), context.getArgument("token", String.class))))
                                        )
                                )
                        )
                ).then(
                        commandBuilder.literal("help").then(
                                commandBuilder.literal("do").executes(
                                        commandBuilder.box(context -> context.getSource().sendToSource(new MarkdownComponent(
                                                HelpMessage.get("github do", helpMessageBuilder -> helpMessageBuilder
                                                        .addSubCommand("<repository> issues <issueID> status close-true", "Close an issue as completed.")
                                                        .addSubCommand("<repository> issues <issueID> status close-false", "Close an issue as not planned.")
                                                        .addSubCommand("<repository> issues <issueID> status reopen", "Reopen an issue.")
                                                        .addArg("repository", "String", "The path to the github repository.", "\"burningtnt/GithubActor\"")
                                                        .addArg("issueID", "Integer", "The ID of the issue.", "1")
                                                )
                                        ))))
                        ).then(
                                commandBuilder.literal("tokens").executes(
                                        commandBuilder.box(context -> context.getSource().sendToSource(new MarkdownComponent(
                                                HelpMessage.get("github tokens", helpMessageBuilder -> helpMessageBuilder
                                                        .addSubCommand("tokens <repository> put <token>", "Register a token for a repository.")
                                                        .addArg("repository", "String", "The path to the github repository.", "\"burningtnt/GithubActor\"")
                                                        .addArg("token", "String", "The token for the repository", "ghp_xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx")
                                                )
                                        ))))
                        ).executes(
                                commandBuilder.box(context -> context.getSource().sendToSource(new MarkdownComponent(
                                        HelpMessage.get("github help", helpMessageBuilder -> helpMessageBuilder
                                                .addSubCommand("do", "Display the help message for /github do ...")
                                                .addSubCommand("tokens", "Display the help message for /github tokens ...")
                                        )
                                ))))
                )
        );
    }

    public static void execute(String command, Message message) throws CommandSyntaxException {
        dispatcher.execute(command.substring(1), message);
    }
}
