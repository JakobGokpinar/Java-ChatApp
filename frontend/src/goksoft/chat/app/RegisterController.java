package goksoft.chat.app;

import goksoft.chat.app.ErrorClass.ErrorResult;
import goksoft.chat.app.ErrorClass.Result;
import goksoft.chat.app.ErrorClass.SuccessResult;
import goksoft.chat.app.service.ServiceManager;
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
        Function.switchBetweenRegisterAndLogin(event, "login");
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

    public Result registerButton(MouseEvent event) {
        if (showPasswordsButton.isSelected()) {
            showPasswordsButton.setSelected(false);
            showPasswords();
        }

        // Validation
        var result = ControllerRules.Run(
                checkPlaceEmpty(),
                checkLength(),
                checkIfNotMatch()
        );
        if (result != null) {
            return result;
        }

        String username = usernameField.getText();
        String password = password1Field.getText();

        // Use new AuthService
        serviceManager.getAuthService().register(username, password)
                .thenAccept(response -> {
                    Platform.runLater(() -> {
                        if (response.isSuccess()) {
                            new SuccessResult("Registration successful! Please login.");
                        } else {
                            new ErrorResult(response.getMessage() != null ?
                                    response.getMessage() :
                                    "Registration failed. Username might be taken.");
                        }
                    });
                })
                .exceptionally(ex -> {
                    Platform.runLater(() -> {
                        logger.error("Registration error", ex);
                        new ErrorResult("Connection error. Please check your internet connection.");
                    });
                    return null;
                });

        return new SuccessResult();
    }

    private Result checkPlaceEmpty() {
        if (usernameField.getText().isBlank() ||
                password1Field.getText().isBlank() ||
                password2Field.getText().isBlank()) {
            return new ErrorResult("Please fill out all places!");
        }
        return new SuccessResult();
    }

    private Result checkLength() {
        if (password1Field.getText().length() < 4 ||
                password2Field.getText().length() < 4) {
            return new ErrorResult("Password must be at least 4 characters length!");
        }
        return new SuccessResult();
    }

    private Result checkIfNotMatch() {
        if (!password1Field.getText().equals(password2Field.getText())) {
            return new ErrorResult("Passwords are not matching!");
        }
        return new SuccessResult();
    }
}