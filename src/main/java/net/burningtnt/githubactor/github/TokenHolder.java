package net.burningtnt.githubactor.github;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import snw.jkook.message.Message;
import snw.jkook.message.PrivateMessage;
import snw.jkook.message.TextChannelMessage;

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

    public static void put(String repository, String token, Message message) {
        if (message instanceof TextChannelMessage textChannelMessage) {
            map.put(repository + "@channel-message:" + textChannelMessage.getChannel().getId(), token);
        } else if (message instanceof PrivateMessage) {
            map.put(repository + "@private-message:" + message.getSender().getId(), token);
        }

        CompletableFuture.runAsync(TokenHolder::saveToFile);
    }

    public static String get(String repository, Message message) {
        if (message instanceof TextChannelMessage textChannelMessage) {
            return map.get(repository + "@channel-message:" + textChannelMessage.getChannel().getId());
        } else if (message instanceof PrivateMessage) {
            return map.get(repository + "@private-message:" + message.getSender().getId());
        } else {
            return null;
        }
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
