package goksoft.chat.app;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.Objects;


public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        System.out.println("Chat app is active");

        Parent loginRoot = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("userinterfaces/login.fxml")));
        Scene loginScene = new Scene(loginRoot);

        primaryStage.setScene(loginScene);
        primaryStage.setTitle("Login");
        primaryStage.setResizable(false);
        primaryStage.setX(455);
        primaryStage.setY(155);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
