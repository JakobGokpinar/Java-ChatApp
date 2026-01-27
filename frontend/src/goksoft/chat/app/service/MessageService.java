package goksoft.chat.app.service;

public class MessageService {
    private final ApiClient apiClient;
    
    public CompletableFuture<List<MessageDto>> getMessages(String friend) {
        return apiClient.get("/messages?friend=" + friend)
            .thenApply(json -> JsonUtil.fromJson(
                json, 
                new TypeToken<ApiResponse<List<MessageDto>>>(){}
            ))
            .thenApply(ApiResponse::getData);
    }
    
    public CompletableFuture<Void> sendMessage(String receiver, String message) {
        SendMessageRequest request = new SendMessageRequest(receiver, message);
        String jsonBody = JsonUtil.toJson(request);
        return apiClient.post("/messages/send", jsonBody)
            .thenApply(json -> null);
    }
}
