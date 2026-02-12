package goksoft.chat.app.ui.components;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.layout.*;

/**
 * Friend list item — chat conversation row in the sidebar.
 *
 * Layout matches the prototype:
 *   [Gradient Avatar]  Name           Time
 *                      Last message…  (badge)
 *
 * - Left accent border when selected
 * - Unread badge with glow
 * - Ellipsis on long messages
 *
 * Signature preserved for controller compatibility.
 */
public class FriendBoxComponent {

    public static BorderPane create(String friendName, String lastMessage,
                                    String notifCount, String lastDate,
                                    Runnable onClickCallback) {

        BorderPane container = new BorderPane();
        container.getStyleClass().add("friend-item");
        container.setPrefHeight(68);
        container.setCursor(Cursor.HAND);
        container.setId(friendName);

        // ── Avatar (gradient with initials) ──
        StackPane avatar = AvatarFactory.create(friendName, 22);
        BorderPane.setAlignment(avatar, Pos.CENTER);
        BorderPane.setMargin(avatar, new Insets(0, 12, 0, 0));

        // ── Center: name + message preview ──
        VBox centerBox = new VBox(3);
        centerBox.setAlignment(Pos.CENTER_LEFT);
        centerBox.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(centerBox, Priority.ALWAYS);

        Label nameLabel = new Label(friendName);
        nameLabel.getStyleClass().add("friend-name");

        boolean hasUnread = false;
        try {
            hasUnread = Integer.parseInt(notifCount) > 0;
        } catch (NumberFormatException ignored) {}

        String previewText = (lastMessage != null && !lastMessage.isEmpty())
                ? lastMessage : "Start a conversation";
        Label messageLabel = new Label(previewText);
        messageLabel.getStyleClass().add(hasUnread ? "friend-last-message-unread" : "friend-last-message");
        messageLabel.setMaxWidth(180);
        messageLabel.setEllipsisString("…");

        centerBox.getChildren().addAll(nameLabel, messageLabel);

        // ── Right: time + badge ──
        VBox rightBox = new VBox(6);
        rightBox.setAlignment(Pos.CENTER_RIGHT);
        rightBox.setMinWidth(50);

        if (lastDate != null && !lastDate.isEmpty()) {
            Label timeLabel = new Label(lastDate);
            timeLabel.getStyleClass().add("friend-time");
            rightBox.getChildren().add(timeLabel);
        }

        // Unread badge
        if (hasUnread) {
            int count;
            try {
                count = Integer.parseInt(notifCount);
            } catch (NumberFormatException e) {
                count = 0;
            }

            if (count > 0) {
                StackPane badge = new StackPane();
                badge.getStyleClass().add("notification-badge");

                Label badgeText = new Label(count > 99 ? "99+" : String.valueOf(count));
                badgeText.getStyleClass().add("notification-badge-text");
                badge.getChildren().add(badgeText);

                HBox badgeRow = new HBox();
                badgeRow.setAlignment(Pos.CENTER_RIGHT);
                badgeRow.getChildren().add(badge);
                rightBox.getChildren().add(badgeRow);
            }
        }

        // ── Assemble ──
        container.setLeft(avatar);
        container.setCenter(centerBox);
        container.setRight(rightBox);

        if (onClickCallback != null) {
            container.setOnMouseClicked(event -> onClickCallback.run());
        }

        return container;
    }
}
