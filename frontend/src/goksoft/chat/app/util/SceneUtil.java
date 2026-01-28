package goksoft.chat.app.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Utility class for scene switching operations
 */
public class SceneUtil {

    private static final Logger logger = LoggerFactory.getLogger(SceneUtil.class);

    /**
     * Switch to a new scene in the same window
     * @param currentNode Any node from the current scene
     * @param fxmlPath Path to FXML file (e.g., "userinterfaces/MainPanel.fxml")
     * @param title Window title
     * @param contextClass Class to use for resource loading
     */
    public static void switchScene(Node currentNode, String fxmlPath, String title, Class<?> contextClass) {
        try {
            Parent root = FXMLLoader.load(contextClass.getResource(fxmlPath));
            Scene scene = new Scene(root);
            Stage window = (Stage) currentNode.getScene().getWindow();
            window.setScene(scene);
            window.setTitle(title);
        } catch (IOException e) {
            logger.error("Failed to load scene: {}", fxmlPath, e);
            throw new RuntimeException("Failed to load scene: " + fxmlPath, e);
        }
    }

    /**
     * Open a new window with a scene
     * @param fxmlPath Path to FXML file
     * @param title Window title
     * @param contextClass Class to use for resource loading
     * @param resizable Whether window is resizable
     * @param fullScreen Whether to open in fullscreen
     * @return The new Stage
     */
    public static Stage openNewWindow(String fxmlPath, String title, Class<?> contextClass,
                                      boolean resizable, boolean fullScreen) {
        try {
            Parent root = FXMLLoader.load(contextClass.getResource(fxmlPath));
            Scene scene = new Scene(root);
            Stage stage = new Stage();
            stage.setScene(scene);
            stage.setTitle(title);
            stage.setResizable(resizable);
            stage.setFullScreen(fullScreen);
            stage.show();
            return stage;
        } catch (IOException e) {
            logger.error("Failed to open new window: {}", fxmlPath, e);
            throw new RuntimeException("Failed to open new window: " + fxmlPath, e);
        }
    }

    /**
     * Close current window and open a new one
     * @param currentNode Any node from the current scene
     * @param fxmlPath Path to FXML file
     * @param title Window title
     * @param contextClass Class to use for resource loading
     * @param resizable Whether new window is resizable
     * @param fullScreen Whether to open in fullscreen
     * @return The new Stage
     */
    public static Stage closeAndOpenNew(Node currentNode, String fxmlPath, String title,
                                        Class<?> contextClass, boolean resizable, boolean fullScreen) {
        Stage currentWindow = (Stage) currentNode.getScene().getWindow();
        currentWindow.close();
        return openNewWindow(fxmlPath, title, contextClass, resizable, fullScreen);
    }
}