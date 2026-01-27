package goksoft.chat.app.controller;

import com.google.gson.reflect.TypeToken;
import goksoft.chat.app.ErrorClass.SuccessResult;
import goksoft.chat.app.GlobalVariables;
import goksoft.chat.app.api.ApiClient;
import goksoft.chat.app.model.dto.ApiResponse;
import goksoft.chat.app.model.dto.LoginRequest;
import goksoft.chat.app.model.dto.LoginResponse;
import goksoft.chat.app.model.dto.Result;
import goksoft.chat.app.util.JsonUtil;
import javafx.application.Platform;

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
