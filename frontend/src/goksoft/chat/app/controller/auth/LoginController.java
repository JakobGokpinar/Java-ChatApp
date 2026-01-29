package goksoft.chat.app.controller.auth;

import goksoft.chat.app.controller.dialog.WarningWindowController;
import goksoft.chat.app.service.ServiceManager;
import goksoft.chat.app.util.SceneUtil;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.prefs.Preferences;

public class LoginController {

    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);
    private static final String PREF_REMEMBER_ME = "rememberMe";
    private static final String PREF_USERNAME = "username";

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private TextField textField;
    @FXML private CheckBox rememberMeButton;
    @FXML private CheckBox showPasswordButton;
    @FXML private Button signinbutton;

    private final ServiceManager serviceManager = ServiceManager.getInstance();
    private final Preferences prefs = Preferences.userNodeForPackage(LoginController.class);

    @FXML
    public void initialize() {

        usernameField.setOnKeyReleased(event -> {
            if (event.getCode() == KeyCode.ENTER) signIn();
        });
        passwordField.setOnKeyReleased(event -> {
            if (event.getCode() == KeyCode.ENTER) signIn();
        });
        signinbutton.setOnKeyReleased(event -> {
            if (event.getCode() == KeyCode.ENTER) signIn();
        });
        rememberMeFill();
    }

    public void changeSceneToRegister(MouseEvent event) {
        SceneUtil.switchScene(usernameField, "goksoft/chat/app/view/main/register.fxml", "Register", getClass());
    }

    public void rememberMeListener(MouseEvent event) {
        if (rememberMeButton.isSelected()) {
            // Store only username, never password
            prefs.putBoolean(PREF_REMEMBER_ME, true);
            prefs.put(PREF_USERNAME, usernameField.getText());
            logger.info("Remember Me enabled for user: {}", usernameField.getText());
        } else {
            // Clear stored preferences
            prefs.putBoolean(PREF_REMEMBER_ME, false);
            prefs.remove(PREF_USERNAME);
            logger.info("Remember Me disabled");
        }
    }

    public void rememberMeFill() {
        boolean rememberMe = prefs.getBoolean(PREF_REMEMBER_ME, false);

        if (rememberMe) {
            String savedUsername = prefs.get(PREF_USERNAME, "");
            if (!savedUsername.isEmpty()) {
                usernameField.setText(savedUsername);
                rememberMeButton.setSelected(true);
                logger.debug("Loaded saved username: {}", savedUsername);
            }
        }
    }

    public void showPassword(MouseEvent event) {
        String pass = passwordField.getText();
        if (showPasswordButton.isSelected()) {
            passwordField.setVisible(false);
            textField.setText(pass);
            textField.setVisible(true);
            return;
        }
        textField.setVisible(false);
        passwordField.setText(textField.getText());
        passwordField.setVisible(true);
    }

    public void signInButton(MouseEvent event) {
        signIn();
    }

    public void signIn() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        signinbutton.setDisable(true);

        serviceManager.getAuthService().login(username, password)
                .thenAccept(response -> {
                    Platform.runLater(() -> {
                        if (response.isSuccess() && response.getData() != null) {
                            serviceManager.setCurrentUser(username);
                            SceneUtil.closeAndOpenNew(usernameField, "goksoft/chat/app/view/main/MainPanel.fxml",
                                    "Chat", getClass(), false, false);
                        } else {
                            signinbutton.setDisable(false);
                            String message = response.getMessage() != null ?
                                    response.getMessage() : "Wrong username or password!";
                            WarningWindowController.warningMessage(message);
                        }
                    });
                })
                .exceptionally(ex -> {
                    Platform.runLater(() -> {
                        signinbutton.setDisable(false);
                        WarningWindowController.warningMessage("Connection error. Please check your internet connection.");
                    });
                    return null;
                });
    }
}