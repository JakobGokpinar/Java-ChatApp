package goksoft.chat.app;

import goksoft.chat.app.ErrorClass.ErrorResult;
import goksoft.chat.app.ErrorClass.Result;
import goksoft.chat.app.ErrorClass.SuccessResult;
import goksoft.chat.app.service.ServiceManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

public class LoginController {

    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private TextField textField;
    @FXML private CheckBox rememberMeButton;
    @FXML private CheckBox showPasswordButton;
    @FXML private Button signinbutton;

    public static String loggedUser;
    private final ServiceManager serviceManager = ServiceManager.getInstance();

    @FXML
    public void initialize() throws FileNotFoundException {
        usernameField.setText("jakob");
        passwordField.setText("1234");

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
        Function.switchBetweenRegisterAndLogin(event, "register");
    }

    public void setUsernameField(String text) {
        usernameField.setText(text);
    }

    public void setPasswordField(String text) {
        passwordField.setText(text);
    }

    public void rememberMeListener(MouseEvent event) {
        try {
            File file = new File(System.getProperty("user.home") + "/settings.txt");
            FileWriter writer = new FileWriter(file);
            if (rememberMeButton.isSelected()) {
                writer.write("rememberme:true\n" + "username:" + usernameField.getText() + "\n" + "pass:" + passwordField.getText());
            } else {
                writer.write("rememberme:false");
            }
            writer.close();
        } catch (IOException e) {
            logger.error("Failed to save remember me preference", e);
        }
    }

    public void rememberMeFill() throws FileNotFoundException {
        File file = new File(System.getProperty("user.home") + "/settings.txt");
        if (file.exists()) {
            Scanner scanner = new Scanner(file);
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                if (line.equals("rememberme:true")) {
                    line = scanner.nextLine();
                    if (line.contains("username:")) {
                        String[] user = line.split(":");
                        if (user.length > 1) {
                            setUsernameField(user[1]);
                        }
                        if (scanner.hasNextLine()) {
                            line = scanner.nextLine();
                            if (line.contains("pass:")) {
                                user = line.split(":");
                                if (user.length > 1) {
                                    setPasswordField(user[1]);
                                    rememberMeButton.setSelected(true);
                                }
                            }
                        }
                    }
                }
            }
            scanner.close();
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

    public Result signIn() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        signinbutton.setDisable(true);

        serviceManager.getAuthService().login(username, password)
                .thenAccept(response -> {
                    Platform.runLater(() -> {
                        if (response.isSuccess() && response.getData() != null) {
                            loggedUser = username;
                            GlobalVariables.setLoggedUser(loggedUser);
                            loadMainPanel();
                        } else {
                            signinbutton.setDisable(false);
                            new ErrorResult(response.getMessage() != null ? response.getMessage() : "Wrong username or password!");
                        }
                    });
                })
                .exceptionally(ex -> {
                    Platform.runLater(() -> {
                        signinbutton.setDisable(false);
                        new ErrorResult("Connection error. Please check your internet connection.");
                    });
                    return null;
                });

        return new SuccessResult();
    }

    private void loadMainPanel() {
        try {
            var fxmlUrl = LoginController.class.getResource("userinterfaces/MainPanel.fxml");
            if (fxmlUrl == null) {
                throw new IOException("MainPanel.fxml not found");
            }

            Parent mainPanel = FXMLLoader.load(fxmlUrl);
            Scene scene = new Scene(mainPanel);
            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.close();
            Stage newStage = new Stage();
            newStage.setScene(scene);
            newStage.setTitle("Chat");
            newStage.show();
        } catch (IOException e) {
            logger.error("Failed to load main panel", e);
            new ErrorResult("An error occurred while loading main panel");
        }
    }
}