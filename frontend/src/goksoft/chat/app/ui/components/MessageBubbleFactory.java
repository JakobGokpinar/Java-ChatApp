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

/**
 * Custom cell factory for the chat ListView that renders messages as bubbles.
 *
 * Messages are expected in the format: "sender: message"
 * The current logged-in user's messages are right-aligned (sent),
 * others are left-aligned (received).
 *
 * Usage in MainPanelController.initialize():
 *   listView.setCellFactory(MessageBubbleFactory.create(loggedUser));
 *
 * Then add messages as before:
 *   listView.getItems().add("jakob: Hello!");
 *   listView.getItems().add("emma: Hey there!");
 */
public class MessageBubbleFactory {

    private static final double MAX_BUBBLE_WIDTH = 420;

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

                // Parse "sender: message" format
                String sender;
                String message;
                int colonIndex = item.indexOf(": ");
                if (colonIndex > 0) {
                    sender = item.substring(0, colonIndex);
                    message = item.substring(colonIndex + 2);
                } else {
                    sender = "";
                    message = item;
                }

                boolean isSent = sender.equals(currentUser);

                // Build bubble
                setGraphic(createBubble(sender, message, isSent));
            }
        };
    }

    private static HBox createBubble(String sender, String message, boolean isSent) {
        // Outer row — controls alignment
        HBox row = new HBox();
        row.setPadding(new Insets(2, 12, 2, 12));
        row.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(row, Priority.ALWAYS);

        // Spacer for alignment
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Bubble container
        VBox bubble = new VBox(2);
        bubble.setMaxWidth(MAX_BUBBLE_WIDTH);
        bubble.setPadding(new Insets(10, 16, 10, 16));

        if (isSent) {
            bubble.getStyleClass().add("message-bubble-sent");
        } else {
            bubble.getStyleClass().add("message-bubble-received");

            // Sender name label (received only)
            if (sender != null && !sender.isEmpty()) {
                Label senderLabel = new Label(sender);
                senderLabel.getStyleClass().add("message-sender-label");
                bubble.getChildren().add(senderLabel);
            }
        }

        // Message text
        Label msgLabel = new Label(message);
        msgLabel.setWrapText(true);
        msgLabel.setMaxWidth(MAX_BUBBLE_WIDTH - 32);
        if (isSent) {
            msgLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14px;");
        } else {
            msgLabel.setStyle("-fx-text-fill: #EEEEF0; -fx-font-size: 14px;");
        }
        bubble.getChildren().add(msgLabel);

        // Assemble row: sent = [spacer, bubble], received = [bubble, spacer]
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
