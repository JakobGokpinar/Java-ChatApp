package goksoft.chat.app.controller.auth;

import goksoft.chat.app.controller.dialog.WarningWindowController;
import goksoft.chat.app.service.ServiceManager;
import goksoft.chat.app.util.SceneUtil;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RegisterController {

    private static final Logger logger = LoggerFactory.getLogger(RegisterController.class);
    private final ServiceManager serviceManager = ServiceManager.getInstance();
    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private TextField textField;
    @FXML
    private CheckBox showPasswordsButton;

    public void changeSceneToLogin(ActionEvent event) {
        SceneUtil.switchScene(usernameField, "../../view/auth/login.fxml", "Login", getClass());
    }

    public void showPasswords() {
        String pass = passwordField.getText();

        if (showPasswordsButton.isSelected()) {
            passwordField.setVisible(false);
            textField.setText(pass);
            textField.setVisible(true);
            return;
        }
        passwordField.setText(textField.getText());
        textField.setVisible(false);
        passwordField.setVisible(true);
    }

    public void registerButton(MouseEvent event) {
        if (showPasswordsButton.isSelected()) {
            showPasswordsButton.setSelected(false);
            showPasswords();
        }

        // Validation
        if (!validateInputs()) {
            return;
        }

        String username = usernameField.getText();
        String password = passwordField.getText();

        // Use AuthService
        serviceManager.getAuthService().register(username, password)
                .thenAccept(response -> {
                    Platform.runLater(() -> {
                        if (response.isSuccess()) {
                            WarningWindowController.warningMessage("Registration successful! Please login.");
                        } else {
                            String message = response.getMessage() != null ?
                                    response.getMessage() :
                                    "Registration failed. Username might be taken.";
                            WarningWindowController.warningMessage(message);
                        }
                    });
                })
                .exceptionally(ex -> {
                    Platform.runLater(() -> {
                        logger.error("Registration error", ex);
                        WarningWindowController.warningMessage("Connection error. Please check your internet connection.");
                    });
                    return null;
                });
    }

    /**
     * Validate all input fields
     *
     * @return true if valid, false otherwise (shows error message)
     */
    private boolean validateInputs() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (username.isBlank() || passwordField.getText().isBlank()) {
            WarningWindowController.warningMessage("Please fill out all places!");
            return false;
        }

        // Check password length
        if (password.length() < 6) {
            WarningWindowController.warningMessage("Password must be at least 6 characters length!");
            return false;
        }

        if (password.length() > 24) {
            WarningWindowController.warningMessage("Password must be maximum 24 characters!");
            return false;
        }

        if (!password.matches(".*[a-zA-Z].*")) {
            WarningWindowController.warningMessage("Password must contain at least one letter!");
            return false;
        }

        if (!password.matches(".*\\d.*")) {
            WarningWindowController.warningMessage("Password must contain at least one number!");
            return false;
        }

        return true;
    }
}