package goksoft.chat.app.config;

/**
 * Environment configuration for dev/prod separation.
 * <p>
 * Usage:
 * - Development: Run with VM argument -Dapp.env=dev
 * - Production: Run without arguments (defaults to prod)
 * <p>
 * IntelliJ Setup:
 * 1. Run → Edit Configurations
 * 2. Add "Dev" configuration with VM options: -Dapp.env=dev
 * 3. Add "Prod" configuration with no VM options
 */
public class Environment {

    // ===== BACKEND URLS =====
    private static final String PROD_URL = "https://chatapp-api.jakobg.tech/api";
    private static final String DEV_URL = "http://localhost:8080/api";

    // ===== TIMEOUTS =====
    public static final int CONNECT_TIMEOUT_SECONDS = 10;
    public static final int REQUEST_TIMEOUT_SECONDS = 30;

    // ===== POLLING INTERVALS =====
    public static final int MESSAGE_POLL_INTERVAL_MS = 2000;      // 2 seconds
    public static final int FRIEND_REQUEST_POLL_INTERVAL_MS = 20000; // 20 seconds

    // ===== ENVIRONMENT DETECTION (resolved once at class load) =====
    private static final boolean IS_PRODUCTION;

    static {
        String env = System.getProperty("app.env", "prod");
        IS_PRODUCTION = env.equalsIgnoreCase("prod");

        if (IS_PRODUCTION) {
            System.out.println("== RUNNING IN PRODUCTION MODE ==");
            System.out.println("   Backend: " + PROD_URL);
        } else {
            System.out.println("== RUNNING IN DEVELOPMENT MODE ==");
            System.out.println("   Backend: " + DEV_URL);
        }
    }

    /**
     * Get the base API URL based on current environment.
     *
     * @return Base URL for API calls (includes /api suffix)
     */
    public static String getBaseUrl() {
        return IS_PRODUCTION ? PROD_URL : DEV_URL;
    }

    /**
     * Get the server URL without /api suffix (for direct resource access like photos).
     *
     * @return Server base URL
     */
    public static String getServerUrl() {
        return IS_PRODUCTION
                ? "https://chatapp-api.jakobg.tech/api"
                : "http://localhost:8080";
    }

}