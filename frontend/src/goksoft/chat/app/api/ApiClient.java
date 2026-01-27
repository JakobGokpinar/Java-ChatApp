package goksoft.chat.app.api;

import goksoft.chat.app.config.Environment;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;

public class ApiClient {

    private final HttpClient client;
    private String jwtToken;

    public ApiClient() {
        this.client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(Environment.CONNECT_TIMEOUT_SECONDS))
                .build();
    }

    public void setToken(String token) {
        this.jwtToken = token;
    }

    public String getToken() {
        return jwtToken;
    }

    public boolean hasToken() {
        return jwtToken != null && !jwtToken.isEmpty();
    }

    public CompletableFuture<String> get(String endpoint) {
        HttpRequest request = buildRequest(endpoint)
                .GET()
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body);
    }

    public CompletableFuture<String> post(String endpoint, String jsonBody) {
        HttpRequest request = buildRequest(endpoint)
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .header("Content-Type", "application/json")
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body);
    }

    // Build request with JWT token if available
    private HttpRequest.Builder buildRequest(String endpoint) {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(Environment.getApiBaseUrl() + endpoint))
                .timeout(Duration.ofSeconds(Environment.REQUEST_TIMEOUT_SECONDS));

        if (hasToken()) {
            builder.header("Authorization", "Bearer " + jwtToken);
        }

        return builder;
    }

    public void clearToken() {
        this.jwtToken = null;
    }
}