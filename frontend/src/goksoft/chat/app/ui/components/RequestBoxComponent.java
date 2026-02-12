package goksoft.chat.app.ui.components;

import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;

/**
 * Friend request card — shown in the Requests panel.
 *
 * Layout:
 *   [Gradient Avatar]  Name               [Decline] [Accept]
 *                      Wants to connect
 *
 * - Card background with 1px border and rounded corners
 * - Hover lifts border brightness
 * - Accept = green tinted, Decline = outline ghost
 *
 * Signature preserved for controller compatibility.
 */
public class RequestBoxComponent {

    public static BorderPane create(String requesterName,
                                    EventHandler<MouseEvent> onAccept,
                                    EventHandler<MouseEvent> onReject) {

        BorderPane container = new BorderPane();
        container.getStyleClass().add("request-item");
        container.setPrefHeight(70);
        container.setId(requesterName);

        // ── Avatar ──
        StackPane avatar = AvatarFactory.create(requesterName, 20);
        BorderPane.setAlignment(avatar, Pos.CENTER);
        BorderPane.setMargin(avatar, new Insets(0, 12, 0, 0));

        // ── Center: name + subtitle ──
        // ── Center: name + subtitle ──
        VBox centerBox = new VBox(3);
        centerBox.setAlignment(Pos.CENTER_LEFT);
        centerBox.setMaxWidth(Double.MAX_VALUE);

        Label nameLabel = new Label(requesterName);
        nameLabel.getStyleClass().add("request-name");

        Label subtitleLabel = new Label("Wants to connect");
        subtitleLabel.getStyleClass().add("request-label");

        centerBox.getChildren().addAll(nameLabel, subtitleLabel);

        // ── Right: action buttons ──
        HBox buttonBox = new HBox(8);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        buttonBox.setMinWidth(Region.USE_PREF_SIZE);

        Button rejectBtn = new Button("Decline");
        rejectBtn.getStyleClass().add("btn-reject");
        rejectBtn.setMinWidth(Region.USE_PREF_SIZE);
        if (onReject != null) rejectBtn.setOnMouseClicked(onReject);

        Button acceptBtn = new Button("Accept");
        acceptBtn.getStyleClass().add("btn-accept");
        acceptBtn.setMinWidth(Region.USE_PREF_SIZE);
        if (onAccept != null) acceptBtn.setOnMouseClicked(onAccept);

        buttonBox.getChildren().addAll(rejectBtn, acceptBtn);

        // ── Assemble ──
        container.setLeft(avatar);
        container.setCenter(centerBox);
        container.setRight(buttonBox);
        BorderPane.setAlignment(buttonBox, Pos.CENTER);
        BorderPane.setMargin(buttonBox, new Insets(0, 0, 0, 8));

        return container;
    }
}
