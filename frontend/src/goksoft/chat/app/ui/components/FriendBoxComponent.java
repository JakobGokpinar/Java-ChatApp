package goksoft.chat.app.ui.components;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextBoundsType;

/**
 * Component for rendering a friend box in the friends list
 */
public class FriendBoxComponent {

    // Colors
    private static final String COLOR_BORDER = "#949494";
    private static final String COLOR_TEXT_SECONDARY = "#949494";
    private static final String COLOR_NOTIFICATION = "#ff6f00";
    private static final String COLOR_BG_DARK = "#1c1b1b";

    /**
     * Create a friend box UI component
     *
     * @param friendName Friend's username
     * @param lastMessage Last message text
     * @param notifCount Notification count (as string)
     * @param lastDate Last message timestamp
     * @param photo Profile photo (nullable)
     * @param onClickCallback Callback when friend box is clicked
     * @return BorderPane containing friend info
     */
    public static BorderPane create(String friendName, String lastMessage,
                                    String notifCount, String lastDate,
                                    Image photo, Runnable onClickCallback) {

        // Main container
        BorderPane borderPane = new BorderPane();
        borderPane.setPrefHeight(86);
        borderPane.setPrefWidth(237);
        borderPane.setStyle("-fx-border-color: " + COLOR_BORDER + "; -fx-border-width: 0.5px 0px 0.5px 0px");
        borderPane.setCursor(Cursor.HAND);
        borderPane.setId(friendName);

        // Profile photo circle
        Circle profileCircle = new Circle(21);
        profileCircle.setStrokeWidth(0);
        if (photo == null || photo.isError()) {
            profileCircle.setFill(Color.BLACK);
        } else {
            profileCircle.setFill(new ImagePattern(photo));
        }
        BorderPane.setAlignment(profileCircle, Pos.CENTER);
        BorderPane.setMargin(profileCircle, new Insets(0, 0, 30, 10));

        // Friend name label
        Label nameLabel = new Label(friendName);
        nameLabel.setPrefHeight(30);
        nameLabel.setPrefWidth(246);
        nameLabel.setFont(new Font(15));
        nameLabel.setTextFill(Color.WHITE);
        nameLabel.setPadding(new Insets(10, 0, 0, 70));
        BorderPane.setAlignment(nameLabel, Pos.CENTER_LEFT);

        // Last message label
        Label messageLabel = new Label(lastMessage);
        messageLabel.setMaxWidth(Double.MAX_VALUE);
        messageLabel.setPrefHeight(18);
        messageLabel.setPrefWidth(49);
        messageLabel.setTextFill(Color.web(COLOR_TEXT_SECONDARY));
        BorderPane.setAlignment(messageLabel, Pos.CENTER_LEFT);
        BorderPane.setMargin(messageLabel, new Insets(0, 0, 10, 15));

        // Right section (notification + date)
        HBox rightBox = new HBox();
        rightBox.setMaxWidth(Double.MAX_VALUE);
        rightBox.setPrefHeight(42);
        rightBox.setPrefWidth(87);
        rightBox.setAlignment(Pos.CENTER_LEFT);
        rightBox.setPadding(new Insets(0, 0, 10, 0));
        BorderPane.setAlignment(rightBox, Pos.CENTER_LEFT);

        // Date label
        Label dateLabel = new Label(lastDate);
        dateLabel.setMaxWidth(Double.MAX_VALUE);
        dateLabel.setPrefHeight(18);
        dateLabel.setPrefWidth(71);
        dateLabel.setTextFill(Color.web(COLOR_TEXT_SECONDARY));
        dateLabel.setAlignment(Pos.CENTER);
        BorderPane.setMargin(dateLabel, new Insets(0, 10, 0, 0));

        // Add notification badge if count > 0
        try {
            int count = Integer.parseInt(notifCount);
            if (count > 0) {
                // Notification badge
                Circle notifCircle = new Circle(9);
                notifCircle.setFill(Color.web(COLOR_NOTIFICATION));
                notifCircle.setStroke(Color.web(COLOR_NOTIFICATION));

                Text notifText = new Text(notifCount);
                notifText.setFill(Color.WHITE);
                notifText.setBoundsType(TextBoundsType.VISUAL);

                StackPane badgeStack = new StackPane(notifCircle, notifText);
                rightBox.getChildren().addAll(badgeStack, dateLabel);
            } else {
                rightBox.getChildren().add(dateLabel);
            }
        } catch (NumberFormatException e) {
            rightBox.getChildren().add(dateLabel);
        }

        // Assemble components
        borderPane.setLeft(profileCircle);
        borderPane.setTop(nameLabel);
        borderPane.setCenter(messageLabel);
        borderPane.setRight(rightBox);

        // Set click handler
        if (onClickCallback != null) {
            borderPane.setOnMouseClicked(event -> onClickCallback.run());
        }

        return borderPane;
    }
}