package goksoft.chat.app;

import javafx.event.Event;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Legacy navigation utilities
 * TODO: Move these to a proper NavigationService
 */
public class Function {

    /**
     * Switch between login and register scenes
     */
    public static void switchBetweenRegisterAndLogin(Event event, String scene) {
        Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();
        if (scene.equals("login")) {
            window.setScene(GlobalVariables.loginScene);
        }
        if (scene.equals("register")) {
            window.setScene(GlobalVariables.registerScene);
        }
        window.setTitle(Character.toUpperCase(scene.charAt(0)) + scene.substring(1));
    }

    /**
     * Log off and return to login screen
     */
    public static void logOff(MouseEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(Function.class.getResource("userinterfaces/login.fxml"));
            Parent loginPanel = loader.load();
            Scene scene = new Scene(loginPanel);
            Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();
            window.close();
            Stage newWindow = new Stage();
            newWindow.setScene(scene);
            newWindow.setResizable(false);
            newWindow.setFullScreen(false);
            newWindow.setTitle("Login");
            newWindow.show();

            // Clear stored credentials
            GlobalVariables.setIsThread(false);
            File file = new File(System.getProperty("user.home") + "/settings.txt");
            if (file.exists()) {
                FileWriter writer = new FileWriter(file);
                writer.write("");
                writer.close();
            }
            newWindow.setOnCloseRequest(windowEvent -> System.exit(0));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Open contact panel
     */
    public static void contactUs(MouseEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(Function.class.getResource("userinterfaces/ContactPanel.fxml"));
            Parent contactPanel = loader.load();
            Scene scene = new Scene(contactPanel);
            Stage newWindow = new Stage();
            newWindow.setScene(scene);
            newWindow.setResizable(false);
            newWindow.setFullScreen(false);
            newWindow.setTitle("Contact");
            newWindow.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}