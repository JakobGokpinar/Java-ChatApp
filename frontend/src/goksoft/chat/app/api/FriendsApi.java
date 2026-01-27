package goksoft.chat.app.api;

import goksoft.chat.app.model.dto.ApiResponse;
import goksoft.chat.app.model.dto.User;
import retrofit2.Call;
import retrofit2.http.*;

import java.util.List;

/**
 * Friends API endpoints
 */
public interface FriendsApi {

    @GET("friends")
    Call<ApiResponse<List<User>>> getFriends(@Header("Authorization") String token);

    @POST("friends/{userId}")
    Call<ApiResponse<String>> sendFriendRequest(
            @Header("Authorization") String token,
            @Path("userId") Long userId
    );

    @PUT("friends/accept/{userId}")
    Call<ApiResponse<String>> acceptFriendRequest(
            @Header("Authorization") String token,
            @Path("userId") Long userId
    );

    @DELETE("friends/{userId}")
    Call<ApiResponse<String>> removeFriend(
            @Header("Authorization") String token,
            @Path("userId") Long userId
    );
}