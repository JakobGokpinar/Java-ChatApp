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
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

/**
 * Modern friend list item component
 *
 * Features:
 * - Profile photo with fallback
 * - Friend name
 * - Last message preview
 * - Notification badge
 * - Timestamp
 * - Hover effects
 * - Click handler
 */
public class FriendListItem extends BorderPane {

    private static final String STYLE_NORMAL =
            "-fx-background-color: transparent;" +
                    "-fx-border-color: #e3e5e8;" +
                    "-fx-border-width: 0 0 1 0;" +
                    "-fx-cursor: hand;";

    private static final String STYLE_HOVER =
            "-fx-background-color: #f8f9fa;" +
                    "-fx-border-color: #e3e5e8;" +
                    "-fx-border-width: 0 0 1 0;" +
                    "-fx-cursor: hand;";

    private final String friendName;
    private final String lastMessage;
    private final int notificationCount;
    private final String timestamp;
    private final Image profilePhoto;
    private final Runnable onClickHandler;

    /**
     * Create a friend list item
     *
     * @param friendName Friend's username
     * @param lastMessage Last message preview (can be empty)
     * @param notificationCount Unread message count
     * @param timestamp Last message time (e.g., "2m ago", "14:30")
     * @param profilePhoto Profile photo (nullable)
     * @param onClickHandler Click handler (nullable)
     */
    public FriendListItem(String friendName, String lastMessage, int notificationCount,
                          String timestamp, Image profilePhoto, Runnable onClickHandler) {
        this.friendName = friendName;
        this.lastMessage = lastMessage != null ? lastMessage : "";
        this.notificationCount = notificationCount;
        this.timestamp = timestamp != null ? timestamp : "";
        this.profilePhoto = profilePhoto;
        this.onClickHandler = onClickHandler;

        setupLayout();
        buildComponents();
        setupInteractions();
    }

    /**
     * Constructor with notification count as String (for compatibility)
     */
    public FriendListItem(String friendName, String lastMessage, String notifCountStr,
                          String timestamp, Image profilePhoto, Runnable onClickHandler) {
        this(friendName, lastMessage, parseNotifCount(notifCountStr),
                timestamp, profilePhoto, onClickHandler);
    }

    private static int parseNotifCount(String str) {
        try {
            return Integer.parseInt(str);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private void setupLayout() {
        setPrefHeight(76);
        setMaxWidth(Double.MAX_VALUE);
        setPadding(new Insets(12, 16, 12, 16));
        setStyle(STYLE_NORMAL);
        setCursor(Cursor.HAND);
        setId(friendName); // For identification
    }

    private void buildComponents() {
        // Left: Profile photo
        Circle photoCircle = createProfilePhoto();
        BorderPane.setAlignment(photoCircle, Pos.CENTER);
        setLeft(photoCircle);

        // Center: Name and message
        VBox centerBox = createCenterContent();
        BorderPane.setAlignment(centerBox, Pos.CENTER_LEFT);
        BorderPane.setMargin(centerBox, new Insets(0, 12, 0, 12));
        setCenter(centerBox);

        // Right: Time and badge
        VBox rightBox = createRightContent();
        BorderPane.setAlignment(rightBox, Pos.TOP_RIGHT);
        setRight(rightBox);
    }

    private Circle createProfilePhoto() {
        Circle circle = new Circle(24);
        circle.setStrokeWidth(0);

        if (profilePhoto == null || profilePhoto.isError()) {
            // Fallback: colored circle with initial
            circle.setFill(Color.web("#5865f2"));

            // Add initial letter
            if (!friendName.isEmpty()) {
                Text initial = new Text(friendName.substring(0, 1).toUpperCase());
                initial.setFill(Color.WHITE);
                initial.setFont(Font.font("System", FontWeight.SEMI_BOLD, 14));

                StackPane photoStack = new StackPane(circle, initial);
                setLeft(photoStack);
                return circle;
            }
        } else {
            circle.setFill(new ImagePattern(profilePhoto));
        }

        return circle;
    }

    private VBox createCenterContent() {
        VBox vbox = new VBox(4);

        // Friend name
        Label nameLabel = new Label(friendName);
        nameLabel.setFont(Font.font("System", FontWeight.SEMI_BOLD, 15));
        nameLabel.setTextFill(Color.web("#2e3338"));
        nameLabel.setMaxWidth(Double.MAX_VALUE);

        // Last message preview
        Label messageLabel = new Label(lastMessage);
        messageLabel.setFont(Font.font("System", 13));
        messageLabel.setTextFill(Color.web("#80848e"));
        messageLabel.setMaxWidth(180);
        messageLabel.setStyle("-fx-text-overrun: ellipsis;");

        vbox.getChildren().addAll(nameLabel, messageLabel);
        return vbox;
    }

    private VBox createRightContent() {
        VBox vbox = new VBox(6);
        vbox.setAlignment(Pos.TOP_RIGHT);

        // Timestamp
        Label timeLabel = new Label(timestamp);
        timeLabel.setFont(Font.font("System", 12));
        timeLabel.setTextFill(Color.web("#80848e"));
        vbox.getChildren().add(timeLabel);

        // Notification badge (if count > 0)
        if (notificationCount > 0) {
            StackPane badge = createNotificationBadge();
            vbox.getChildren().add(badge);
        }

        return vbox;
    }

    private StackPane createNotificationBadge() {
        Circle badgeCircle = new Circle(10);
        badgeCircle.setFill(Color.web("#5865f2"));

        String countText = notificationCount > 9 ? "9+" : String.valueOf(notificationCount);
        Label countLabel = new Label(countText);
        countLabel.setFont(Font.font("System", FontWeight.BOLD, 10));
        countLabel.setTextFill(Color.WHITE);

        StackPane badge = new StackPane(badgeCircle, countLabel);
        badge.setAlignment(Pos.CENTER);

        return badge;
    }

    private void setupInteractions() {
        // Hover effect
        setOnMouseEntered(e -> setStyle(STYLE_HOVER));
        setOnMouseExited(e -> setStyle(STYLE_NORMAL));

        // Click handler
        if (onClickHandler != null) {
            setOnMouseClicked(e -> onClickHandler.run());
        }
    }

    /**
     * Update notification count
     */
    public void updateNotificationCount(int count) {
        // Rebuild right content with new count
        VBox rightBox = createRightContent();
        setRight(rightBox);
    }

    /**
     * Update last message
     */
    public void updateLastMessage(String message, String time) {
        VBox centerBox = createCenterContent();
        BorderPane.setMargin(centerBox, new Insets(0, 12, 0, 12));
        setCenter(centerBox);

        VBox rightBox = createRightContent();
        setRight(rightBox);
    }
}