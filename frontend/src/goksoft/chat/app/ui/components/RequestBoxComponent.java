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
import javafx.scene.layout.VBox;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;

/**
 * Signal-style friend request card component.
 * Shows avatar, requester name, and accept/reject buttons.
 */
public class RequestBoxComponent {

    /**
     * Create a friend request card
     *
     * @param requesterName Username of person who sent request
     * @param photo         Profile photo (nullable)
     * @param onAccept      Callback when accept button clicked
     * @param onReject      Callback when reject button clicked
     * @return BorderPane containing request card
     */
    public static BorderPane create(String requesterName, Image photo,
                                    EventHandler<MouseEvent> onAccept,
                                    EventHandler<MouseEvent> onReject) {

        BorderPane container = new BorderPane();
        container.getStyleClass().add("request-item");
        container.setPrefHeight(72);

        // Avatar
        Circle avatar = new Circle(20);
        avatar.setStrokeWidth(0);
        if (photo != null && !photo.isError()) {
            avatar.setFill(new ImagePattern(photo));
        } else {
            avatar.getStyleClass().add("profile-circle-default");
        }
        BorderPane.setAlignment(avatar, Pos.CENTER);
        BorderPane.setMargin(avatar, new Insets(0, 12, 0, 0));

        // Center: name + subtitle
        VBox centerBox = new VBox(2);
        centerBox.setAlignment(Pos.CENTER_LEFT);

        Label nameLabel = new Label(requesterName);
        nameLabel.getStyleClass().add("request-name");

        Label subtitleLabel = new Label("Wants to connect");
        subtitleLabel.getStyleClass().add("request-label");

        centerBox.getChildren().addAll(nameLabel, subtitleLabel);

        // Right: buttons
        HBox buttonBox = new HBox(8);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);

        Button acceptBtn = new Button("Accept");
        acceptBtn.getStyleClass().add("btn-accept");
        if (onAccept != null) acceptBtn.setOnMouseClicked(onAccept);

        Button rejectBtn = new Button("Decline");
        rejectBtn.getStyleClass().add("btn-reject");
        if (onReject != null) rejectBtn.setOnMouseClicked(onReject);

        buttonBox.getChildren().addAll(rejectBtn, acceptBtn);

        // Assemble
        container.setLeft(avatar);
        container.setCenter(centerBox);
        container.setRight(buttonBox);
        BorderPane.setAlignment(buttonBox, Pos.CENTER);

        return container;
    }
}