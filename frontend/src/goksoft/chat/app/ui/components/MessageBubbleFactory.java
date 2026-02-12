package goksoft.chat.app.ui.components;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.Callback;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * Custom cell factory for the chat ListView that renders messages as bubbles.
 *
 * Message format: "sender: message" or "sender|HH:mm: message" (with timestamp)
 *
 * Sent messages: right-aligned indigo gradient with white text
 * Received messages: left-aligned dark card with border
 * Both include a small timestamp in the bottom corner.
 */
public class MessageBubbleFactory {

    private static final double MAX_BUBBLE_WIDTH = 420;
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    /**
     * Create a cell factory for the chat ListView.
     *
     * @param currentUser The logged-in user's username (for sent/received detection)
     * @return Callback to set on listView.setCellFactory()
     */
    public static Callback<ListView<String>, ListCell<String>> create(String currentUser) {
        return listView -> new ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                    setStyle("-fx-background-color: transparent;");
                    return;
                }

                setStyle("-fx-background-color: transparent; -fx-padding: 0;");
                setText(null);

                // Parse message — supports "sender: msg" and "sender|HH:mm: msg"
                String sender;
                String message;
                String timestamp = null;

                int colonIndex = item.indexOf(": ");
                if (colonIndex > 0) {
                    String prefix = item.substring(0, colonIndex);
                    message = item.substring(colonIndex + 2);

                    // Check for embedded timestamp: "sender|HH:mm"
                    int pipeIndex = prefix.indexOf('|');
                    if (pipeIndex > 0) {
                        sender = prefix.substring(0, pipeIndex);
                        timestamp = prefix.substring(pipeIndex + 1);
                    } else {
                        sender = prefix;
                    }
                } else {
                    sender = "";
                    message = item;
                }

                boolean isSent = sender.equals(currentUser);
                setGraphic(createBubble(message, isSent, timestamp));
            }
        };
    }

    /**
     * Format a message string with embedded timestamp for the ListView.
     * Use this when adding messages to include the time.
     *
     * @param sender   The sender username
     * @param message  The message text
     * @return Formatted string: "sender|HH:mm: message"
     */
    public static String formatMessage(String sender, String message) {
        String time = LocalTime.now().format(TIME_FORMAT);
        return sender + "|" + time + ": " + message;
    }

    /**
     * Format with explicit timestamp.
     */
    public static String formatMessage(String sender, String message, String time) {
        return sender + "|" + time + ": " + message;
    }

    private static HBox createBubble(String message, boolean isSent, String timestamp) {
        // Outer row — controls alignment
        HBox row = new HBox();
        row.setPadding(new Insets(3, 16, 3, 16));
        row.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(row, Priority.ALWAYS);

        // Spacer for alignment
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Bubble container
        VBox bubble = new VBox(4);
        bubble.setMaxWidth(MAX_BUBBLE_WIDTH);
        bubble.setPadding(new Insets(10, 16, 8, 16));

        if (isSent) {
            bubble.getStyleClass().add("message-bubble-sent");
        } else {
            bubble.getStyleClass().add("message-bubble-received");
        }

        // Message text
        Label msgLabel = new Label(message);
        msgLabel.setWrapText(true);
        msgLabel.setMaxWidth(MAX_BUBBLE_WIDTH - 32);
        if (isSent) {
            msgLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14px; -fx-line-spacing: 2px;");
        } else {
            msgLabel.setStyle("-fx-text-fill: #EEEEF0; -fx-font-size: 14px; -fx-line-spacing: 2px;");
        }
        bubble.getChildren().add(msgLabel);

        // Timestamp
        if (timestamp != null && !timestamp.isEmpty()) {
            Label timeLabel = new Label(timestamp);
            timeLabel.setStyle(isSent
                    ? "-fx-text-fill: rgba(255,255,255,0.6); -fx-font-size: 10px;"
                    : "-fx-text-fill: #55556A; -fx-font-size: 10px;");
            if (isSent) {
                timeLabel.setAlignment(Pos.CENTER_RIGHT);
                timeLabel.setMaxWidth(Double.MAX_VALUE);
            }
            bubble.getChildren().add(timeLabel);
        }

        // Assemble row
        if (isSent) {
            row.setAlignment(Pos.CENTER_RIGHT);
            row.getChildren().addAll(spacer, bubble);
        } else {
            row.setAlignment(Pos.CENTER_LEFT);
            row.getChildren().addAll(bubble, spacer);
        }

        return row;
    }
}
