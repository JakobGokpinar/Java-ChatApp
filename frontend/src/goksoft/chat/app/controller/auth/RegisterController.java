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

    @FXML private TextField usernameField;
    @FXML private PasswordField password1Field;
    @FXML private PasswordField password2Field;
    @FXML private TextField textField1;
    @FXML private TextField textField2;
    @FXML private CheckBox showPasswordsButton;

    private final ServiceManager serviceManager = ServiceManager.getInstance();

    public void changeSceneToLogin(ActionEvent event) {
        SceneUtil.switchScene(usernameField, "../../view/auth/login.fxml", "Login", getClass());
    }

    public void showPasswords() {
        String pass1 = password1Field.getText();
        String pass2 = password2Field.getText();

        if (showPasswordsButton.isSelected()) {
            password1Field.setVisible(false);
            textField1.setText(pass1);
            textField1.setVisible(true);
            password2Field.setVisible(false);
            textField2.setText(pass2);
            textField2.setVisible(true);
            return;
        }
        password1Field.setText(textField1.getText());
        password2Field.setText(textField2.getText());
        textField1.setVisible(false);
        password1Field.setVisible(true);
        textField2.setVisible(false);
        password2Field.setVisible(true);
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
        String password = password1Field.getText();

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
     * @return true if valid, false otherwise (shows error message)
     */
    private boolean validateInputs() {
        // Check if fields are empty
        if (usernameField.getText().isBlank() ||
                password1Field.getText().isBlank() ||
                password2Field.getText().isBlank()) {
            WarningWindowController.warningMessage("Please fill out all places!");
            return false;
        }

        // Check password length
        if (password1Field.getText().length() < 4 ||
                password2Field.getText().length() < 4) {
            WarningWindowController.warningMessage("Password must be at least 4 characters length!");
            return false;
        }

        // Check if passwords match
        if (!password1Field.getText().equals(password2Field.getText())) {
            WarningWindowController.warningMessage("Passwords are not matching!");
            return false;
        }

        return true;
    }
}