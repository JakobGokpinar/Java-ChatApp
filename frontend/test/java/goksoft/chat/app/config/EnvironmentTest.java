package goksoft.chat.app.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Environment Tests")
class EnvironmentTest {

    // ===== CONSTANTS =====

    @Nested
    @DisplayName("Configuration Constants")
    class ConfigurationConstants {

        @Test
        @DisplayName("Connect timeout is reasonable (5-30 seconds)")
        void connectTimeout_isReasonable() {
            assertTrue(Environment.CONNECT_TIMEOUT_SECONDS >= 5);
            assertTrue(Environment.CONNECT_TIMEOUT_SECONDS <= 30);
        }

        @Test
        @DisplayName("Request timeout is reasonable (10-60 seconds)")
        void requestTimeout_isReasonable() {
            assertTrue(Environment.REQUEST_TIMEOUT_SECONDS >= 10);
            assertTrue(Environment.REQUEST_TIMEOUT_SECONDS <= 60);
        }

        @Test
        @DisplayName("Message poll interval is at least 1 second")
        void messagePollInterval_isReasonable() {
            assertTrue(Environment.MESSAGE_POLL_INTERVAL_MS >= 1000);
        }

        @Test
        @DisplayName("Friend request poll interval is longer than message poll")
        void friendRequestPollInterval_longerThanMessagePoll() {
            assertTrue(Environment.FRIEND_REQUEST_POLL_INTERVAL_MS >
                    Environment.MESSAGE_POLL_INTERVAL_MS);
        }

        @Test
        @DisplayName("Request timeout is greater than connect timeout")
        void requestTimeout_greaterThanConnectTimeout() {
            assertTrue(Environment.REQUEST_TIMEOUT_SECONDS >
                    Environment.CONNECT_TIMEOUT_SECONDS);
        }
    }

    // ===== URL GENERATION =====

    @Nested
    @DisplayName("URL Generation")
    class UrlGeneration {

        @Test
        @DisplayName("getBaseUrl returns non-null URL")
        void getBaseUrl_returnsNonNull() {
            assertNotNull(Environment.getBaseUrl());
        }

        @Test
        @DisplayName("getBaseUrl returns URL ending with /api")
        void getBaseUrl_endsWithApi() {
            assertTrue(Environment.getBaseUrl().endsWith("/api"));
        }

        @Test
        @DisplayName("getBaseUrl returns URL starting with http")
        void getBaseUrl_startsWithHttp() {
            assertTrue(Environment.getBaseUrl().startsWith("http"));
        }

        @Test
        @DisplayName("getServerUrl returns non-null URL")
        void getServerUrl_returnsNonNull() {
            assertNotNull(Environment.getServerUrl());
        }

        @Test
        @DisplayName("getServerUrl does NOT end with /api")
        void getServerUrl_doesNotEndWithApi() {
            assertFalse(Environment.getServerUrl().endsWith("/api"));
        }

        @Test
        @DisplayName("getEnvironmentName returns valid name")
        void getEnvironmentName_returnsValidName() {
            String name = Environment.getEnvironmentName();
            assertTrue("development".equals(name) || "production".equals(name));
        }
    }

    // ===== ENVIRONMENT DETECTION =====

    @Nested
    @DisplayName("Environment Detection")
    class EnvironmentDetection {

        @Test
        @DisplayName("Dev mode URL points to localhost when app.env=dev")
        void devMode_urlPointsToLocalhost() {
            // Store original value
            String original = System.getProperty("app.env");

            try {
                System.setProperty("app.env", "dev");

                assertFalse(Environment.isProduction());
                assertTrue(Environment.getBaseUrl().contains("localhost"));
                assertEquals("development", Environment.getEnvironmentName());
            } finally {
                // Restore original value
                if (original != null) {
                    System.setProperty("app.env", original);
                } else {
                    System.clearProperty("app.env");
                }
            }
        }

        @Test
        @DisplayName("Prod mode URL points to Railway when app.env=prod")
        void prodMode_urlPointsToRailway() {
            // Store original value
            String original = System.getProperty("app.env");

            try {
                System.setProperty("app.env", "prod");

                assertTrue(Environment.isProduction());
                assertTrue(Environment.getBaseUrl().contains("railway"));
                assertEquals("production", Environment.getEnvironmentName());
            } finally {
                // Restore original value
                if (original != null) {
                    System.setProperty("app.env", original);
                } else {
                    System.clearProperty("app.env");
                }
            }
        }

        @Test
        @DisplayName("Default environment is production when no property set")
        void defaultEnv_isProduction() {
            // Store original value
            String original = System.getProperty("app.env");

            try {
                System.clearProperty("app.env");

                assertTrue(Environment.isProduction());
            } finally {
                // Restore original value
                if (original != null) {
                    System.setProperty("app.env", original);
                }
            }
        }
    }
}