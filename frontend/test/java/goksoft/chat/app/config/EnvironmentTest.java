package goksoft.chat.app.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Environment Tests")
class EnvironmentTest {

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
    }
}