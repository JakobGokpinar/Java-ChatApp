package goksoft.chat.app.config;

public class Environment {

    // Toggle between dev and production
    private static final boolean IS_PRODUCTION = false; // Change to true for production

    // Backend URLs
    private static final String PROD_URL = "https://java-chatapp-production.up.railway.app/api";
    private static final String DEV_URL = "http://localhost:8080/api";

    // Timeouts
    public static final int CONNECT_TIMEOUT_SECONDS = 10;
    public static final int REQUEST_TIMEOUT_SECONDS = 30;

    // Polling intervals
    public static final int MESSAGE_POLL_INTERVAL_MS = 2000;      // 2 seconds
    public static final int FRIEND_REQUEST_POLL_INTERVAL_MS = 20000; // 20 seconds

    /**
     * Get the base API URL based on environment
     */
    public static String getBaseUrl() {
        return IS_PRODUCTION ? PROD_URL : DEV_URL;
    }

    /**
     * Get the API base URL without /api suffix (for direct resource access like photos)
     */
    public static String getServerUrl() {
        String baseUrl = IS_PRODUCTION
                ? "https://java-chatapp-production.up.railway.app"
                : "http://localhost:8080";
        return baseUrl;
    }

    /**
     * Check if running in production mode
     */
    public static boolean isProduction() {
        return IS_PRODUCTION;
    }
}