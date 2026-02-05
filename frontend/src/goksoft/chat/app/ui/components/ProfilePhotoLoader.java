package goksoft.chat.app.ui.components;

import goksoft.chat.app.config.Environment;
import goksoft.chat.app.service.ServiceManager;
import javafx.scene.image.Image;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Utility class for loading and caching profile photos.

 * Features:
 * - Loads profile photos from backend API
 * - Caches loaded photos to reduce server requests
 * - Falls back to default user icon on error
 * - Thread-safe caching
 */
public class ProfilePhotoLoader {

    private static final Logger logger = LoggerFactory.getLogger(ProfilePhotoLoader.class);

    // Cache for loaded profile photos
    private static final Map<String, Image> photoCache = new ConcurrentHashMap<>();

    // Default user icon (loaded once)
    private static Image defaultUserIcon;

    // HTTP client for fetching photos
    private static final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(Environment.CONNECT_TIMEOUT_SECONDS))
            .build();

    // Private constructor - utility class
    private ProfilePhotoLoader() {
    }

    /**
     * Load profile photo for a user.
     * Returns cached photo if available, otherwise fetches from server.
     * Falls back to default user icon on error.
     *
     * @param username The username to load photo for
     * @return The profile photo Image, or default user icon if unavailable
     */
    public static Image loadPhoto(String username) {
        if (username == null || username.isBlank()) {
            return getDefaultUserIcon();
        }

        // Check cache first
        Image cachedPhoto = photoCache.get(username);
        if (cachedPhoto != null) {
            return cachedPhoto;
        }

        // Try to fetch from server
        try {
            Image photo = fetchPhotoFromServer(username);
            if (photo != null && !photo.isError()) {
                photoCache.put(username, photo);
                return photo;
            }
        } catch (Exception e) {
            logger.debug("Could not load photo for user '{}': {}", username, e.getMessage());
        }

        // Return default icon on failure
        return getDefaultUserIcon();
    }

    /**
     * Fetch profile photo from the backend server.
     *
     * @param username The username to fetch photo for
     * @return The Image, or null if fetch failed
     */
    private static Image fetchPhotoFromServer(String username) {
        try {
            String token = ServiceManager.getInstance().getApiClient().getToken();
            if (token == null || token.isEmpty()) {
                logger.debug("No auth token available for photo fetch");
                return null;
            }

            String photoUrl = Environment.getBaseUrl() + "/users/photo/" + username;

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(photoUrl))
                    .header("Authorization", "Bearer " + token)
                    .timeout(Duration.ofSeconds(10))
                    .GET()
                    .build();

            HttpResponse<InputStream> response = httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.ofInputStream()
            );

            if (response.statusCode() == 200) {
                try (InputStream inputStream = response.body()) {
                    Image photo = new Image(inputStream);
                    if (!photo.isError()) {
                        logger.debug("Successfully loaded photo for user '{}'", username);
                        return photo;
                    }
                }
            } else {
                logger.debug("Photo fetch returned status {} for user '{}'",
                        response.statusCode(), username);
            }
        } catch (Exception e) {
            logger.debug("Failed to fetch photo for user '{}': {}", username, e.getMessage());
        }

        return null;
    }

    /**
     * Get the default user icon.
     * Loaded once and cached.
     *
     * @return The default user icon Image
     */
    public static Image getDefaultUserIcon() {
        if (defaultUserIcon == null) {
            try {
                defaultUserIcon = new Image(Objects.requireNonNull(
                        ProfilePhotoLoader.class.getResourceAsStream(
                                "/goksoft/chat/app/resources/images/icons/user-icon.png"
                        )
                ));
            } catch (Exception e) {
                logger.error("Failed to load default user icon", e);
                // Create a placeholder 1x1 transparent image as last resort
                defaultUserIcon = new Image("data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg==");
            }
        }
        return defaultUserIcon;
    }
}