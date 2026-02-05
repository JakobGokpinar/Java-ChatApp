package goksoft.chat.app.ui.components;

import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.shape.Circle;

/**
 * Signal-style user search result component.
 * Shows avatar, username, and an add-friend button.
 */
public class UserBoxComponent {

    /**
     * Create a user search result item
     *
     * @param userName   Username of found user
     * @param photo      Profile photo (nullable)
     * @param onAddClick Callback when add button clicked
     * @return HBox containing user info
     */
    public static HBox create(String userName, Image photo,
                              EventHandler<MouseEvent> onAddClick) {

        HBox container = new HBox(12);
        container.getStyleClass().add("user-item");
        container.setAlignment(Pos.CENTER_LEFT);
        container.setPrefHeight(56);

        Circle avatar = AvatarComponent.createAvatar(22, photo);

        // Username
        Label nameLabel = new Label(userName);
        nameLabel.getStyleClass().add("friend-name");
        nameLabel.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(nameLabel, Priority.ALWAYS);

        // Add button
        Button addButton = new Button("Add");
        addButton.getStyleClass().add("btn-add-friend");
        if (onAddClick != null) {
            addButton.setOnMouseClicked(onAddClick);
        }

        container.getChildren().addAll(avatar, nameLabel, addButton);

        return container;
    }
}