package goksoft.chat.app.ui.components;

import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;

/**
 * User search result item — shown in Find People panel.
 *
 * Layout:
 *   [Gradient Avatar]  Username          [+ Add]
 *
 * - Card background with 1px border
 * - "Add" button transitions to "Sent ✓" after click
 *
 * Signature preserved for controller compatibility.
 */
public class UserBoxComponent {

    public static HBox create(String userName,
                              EventHandler<MouseEvent> onAddClick) {

        HBox container = new HBox(14);
        container.getStyleClass().add("user-item");
        container.setAlignment(Pos.CENTER_LEFT);
        container.setPrefHeight(58);

        // ── Avatar ──
        StackPane avatar = AvatarFactory.create(userName, 20);

        // ── Name ──
        Label nameLabel = new Label(userName);
        nameLabel.getStyleClass().add("friend-name");
        nameLabel.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(nameLabel, Priority.ALWAYS);

        // ── Add button with state change ──
        Button addButton = new Button("Add");
        addButton.getStyleClass().add("btn-add-friend");

        if (onAddClick != null) {
            addButton.setOnMouseClicked(event -> {
                // Fire the original handler (sends friend request)
                onAddClick.handle(event);

                // Transition to "Sent" state
                addButton.setText("Sent ✓");
                addButton.setDisable(true);
                addButton.getStyleClass().remove("btn-add-friend");
                addButton.getStyleClass().add("btn-add-friend-sent");
            });
        }

        container.getChildren().addAll(avatar, nameLabel, addButton);

        return container;
    }
}
