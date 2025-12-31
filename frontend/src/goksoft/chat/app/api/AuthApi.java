package goksoft.chat.app.api;

import goksoft.chat.app.model.dto.ApiResponse;
import goksoft.chat.app.model.dto.LoginRequest;
import goksoft.chat.app.model.dto.LoginResponse;
import goksoft.chat.app.model.dto.RegisterRequest;
import goksoft.chat.app.model.dto.User;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

/**
 * Authentication API endpoints
 */
public interface AuthApi {

    @POST("auth/login")
    Call<ApiResponse<LoginResponse>> login(@Body LoginRequest request);

    @POST("auth/register")
    Call<ApiResponse<User>> register(@Body RegisterRequest request);
}