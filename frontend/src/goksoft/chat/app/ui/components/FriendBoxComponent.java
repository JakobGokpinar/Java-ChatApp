package goksoft.chat.app.ui.components;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;

/**
 * Signal-style friend list item component.
 * Shows avatar, name, last message preview, time, and unread badge.
 */
public class FriendBoxComponent {

    /**
     * Create a friend list item
     *
     * @param friendName    Friend's username
     * @param lastMessage   Last message preview text
     * @param notifCount    Unread message count (as string)
     * @param lastDate      Time since last message
     * @param photo         Profile photo (nullable, falls back to colored circle)
     * @param onClickCallback Action when item is clicked
     * @return BorderPane containing the friend item
     */
    public static BorderPane create(String friendName, String lastMessage,
                                    String notifCount, String lastDate,
                                    Image photo, Runnable onClickCallback) {

        BorderPane container = new BorderPane();
        container.getStyleClass().add("friend-item");
        container.setPrefHeight(68);
        container.setCursor(Cursor.HAND);
        container.setId(friendName);

        // Avatar
        Circle avatar = AvatarComponent.createAvatar(22, photo);

        BorderPane.setAlignment(avatar, Pos.CENTER);
        BorderPane.setMargin(avatar, new Insets(0, 12, 0, 0));

        // Center: name + last message
        VBox centerBox = new VBox(3);
        centerBox.setAlignment(Pos.CENTER_LEFT);

        Label nameLabel = new Label(friendName);
        nameLabel.getStyleClass().add("friend-name");

        Label messageLabel = new Label(
                lastMessage != null && !lastMessage.isEmpty() ? lastMessage : "No messages yet"
        );
        messageLabel.getStyleClass().add("friend-last-message");
        messageLabel.setMaxWidth(180);
        messageLabel.setEllipsisString("...");

        centerBox.getChildren().addAll(nameLabel, messageLabel);

        // Right: time + notification badge
        VBox rightBox = new VBox(6);
        rightBox.setAlignment(Pos.CENTER_RIGHT);
        rightBox.setMinWidth(50);

        Label timeLabel = new Label(lastDate != null ? lastDate : "");
        timeLabel.getStyleClass().add("friend-time");

        rightBox.getChildren().add(timeLabel);

        // Notification badge
        try {
            int count = Integer.parseInt(notifCount);
            if (count > 0) {
                StackPane badge = new StackPane();
                badge.getStyleClass().add("notification-badge");
                badge.setMaxSize(22, 22);
                badge.setMinSize(22, 22);

                Label badgeText = new Label(count > 99 ? "99+" : String.valueOf(count));
                badgeText.getStyleClass().add("notification-badge-text");

                badge.getChildren().add(badgeText);

                HBox badgeContainer = new HBox();
                badgeContainer.setAlignment(Pos.CENTER_RIGHT);
                badgeContainer.getChildren().add(badge);

                rightBox.getChildren().add(badgeContainer);
            }
        } catch (NumberFormatException ignored) {}

        // Assemble
        container.setLeft(avatar);
        container.setCenter(centerBox);
        container.setRight(rightBox);

        if (onClickCallback != null) {
            container.setOnMouseClicked(event -> onClickCallback.run());
        }

        return container;
    }
}