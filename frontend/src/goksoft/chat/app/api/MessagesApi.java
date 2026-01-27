package goksoft.chat.app.api;

import goksoft.chat.app.model.dto.ApiResponse;
import retrofit2.Call;
import retrofit2.http.*;

import java.util.List;
import java.util.Map;

/**
 * Messages API endpoints
 */
public interface MessagesApi {

    @GET("messages/{friendId}")
    Call<ApiResponse<List<Map<String, Object>>>> getMessages(
            @Header("Authorization") String token,
            @Path("friendId") Long friendId
    );

    @POST("messages/{friendId}")
    Call<ApiResponse<String>> sendMessage(
            @Header("Authorization") String token,
            @Path("friendId") Long friendId,
            @Body Map<String, String> message
    );
}