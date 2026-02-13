package goksoft.chat.app.controller.auth;

import goksoft.chat.app.controller.dialog.WarningWindowController;
import goksoft.chat.app.service.ServiceManager;
import goksoft.chat.app.util.SceneUtil;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RegisterController {

    private static final Logger logger = LoggerFactory.getLogger(RegisterController.class);

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField1;
    @FXML private PasswordField passwordField2;
    @FXML private TextField textField1;
    @FXML private TextField textField2;
    @FXML private CheckBox showPasswordsButton;

    private final ServiceManager serviceManager = ServiceManager.getInstance();

    public void changeSceneToLogin(MouseEvent event) {
        SceneUtil.switchScene(usernameField, "/goksoft/chat/app/view/auth/login.fxml", "Login", getClass());
    }

    public void showPasswords() {
        if (showPasswordsButton.isSelected()) {
            // Show passwords: copy from PasswordFields to TextFields
            textField1.setText(passwordField1.getText());
            textField1.setVisible(true);
            passwordField1.setVisible(false);

            textField2.setText(passwordField2.getText());
            textField2.setVisible(true);
            passwordField2.setVisible(false);
        } else {
            // Hide passwords: copy from TextFields back to PasswordFields
            passwordField1.setText(textField1.getText());
            passwordField1.setVisible(true);
            textField1.setVisible(false);

            passwordField2.setText(textField2.getText());
            passwordField2.setVisible(true);
            textField2.setVisible(false);
        }
    }

    public void registerButton(MouseEvent event) {
        // If passwords are shown in plain text, sync back to password fields first
        if (showPasswordsButton.isSelected()) {
            passwordField1.setText(textField1.getText());
            passwordField2.setText(textField2.getText());

            // Reset to hidden password view
            showPasswordsButton.setSelected(false);
            passwordField1.setVisible(true);
            textField1.setVisible(false);
            passwordField2.setVisible(true);
            textField2.setVisible(false);
        }

        // Validation
        if (!validateInputs()) {
            return;
        }

        String username = usernameField.getText();
        String password = passwordField1.getText();

        // Use AuthService
        serviceManager.getAuthService().register(username, password)
                .thenAccept(response -> Platform.runLater(() -> {
                    if (response.isSuccess()) {
                        WarningWindowController.warningMessage("Registration successful! Please login.");
                    } else {
                        String message = response.getMessage() != null ?
                                response.getMessage() :
                                "Registration failed. Username might be taken.";
                        WarningWindowController.warningMessage(message);
                    }
                }))
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
     * @return true if valid, false otherwise (shows error message)
     */
    private boolean validateInputs() {
        // Check if fields are empty
        if (usernameField.getText().isBlank() ||
                passwordField1.getText().isBlank() ||
                passwordField2.getText().isBlank()) {
            WarningWindowController.warningMessage("Please fill out all places!");
            return false;
        }

        // Check password length
        if (passwordField1.getText().length() < 4 ||
                passwordField2.getText().length() < 4) {
            WarningWindowController.warningMessage("Password must be at least 4 characters length!");
            return false;
        }

        // Check if passwords match
        if (!passwordField1.getText().equals(passwordField2.getText())) {
            WarningWindowController.warningMessage("Passwords are not matching!");
            return false;
        }

        return true;
    }
}