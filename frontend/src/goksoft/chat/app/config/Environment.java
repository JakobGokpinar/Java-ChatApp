package goksoft.chat.app.config;

public class Environment {

    private static final boolean IS_PRODUCTION = false; // Change to true for production

    private static final String PROD_URL = "https://java-chatapp-production.up.railway.app/api";
    private static final String DEV_URL = "http://localhost:8080/api";

    public static String getApiBaseUrl() {
        return IS_PRODUCTION ? PROD_URL : DEV_URL;
    }

    public static boolean isProduction() {
        return IS_PRODUCTION;
    }

    // Timeouts
    public static final int CONNECT_TIMEOUT_SECONDS = 10;
    public static final int REQUEST_TIMEOUT_SECONDS = 30;

    // Polling intervals
    public static final int MESSAGE_POLL_INTERVAL_MS = 2000; // 2 seconds
    public static final int FRIEND_REQUEST_POLL_INTERVAL_MS = 20000; // 20 seconds

}
