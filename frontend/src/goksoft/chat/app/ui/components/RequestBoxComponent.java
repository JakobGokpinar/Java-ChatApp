package goksoft.chat.app.ui.components;

import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;

/**
 * Component for rendering a friend request box
 */
public class RequestBoxComponent {

    // Colors
    private static final String COLOR_BG_PRIMARY = "#ffb700";
    private static final String COLOR_BORDER = "#ffaa00";
    private static final String COLOR_BTN_ACCEPT = "#ff6f00";
    private static final String COLOR_BTN_REJECT = "#1c1b1b";

    /**
     * Create a friend request box UI component
     *
     * @param requesterName Username of person who sent request
     * @param photo Profile photo (nullable)
     * @param onAccept Callback when accept button clicked
     * @param onReject Callback when reject button clicked
     * @return BorderPane containing request info
     */
    public static BorderPane create(String requesterName, Image photo,
                                    EventHandler<MouseEvent> onAccept,
                                    EventHandler<MouseEvent> onReject) {

        // Main container
        BorderPane borderPane = new BorderPane();
        borderPane.setPrefHeight(75);
        borderPane.setPrefWidth(237);
        borderPane.setStyle("-fx-background-color: " + COLOR_BG_PRIMARY +
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
        BorderPane.setAlignment(profileCircle, Pos.CENTER);
        BorderPane.setMargin(profileCircle, new Insets(10, 0, 0, 10));

        // Requester name label
        Label nameLabel = new Label(requesterName);
        nameLabel.setPrefHeight(24);
        nameLabel.setPrefWidth(158);
        nameLabel.setFont(new Font(14));
        nameLabel.setPadding(new Insets(10, 0, 0, 0));
        BorderPane.setAlignment(nameLabel, Pos.CENTER_LEFT);
        BorderPane.setMargin(nameLabel, new Insets(0, 0, 0, 20));

        // Buttons container
        HBox buttonBox = new HBox(15);
        buttonBox.setPrefHeight(38);
        buttonBox.setPrefWidth(166);
        buttonBox.setAlignment(Pos.CENTER_LEFT);
        buttonBox.setPadding(new Insets(0, 0, 5, 0));
        BorderPane.setMargin(buttonBox, new Insets(0, 0, 0, 55));
        BorderPane.setAlignment(buttonBox, Pos.CENTER);

        // Accept button
        Button acceptButton = new Button("Accept");
        acceptButton.setPrefHeight(26);
        acceptButton.setPrefWidth(65);
        acceptButton.setStyle("-fx-background-color: " + COLOR_BTN_ACCEPT);
        acceptButton.setTextFill(Color.WHITE);
        if (onAccept != null) {
            acceptButton.setOnMouseClicked(onAccept);
        }

        // Reject button
        Button rejectButton = new Button("Reject");
        rejectButton.setPrefHeight(26);
        rejectButton.setPrefWidth(59);
        rejectButton.setStyle("-fx-background-color: " + COLOR_BTN_REJECT);
        rejectButton.setTextFill(Color.WHITE);
        if (onReject != null) {
            rejectButton.setOnMouseClicked(onReject);
        }

        buttonBox.getChildren().addAll(acceptButton, rejectButton);

        // Assemble components
        borderPane.setLeft(profileCircle);
        borderPane.setCenter(nameLabel);
        borderPane.setBottom(buttonBox);

        return borderPane;
    }
}