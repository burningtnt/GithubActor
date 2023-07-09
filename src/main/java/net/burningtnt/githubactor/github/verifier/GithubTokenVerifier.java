package net.burningtnt.githubactor.github.verifier;

import java.util.regex.Pattern;

public final class GithubTokenVerifier {
    private GithubTokenVerifier() {
    }

    private static final Pattern filter = Pattern.compile("^ghp_[a-zA-z0-9]{36}$");

    public static boolean valid(String token) {
        return filter.matcher(token).find();
    }
}
