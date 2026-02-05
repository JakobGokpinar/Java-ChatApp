package goksoft.chat.app;

/**
 * Module-safe entry point for the application.
 * <p>
 * JavaFX requires the Application subclass to not be the main class
 * when running without the module system. This class delegates to {@link Main#main(String[])}.
 */
public class Launcher {

    public static void main(String[] args) {
        Main.main(args);
    }
}
