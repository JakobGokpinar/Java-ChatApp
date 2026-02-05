package goksoft.chat.app.service;

import com.google.gson.reflect.TypeToken;
import goksoft.chat.app.model.dto.ApiResponse;
import goksoft.chat.app.model.dto.LoginRequest;
import goksoft.chat.app.model.dto.LoginResponse;
import goksoft.chat.app.model.dto.User;
import goksoft.chat.app.util.JsonUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("JsonUtil Tests")
class JsonUtilTest {

    // ===== SERIALIZATION (toJson) =====

    @Nested
    @DisplayName("Serialization")
    class Serialization {

        @Test
        @DisplayName("Serialize LoginRequest to JSON")
        void serializeLoginRequest() {
            LoginRequest request = new LoginRequest("jakob", "password123");

            String json = JsonUtil.toJson(request);

            assertTrue(json.contains("\"username\":\"jakob\""));
            assertTrue(json.contains("\"password\":\"password123\""));
        }

        @Test
        @DisplayName("Serialize object with null fields")
        void serializeWithNulls() {
            User user = new User(null, null);

            String json = JsonUtil.toJson(user);

            assertNotNull(json);
            // Gson excludes nulls by default (no serializeNulls)
        }

    }

    // ===== DESERIALIZATION (fromJson) =====

    @Nested
    @DisplayName("Deserialization")
    class Deserialization {

        @Test
        @DisplayName("Deserialize JSON to LoginRequest")
        void deserializeLoginRequest() {
            String json = """
                    { "username": "jakob", "password": "pass123" }
                    """;

            LoginRequest request = JsonUtil.fromJson(json, LoginRequest.class);

            assertEquals("jakob", request.getUsername());
            assertEquals("pass123", request.getPassword());
        }

        @Test
        @DisplayName("Deserialize JSON to User")
        void deserializeUser() {
            String json = """
                    { "id": 42, "username": "alice" }
                    """;

            User user = JsonUtil.fromJson(json, User.class);

            assertEquals(42L, user.getId());
            assertEquals("alice", user.getUsername());
        }

        @Test
        @DisplayName("Deserialize ApiResponse with generic type")
        void deserializeApiResponseGeneric() {
            String json = """
                    {
                        "success": true,
                        "message": "Login successful",
                        "data": {
                            "token": "jwt-abc",
                            "user": { "id": 1, "username": "jakob" }
                        }
                    }
                    """;

            ApiResponse<LoginResponse> response = JsonUtil.fromJson(
                    json, new TypeToken<ApiResponse<LoginResponse>>() {}
            );

            assertTrue(response.isSuccess());
            assertEquals("Login successful", response.getMessage());
            assertNotNull(response.getData());
            assertEquals("jwt-abc", response.getData().getToken());
            assertEquals("jakob", response.getData().getUser().getUsername());
        }

        @Test
        @DisplayName("Deserialize list of strings with TypeToken")
        void deserializeStringList() {
            String json = """
                    ["alice", "bob", "charlie"]
                    """;

            List<String> names = JsonUtil.fromJson(json, new TypeToken<List<String>>() {});

            assertEquals(3, names.size());
            assertEquals("alice", names.get(0));
            assertEquals("bob", names.get(1));
            assertEquals("charlie", names.get(2));
        }

        @Test
        @DisplayName("Deserialize nested list (message format)")
        void deserializeNestedList() {
            String json = """
                    [
                        ["alice", "Hello!"],
                        ["bob", "Hi there"]
                    ]
                    """;

            List<List<String>> messages = JsonUtil.fromJson(
                    json, new TypeToken<List<List<String>>>() {}
            );

            assertEquals(2, messages.size());
            assertEquals("alice", messages.get(0).get(0));
            assertEquals("Hello!", messages.get(0).get(1));
        }

        @Test
        @DisplayName("Deserialize handles missing fields gracefully")
        void deserializeMissingFields() {
            String json = """
                    { "username": "alice" }
                    """;

            User user = JsonUtil.fromJson(json, User.class);

            assertEquals("alice", user.getUsername());
            assertNull(user.getId()); // Missing field defaults to null
        }

        @Test
        @DisplayName("Deserialize handles extra fields gracefully")
        void deserializeExtraFields() {
            String json = """
                    { "id": 1, "username": "alice", "unknownField": "ignored" }
                    """;

            // Gson ignores unknown fields by default
            User user = JsonUtil.fromJson(json, User.class);

            assertEquals(1L, user.getId());
            assertEquals("alice", user.getUsername());
        }

        @Test
        @DisplayName("Deserialize failed ApiResponse")
        void deserializeFailedResponse() {
            String json = """
                    {
                        "success": false,
                        "message": "Invalid credentials",
                        "data": null
                    }
                    """;

            ApiResponse<LoginResponse> response = JsonUtil.fromJson(
                    json, new TypeToken<ApiResponse<LoginResponse>>() {}
            );

            assertFalse(response.isSuccess());
            assertEquals("Invalid credentials", response.getMessage());
            assertNull(response.getData());
        }
    }

    // ===== ROUND-TRIP =====

    @Nested
    @DisplayName("Round-trip (serialize → deserialize)")
    class RoundTrip {

        @Test
        @DisplayName("LoginRequest survives round-trip")
        void roundTripLoginRequest() {
            LoginRequest original = new LoginRequest("jakob", "secret");

            String json = JsonUtil.toJson(original);
            LoginRequest restored = JsonUtil.fromJson(json, LoginRequest.class);

            assertEquals(original.getUsername(), restored.getUsername());
            assertEquals(original.getPassword(), restored.getPassword());
        }

        @Test
        @DisplayName("User survives round-trip")
        void roundTripUser() {
            User original = new User(7L, "testuser");

            String json = JsonUtil.toJson(original);
            User restored = JsonUtil.fromJson(json, User.class);

            assertEquals(original.getId(), restored.getId());
            assertEquals(original.getUsername(), restored.getUsername());
        }
    }
}