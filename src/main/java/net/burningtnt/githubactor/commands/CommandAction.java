package net.burningtnt.githubactor.commands;

import net.burningtnt.githubactor.github.GithubAPI;
import net.burningtnt.githubactor.github.TokenHolder;
import net.burningtnt.githubactor.github.verifier.GithubRepositoryVerifier;
import net.burningtnt.githubactor.github.verifier.GithubTokenVerifier;
import org.slf4j.Logger;
import snw.jkook.message.Message;
import snw.jkook.message.component.MarkdownComponent;

import java.io.IOException;

public final class CommandAction {
    private CommandAction() {
    }

    public static void updateIssueStatus(Message message, Logger logger, String repository, int issueID, GithubAPI.IssueStatus issueStatus) {
        String token = TokenHolder.get(repository, message);
        if (token == null) {
            logger.error(String.format("No token found for %s.", repository));
            message.sendToSource(new MarkdownComponent(String.format("No token found for %s.", repository)));
        }

        String[] repo = GithubRepositoryVerifier.parse(repository);

        try {
            String r = GithubAPI.updateIssueStatus(repo[0], repo[1], issueID, issueStatus, token);

            if (r != null) {
                throw new IOException(r);
            } else {
                message.sendToSource(new MarkdownComponent("Success."));
            }
        } catch (IOException e) {
            logger.error("Fail to close issues.", e);
            message.sendToSource(new MarkdownComponent("Fail to close issue."));
        }
    }

    public static void putToken(Message message, Logger logger, String repository, String token) {
        if (!GithubTokenVerifier.valid(token)) {
            logger.error(String.format("Invalid token %s for repository %s.", token, repository));
            message.sendToSource(new MarkdownComponent(String.format("Invalid token %s for repository %s.", token, repository)));
        } else {
            TokenHolder.put(repository, token, message);

            message.sendToSource(new MarkdownComponent(String.format("Github Token for %s is added.", repository)));
            message.delete();
        }
    }
}
