package net.burningtnt.githubactor.github;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class TokenHolder {
    private static final File TOKEN_ROOT = new File("").getAbsoluteFile();

    private static final File TOKEN_FILE = new File(TOKEN_ROOT, "tokens.json").getAbsoluteFile();

    private TokenHolder() {
    }

    private static final ConcurrentMap<String, String> map = new ConcurrentHashMap<>();

    public static void read() {
        if (TOKEN_FILE.exists()) {
            try (InputStreamReader inputStreamReader = new InputStreamReader(Files.newInputStream(TOKEN_FILE.toPath()))) {
                JsonObject root = new Gson().fromJson(inputStreamReader, JsonObject.class);
                root.asMap().forEach((key, value) -> map.put(key, value.getAsString()));
            } catch (IOException ignored) {
            }
        }
    }

    public static void put(String path, String token) {
        map.put(path, token);
        CompletableFuture.runAsync(TokenHolder::saveToFile);
    }

    public static String get(String path) {
        return map.get(path);
    }

    private static synchronized void saveToFile() {
        JsonObject root = new JsonObject();
        for (Map.Entry<String, String> entry : map.entrySet()) {
            root.add(entry.getKey(), new JsonPrimitive(entry.getValue()));
        }

        try {
            Path outputFile = Files.createTempFile(TOKEN_ROOT.toPath(), "tokens", ".json");
            try (PrintWriter printWriter = new PrintWriter(outputFile.toFile(), StandardCharsets.UTF_8)) {
                printWriter.write(new Gson().toJson(root));
            }

            Files.deleteIfExists(TOKEN_FILE.toPath());
            outputFile.toFile().renameTo(TOKEN_FILE);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
