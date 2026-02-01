package goksoft.chat.app.ui.components;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Modern message bubble component - WhatsApp/Signal style
 *
 * Features:
 * - Different styling for sent vs received messages
 * - Rounded corners
 * - Timestamps
 * - Word wrapping
 * - Maximum width for readability
 */
public class MessageBubble extends HBox {

    private static final int MAX_WIDTH = 460;
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    private final boolean isSent;
    private final String content;
    private final String sender;
    private final LocalDateTime timestamp;

    /**
     * Create a message bubble
     *
     * @param sender Username of sender
     * @param content Message text
     * @param isSent True if current user sent this message
     * @param timestamp When message was sent (optional, can be null)
     */
    public MessageBubble(String sender, String content, boolean isSent, LocalDateTime timestamp) {
        this.sender = sender;
        this.content = content;
        this.isSent = isSent;
        this.timestamp = timestamp;

        setupLayout();
        buildBubble();
    }

    /**
     * Create a message bubble without timestamp
     */
    public MessageBubble(String sender, String content, boolean isSent) {
        this(sender, content, isSent, null);
    }

    private void setupLayout() {
        setPadding(new Insets(4, 16, 4, 16));
        setMaxWidth(Double.MAX_VALUE);

        if (isSent) {
            setAlignment(Pos.CENTER_RIGHT);
        } else {
            setAlignment(Pos.CENTER_LEFT);
        }
    }

    private void buildBubble() {
        VBox bubble = new VBox(4);
        bubble.setMaxWidth(MAX_WIDTH);
        bubble.setPadding(new Insets(10, 14, 10, 14));

        // Style bubble based on sender
        if (isSent) {
            bubble.setStyle(
                    "-fx-background-color: #5865f2;" +
                            "-fx-background-radius: 18px 18px 4px 18px;" +
                            "-fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.08), 4, 0, 0, 1);"
            );
        } else {
            bubble.setStyle(
                    "-fx-background-color: #f2f3f5;" +
                            "-fx-background-radius: 18px 18px 18px 4px;" +
                            "-fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.06), 4, 0, 0, 1);"
            );
        }

        // Sender name (only for received messages)
        if (!isSent && sender != null && !sender.isEmpty()) {
            Label senderLabel = new Label(sender);
            senderLabel.setFont(Font.font("System", javafx.scene.text.FontWeight.SEMI_BOLD, 13));
            senderLabel.setStyle("-fx-text-fill: #5865f2;");
            bubble.getChildren().add(senderLabel);
        }

        // Message text
        Label messageLabel = new Label(content);
        messageLabel.setWrapText(true);
        messageLabel.setFont(Font.font("System", 14));
        messageLabel.setMaxWidth(MAX_WIDTH - 28); // Account for padding

        if (isSent) {
            messageLabel.setStyle("-fx-text-fill: white;");
        } else {
            messageLabel.setStyle("-fx-text-fill: #2e3338;");
        }

        bubble.getChildren().add(messageLabel);

        // Timestamp (if provided)
        if (timestamp != null) {
            Label timeLabel = new Label(timestamp.format(TIME_FORMAT));
            timeLabel.setFont(Font.font("System", 11));

            if (isSent) {
                timeLabel.setStyle("-fx-text-fill: rgba(255, 255, 255, 0.7);");
            } else {
                timeLabel.setStyle("-fx-text-fill: #80848e;");
            }

            HBox timeBox = new HBox(timeLabel);
            timeBox.setAlignment(isSent ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);
            bubble.getChildren().add(timeBox);
        }

        getChildren().add(bubble);
    }

    /**
     * Factory method for sent messages
     */
    public static MessageBubble sent(String content, LocalDateTime timestamp) {
        return new MessageBubble("You", content, true, timestamp);
    }

    /**
     * Factory method for received messages
     */
    public static MessageBubble received(String sender, String content, LocalDateTime timestamp) {
        return new MessageBubble(sender, content, false, timestamp);
    }

    /**
     * Factory method for sent message without timestamp
     */
    public static MessageBubble sent(String content) {
        return new MessageBubble("You", content, true, null);
    }

    /**
     * Factory method for received message without timestamp
     */
    public static MessageBubble received(String sender, String content) {
        return new MessageBubble(sender, content, false, null);
    }
}