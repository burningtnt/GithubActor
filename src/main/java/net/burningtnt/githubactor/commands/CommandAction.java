package net.burningtnt.githubactor.commands;

import com.mojang.brigadier.context.CommandContext;
import net.burningtnt.githubactor.github.GithubAPI;
import net.burningtnt.githubactor.github.TokenHolder;
import net.burningtnt.githubactor.github.valid.GithubRepository;
import org.slf4j.Logger;
import snw.jkook.message.Message;
import snw.jkook.message.component.MarkdownComponent;

import java.io.IOException;

public final class CommandAction {
    private CommandAction() {
    }

    public static void updateIssueStatus(CommandContext<Message> context, Logger logger, GithubAPI.IssueStatus issueStatus) {
        String token = TokenHolder.get(context.getArgument("repository", String.class), context.getSource());
        if (token == null) {
            logger.error(String.format("No token found for %s.", context.getArgument("repository", String.class)));
            context.getSource().sendToSource(new MarkdownComponent(String.format("No token found for %s.", context.getArgument("repository", String.class))));
        }

        String[] repo = GithubRepository.parse(context.getArgument("repository", String.class));

        try {
            String r = GithubAPI.updateIssueStatus(repo[0], repo[1], context.getArgument("issueID", Integer.class), issueStatus, token);

            if (r != null) {
                throw new IOException(r);
            } else {
                context.getSource().sendToSource(new MarkdownComponent("Success."));
            }
        } catch (IOException e) {
            logger.error("Fail to close issues.", e);
            context.getSource().sendToSource(new MarkdownComponent("Fail to close issue."));
        }
    }
}
