package goksoft.chat.app.api;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;

public class ApiClient {
    
    // Railway backend URL (update this!)
    private static final String BASE_URL = "https://java-chatapp-production.up.railway.app/api";
    
    private final HttpClient client;
    private String jwtToken; // Store JWT token
    
    public ApiClient() {
        this.client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }
    
    // Set JWT token after login
    public void setToken(String token) {
        this.jwtToken = token;
    }
    
    // GET request
    public CompletableFuture<String> get(String endpoint) {
        HttpRequest request = buildRequest(endpoint)
                .GET()
                .build();
        
        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body);
    }
    
    // POST request with JSON body
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
                .uri(URI.create(BASE_URL + endpoint))
                .timeout(Duration.ofSeconds(30));
        
        if (jwtToken != null && !jwtToken.isEmpty()) {
            builder.header("Authorization", "Bearer " + jwtToken);
        }
        
        return builder;
    }
}