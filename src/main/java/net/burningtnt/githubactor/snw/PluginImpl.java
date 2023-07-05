package net.burningtnt.githubactor.snw;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.burningtnt.githubactor.commands.CommandHolder;
import net.burningtnt.githubactor.github.TokenHolder;
import snw.jkook.command.JKookCommand;
import snw.jkook.entity.User;
import snw.jkook.message.component.MarkdownComponent;
import snw.jkook.plugin.BasePlugin;

public class PluginImpl extends BasePlugin {
    @Override
    public void onEnable() {
        this.getLogger().info("Hello world");

        CommandHolder.register(this.getLogger());
        TokenHolder.read();

        new JKookCommand("github").setExecutor((sender, args, message) -> {
            if (sender instanceof User user && message != null) {
                if (message.getComponent() instanceof MarkdownComponent markdownComponent) {
                    try {
                        CommandHolder.execute(markdownComponent.toString(), message);
                    } catch (CommandSyntaxException e) {
                        this.getLogger().info("Invalid command.", e);
                        message.sendToSource(new MarkdownComponent("Invalid command."));
                    } catch (Throwable e) {
                        this.getLogger().info("Unknown exception.", e);
                        message.sendToSource(new MarkdownComponent("Unknown exception."));
                    }
                }
            } else {
                this.getLogger().info("This command is not available for console.");
            }
        }).register(this);
    }
}
