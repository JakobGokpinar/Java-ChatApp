package goksoft.chat.app.ui.components;

import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;

/**
 * Component for rendering a user search result box
 */
public class UserBoxComponent {

    // Colors
    private static final String COLOR_BG = "#ff7d1a";
    private static final String COLOR_BORDER = "#ff6f00";
    private static final String COLOR_BTN = "#1c1b1b";

    /**
     * Create a user search result box UI component
     *
     * @param userName Username of found user
     * @param photo Profile photo (nullable)
     * @param onAddClick Callback when add button clicked
     * @return HBox containing user info
     */
    public static HBox create(String userName, Image photo, EventHandler<MouseEvent> onAddClick) {

        // Main container
        HBox container = new HBox();
        container.setPrefHeight(59);
        container.setPrefWidth(237);
        container.setAlignment(Pos.CENTER_LEFT);
        container.setStyle("-fx-background-color: " + COLOR_BG +
                "; -fx-border-color: " + COLOR_BORDER +
                "; -fx-border-width: 1.5px");

        // Profile photo circle
        Circle profileCircle = new Circle(21);
        profileCircle.setStrokeWidth(0);
        if (photo == null || photo.isError()) {
            profileCircle.setFill(Color.BLACK);
        } else {
            profileCircle.setFill(new ImagePattern(photo));
        }
        HBox.setMargin(profileCircle, new Insets(0, 0, 0, 10));

        // Username label
        Label nameLabel = new Label(userName);
        nameLabel.setMaxWidth(Double.MAX_VALUE);
        nameLabel.setPrefHeight(20);
        nameLabel.setPrefWidth(87);
        nameLabel.setFont(new Font(14));
        nameLabel.setTextFill(Color.WHITE);
        nameLabel.setAlignment(Pos.CENTER);
        HBox.setMargin(nameLabel, new Insets(0, 10, 0, 10));
        HBox.setHgrow(nameLabel, javafx.scene.layout.Priority.ALWAYS);

        // Add button
        Button addButton = new Button("+ Add");
        addButton.setPrefHeight(26);
        addButton.setPrefWidth(51);
        addButton.setStyle("-fx-background-color: " + COLOR_BTN);
        addButton.setTextFill(Color.WHITE);
        HBox.setMargin(addButton, new Insets(0, 10, 0, 0));

        if (onAddClick != null) {
            addButton.setOnMouseClicked(onAddClick);
        }

        // Assemble components
        container.getChildren().addAll(profileCircle, nameLabel, addButton);

        return container;
    }
}