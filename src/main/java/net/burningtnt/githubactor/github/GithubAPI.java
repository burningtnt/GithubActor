package net.burningtnt.githubactor.github;

import com.google.gson.Gson;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHeader;

import java.io.IOException;
import java.util.List;

public final class GithubAPI {
    private GithubAPI() {
    }

    public enum IssueStatus {
        OPEN, CLOSE_TRUE, CLOSE_FALSE, REOPEN
    }

    public static String updateIssueStatus(String owner, String repo, int issueID, IssueStatus status, String token) throws IOException {
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("owner", new JsonPrimitive(owner));
        jsonObject.add("repo", new JsonPrimitive(repo));
        jsonObject.add("issue_number", new JsonPrimitive(issueID));

        switch (status) {
            case OPEN -> {
                jsonObject.add("state", new JsonPrimitive("open"));
                jsonObject.add("state_reason", JsonNull.INSTANCE);
            }

            case CLOSE_TRUE -> {
                jsonObject.add("state", new JsonPrimitive("closed"));
                jsonObject.add("state_reason", new JsonPrimitive("completed"));
            }

            case CLOSE_FALSE -> {
                jsonObject.add("state", new JsonPrimitive("closed"));
                jsonObject.add("state_reason", new JsonPrimitive("not_planned"));
            }

            case REOPEN -> {
                jsonObject.add("state", new JsonPrimitive("open"));
                jsonObject.add("state_reason", new JsonPrimitive("reopened"));
            }
        }

        HttpPatch request = new HttpPatch(String.format("https://api.github.com/repos/%s/%s/issues/%d", owner, repo, issueID));
        request.setEntity(new StringEntity(new Gson().toJson(jsonObject)));

        HttpResponse response = HttpClientBuilder.create()
                .setDefaultHeaders(List.of(
                        new BasicHeader("Accept", "application/vnd.github+json"),
                        new BasicHeader("X-GitHub-Api-Version", "2022-11-28"),
                        new BasicHeader("Authorization", "Bearer " + token)
                )).build().execute(request);

        if (response.getStatusLine().getStatusCode() == 200) {
            return null;
        } else {
            return response.getStatusLine().getReasonPhrase();
        }
    }
}
