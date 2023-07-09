package net.burningtnt.githubactor.commands;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public final class HelpMessage {
    private record CommandArg(String name, String type, String desc, String example) {
    }

    private record SubCommand(String command, String desc) {
    }

    public static final Map<String, HelpMessage> cache = new HashMap<>();

    public static String get(String root, Function<HelpMessageBuilder, HelpMessageBuilder> defaultProvider) {
        if (!cache.containsKey(root)) {
            cache.put(root, defaultProvider.apply(new HelpMessageBuilder(root)).build());
        }

        return cache.get(root).getBuildText();
    }

    public final static class HelpMessageBuilder {
        private final HelpMessage instance;

        private HelpMessageBuilder(String root) {
            this.instance = new HelpMessage(root);
        }

        public HelpMessageBuilder addSubCommand(String subCommand, String desc) {
            this.instance.subCommands.add(new SubCommand(subCommand, desc));
            return this;
        }

        public HelpMessageBuilder addArg(String name, String type, String desc, String example) {
            this.instance.args.add(new CommandArg(name, type, desc, example));
            return this;
        }

        public HelpMessage build() {
            this.instance.buildText();
            return this.instance;
        }
    }

    private final String root;

    private final List<SubCommand> subCommands = new ArrayList<>();

    private final List<CommandArg> args = new ArrayList<>();

    private String text = null;

    private HelpMessage(String root) {
        this.root = root;
    }

    private void buildText() {
        StringBuilder builder = new StringBuilder();

        builder.append("Command tree: /");
        builder.append(this.root);
        builder.append(" ...\n");

        for (SubCommand subCommand : this.subCommands) {
            builder.append(" - ");
            builder.append(this.root);
            builder.append(' ');
            builder.append(subCommand.command);
            builder.append(": ");
            builder.append(subCommand.desc);
            builder.append('\n');
        }
        builder.append('\n');

        for (CommandArg arg : this.args) {
            builder.append("  <");
            builder.append(arg.name);
            builder.append(": ");
            builder.append(arg.type);
            builder.append(">: ");
            builder.append(arg.desc);
            builder.append(" e.g. ");
            builder.append(arg.example);
            builder.append('\n');
        }

        this.text = builder.substring(0, builder.length() - 1);
    }

    public String getBuildText() {
        return this.text;
    }
}
