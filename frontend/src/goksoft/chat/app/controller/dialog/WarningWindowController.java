package goksoft.chat.app.controller.dialog;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Controller for the modal warning/info dialog.
 * Use {@link #warningMessage(String)} to show a message from anywhere in the app.
 */
public class WarningWindowController {

    @FXML
    private Label messageLabel;

    public static void warningMessage(String text) {
        try {
            FXMLLoader loader = new FXMLLoader(WarningWindowController.class.getResource("/goksoft/chat/app/view/dialog/warningWindow2.fxml"));
            Parent root = loader.load();
            WarningWindowController windowController = loader.getController();
            windowController.setLabelText(text);
            Scene scene = new Scene(root);
            Stage stage = new Stage();
            stage.setResizable(false);
            stage.setFullScreen(false);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setAlwaysOnTop(true);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setLabelText(String text) {
        messageLabel.setText(text);
    }

}
