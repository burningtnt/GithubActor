package net.burningtnt.githubactor.github.verifier;

import java.util.regex.Pattern;

public final class GithubRepositoryVerifier {
    private GithubRepositoryVerifier() {
    }

    private static final Pattern filter = Pattern.compile("^\\w+$");

    public static boolean valid(String owner, String project) {
        return filter.matcher(owner).find() && filter.matcher(project).find();
    }

    public static String[] parse(String repository) {
        String[] strings = repository.split("/");
        if (strings.length != 2) {
            throw new RuntimeException("Invalid");
        }
        if (!valid(strings[0], strings[1])) {
            throw new RuntimeException("Invalid");
        }
        return strings;
    }
}
