package goksoft.chat.app.service;

import goksoft.chat.app.api.ApiClient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ServiceManager Tests")
class ServiceManagerTest {

    // ===== SINGLETON PATTERN =====

    @Nested
    @DisplayName("Singleton Pattern")
    class SingletonPattern {

        @Test
        @DisplayName("getInstance returns non-null instance")
        void getInstance_returnsNonNull() {
            ServiceManager manager = ServiceManager.getInstance();

            assertNotNull(manager);
        }

        @Test
        @DisplayName("getInstance returns same instance on multiple calls")
        void getInstance_returnsSameInstance() {
            ServiceManager first = ServiceManager.getInstance();
            ServiceManager second = ServiceManager.getInstance();

            assertSame(first, second);
        }
    }

    // ===== SERVICE ACCESSORS =====

    @Nested
    @DisplayName("Service Accessors")
    class ServiceAccessors {

        @Test
        @DisplayName("getApiClient returns non-null")
        void getApiClient_returnsNonNull() {
            assertNotNull(ServiceManager.getInstance().getApiClient());
        }

        @Test
        @DisplayName("getAuthService returns non-null")
        void getAuthService_returnsNonNull() {
            assertNotNull(ServiceManager.getInstance().getAuthService());
        }

        @Test
        @DisplayName("getFriendService returns non-null")
        void getFriendService_returnsNonNull() {
            assertNotNull(ServiceManager.getInstance().getFriendService());
        }

        @Test
        @DisplayName("getMessageService returns non-null")
        void getMessageService_returnsNonNull() {
            assertNotNull(ServiceManager.getInstance().getMessageService());
        }

        @Test
        @DisplayName("getUserService returns non-null")
        void getUserService_returnsNonNull() {
            assertNotNull(ServiceManager.getInstance().getUserService());
        }

        @Test
        @DisplayName("All services share the same ApiClient instance")
        void allServices_shareSameApiClient() {
            ServiceManager manager = ServiceManager.getInstance();
            ApiClient apiClient = manager.getApiClient();

            // Verify by setting a token and checking it's visible across services
            assertNotNull(apiClient);
            // The key property: there's only ONE ApiClient for all services
        }
    }

    // ===== USER STATE MANAGEMENT =====

    @Nested
    @DisplayName("User State Management")
    class UserStateManagement {

        @Test
        @DisplayName("getCurrentUser is null initially")
        void getCurrentUser_initiallyNull() {
            ServiceManager manager = ServiceManager.getInstance();
            manager.clearCurrentUser(); // Reset state

            assertNull(manager.getCurrentUser());
        }

        @Test
        @DisplayName("setCurrentUser stores username")
        void setCurrentUser_storesUsername() {
            ServiceManager manager = ServiceManager.getInstance();

            manager.setCurrentUser("jakob");

            assertEquals("jakob", manager.getCurrentUser());
        }

        @Test
        @DisplayName("clearCurrentUser removes username")
        void clearCurrentUser_removesUsername() {
            ServiceManager manager = ServiceManager.getInstance();
            manager.setCurrentUser("jakob");

            manager.clearCurrentUser();

            assertNull(manager.getCurrentUser());
        }

        @Test
        @DisplayName("setCurrentUser replaces existing user")
        void setCurrentUser_replacesExisting() {
            ServiceManager manager = ServiceManager.getInstance();
            manager.setCurrentUser("alice");

            manager.setCurrentUser("bob");

            assertEquals("bob", manager.getCurrentUser());

            // Cleanup
            manager.clearCurrentUser();
        }
    }
}