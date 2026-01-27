package goksoft.chat.app.controller;

public class LoginController {
    
    private final ApiClient apiClient = new ApiClient();
    
    public Result signIn() {
        String username = usernameField.getText();
        String password = passwordField.getText();
        
        LoginRequest loginRequest = new LoginRequest(username, password);
        String jsonBody = JsonUtil.toJson(loginRequest);
        
        apiClient.post("/auth/login", jsonBody)
            .thenAccept(responseJson -> {
                ApiResponse<LoginResponse> response = JsonUtil.fromJson(
                    responseJson, 
                    new TypeToken<ApiResponse<LoginResponse>>(){}
                );
                
                if (response.isSuccess()) {
                    String token = response.getData().getToken();
                    apiClient.setToken(token);
                    GlobalVariables.setLoggedUser(username);
                    
                    Platform.runLater(() -> loadMainPanel());
                } else {
                    Platform.runLater(() -> 
                        showError(response.getMessage())
                    );
                }
            })
            .exceptionally(ex -> {
                Platform.runLater(() -> 
                    showError("Connection failed: " + ex.getMessage())
                );
                return null;
            });
        
        return new SuccessResult();
    }
}
