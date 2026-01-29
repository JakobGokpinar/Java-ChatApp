package goksoft.chat.app.config;

/**
 * Environment configuration for dev/prod separation.
 *
 * Usage:
 * - Development: Run with VM argument -Dapp.env=dev
 * - Production: Run without arguments (defaults to prod)
 *
 * IntelliJ Setup:
 * 1. Run → Edit Configurations
 * 2. Add "Dev" configuration with VM options: -Dapp.env=dev
 * 3. Add "Prod" configuration with no VM options
 */
public class Environment {

    // ===== ENVIRONMENT DETECTION =====

    /**
     * Check if running in production mode.
     * Reads from system property 'app.env'.
     * If not set or equals 'prod', returns true (production).
     * If set to 'dev', returns false (development).
     *
     * @return true if production, false if development
     */
    public static boolean isProduction() {
        String env = System.getProperty("app.env", "prod");
        boolean isProd = env.equalsIgnoreCase("prod");

        // Log environment on startup
        if (!isProd) {
            System.out.println("== RUNNING IN DEVELOPMENT MODE ==");
            System.out.println("   Backend: " + DEV_URL);
        } else {
            System.out.println("== RUNNING IN PRODUCTION MODE ==");
            System.out.println("   Backend: " + PROD_URL);
        }

        return isProd;
    }

    // ===== BACKEND URLS =====

    private static final String PROD_URL = "https://java-chatapp-production.up.railway.app/api";
    private static final String DEV_URL = "http://localhost:8080/api";

    /**
     * Get the base API URL based on current environment
     * @return Base URL for API calls
     */
    public static String getBaseUrl() {
        return isProduction() ? PROD_URL : DEV_URL;
    }

    /**
     * Get the server URL without /api suffix (for direct resource access like photos)
     * @return Server base URL
     */
    public static String getServerUrl() {
        String baseUrl = isProduction()
                ? "https://java-chatapp-production.up.railway.app"
                : "http://localhost:8080";
        return baseUrl;
    }

    // ===== TIMEOUTS =====

    public static final int CONNECT_TIMEOUT_SECONDS = 10;
    public static final int REQUEST_TIMEOUT_SECONDS = 30;

    // ===== POLLING INTERVALS =====

    public static final int MESSAGE_POLL_INTERVAL_MS = 2000;      // 2 seconds
    public static final int FRIEND_REQUEST_POLL_INTERVAL_MS = 20000; // 20 seconds

    // ===== ENVIRONMENT INFO =====

    /**
     * Get current environment name
     * @return "development" or "production"
     */
    public static String getEnvironmentName() {
        return isProduction() ? "production" : "development";
    }

    /**
     * Print environment configuration (useful for debugging)
     */
    public static void printConfig() {
        System.out.println("========================================");
        System.out.println("  ENVIRONMENT CONFIGURATION");
        System.out.println("========================================");
        System.out.println("Environment: " + getEnvironmentName());
        System.out.println("Base URL: " + getBaseUrl());
        System.out.println("Server URL: " + getServerUrl());
        System.out.println("Connect Timeout: " + CONNECT_TIMEOUT_SECONDS + "s");
        System.out.println("Request Timeout: " + REQUEST_TIMEOUT_SECONDS + "s");
        System.out.println("========================================");
    }
}