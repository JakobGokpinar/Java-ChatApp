package goksoft.chat.app;

import javafx.embed.swing.SwingFXUtils;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextBoundsType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

/**
 * Pure UI component builder - no business logic, no API calls
 * All event handlers are passed in from controllers
 */
public class GUIComponents {

    private static final Logger logger = LoggerFactory.getLogger(GUIComponents.class);

    /**
     * Fetch and return profile photo for a user
     * @param username Username to fetch photo for
     * @return Image or null if not found
     */
    public static Image returnPhoto(String username) {
        String imageName = ServerFunctions.encodeURL(username);
        Image imagefx = null;
        try {
            BufferedImage image = ImageIO.read(
                    new URL(ServerFunctions.serverURL + "/users/photo/" + imageName)
            );
            if (image != null) {
                imagefx = SwingFXUtils.toFXImage(image, null);
            }
        } catch (IOException e) {
            logger.warn("Failed to load photo for user: {}", username);
        }
        return imagefx;
    }

    private static Circle createPhotoCircle(Image photo, double radius, double strokeWidth) {
        Circle circle = new Circle();
        circle.setRadius(radius);
        circle.setStrokeWidth(strokeWidth);
        if (photo == null || photo.isError()) {
            circle.setFill(Color.BLACK);
        } else {
            circle.setFill(new ImagePattern(photo));
        }
        return circle;
    }

    private static BorderPane createBorderPane(double height, double width, String style,
                                               Cursor cursor, String id) {
        BorderPane borderPane = new BorderPane();
        borderPane.setPrefHeight(height);
        borderPane.setPrefWidth(width);
        if (style != null) borderPane.setStyle(style);
        if (cursor != null) borderPane.setCursor(cursor);
        if (id != null) borderPane.setId(id);
        return borderPane;
    }

    private static Label createLabel(double maxHeight, double maxWidth, String text,
                                     double height, double width, double font,
                                     Color color, String stringColor, Pos pos) {
        Label label = new Label(text);
        if (maxHeight != 0) label.setMaxHeight(maxHeight);
        if (maxWidth != 0) label.setMaxWidth(maxWidth);
        label.setPrefHeight(height);
        label.setPrefWidth(width);
        if (font != 0) label.setFont(new Font(font));
        if (color != null) label.setTextFill(color);
        if (stringColor != null) label.setTextFill(Color.web(stringColor));
        if (pos != null) label.setAlignment(pos);
        return label;
    }

    private static HBox createHBox(double maxHeight, double maxWidth, double height,
                                   double width, String style, Pos pos, double[] insetArray) {
        HBox hBox = new HBox();
        if (maxHeight != 0) hBox.setMaxHeight(maxHeight);
        if (maxWidth != 0) hBox.setMaxWidth(maxWidth);
        hBox.setPrefHeight(height);
        hBox.setPrefWidth(width);
        if (style != null) hBox.setStyle(style);
        if (pos != null) hBox.setAlignment(pos);
        if (insetArray != null) {
            hBox.setPadding(new Insets(insetArray[0], insetArray[1], insetArray[2], insetArray[3]));
        }
        return hBox;
    }

    private static Button createButton(double height, double width, String text,
                                       String style, Color color, EventHandler event, Pos pos) {
        Button button = new Button(text);
        button.setPrefHeight(height);
        button.setPrefWidth(width);
        if (style != null) button.setStyle(style);
        if (color != null) button.setTextFill(color);
        if (event != null) {
            button.setOnMouseClicked(event);
            button.setOnKeyReleased(e -> {
                if (e.getCode() == KeyCode.ENTER) event.handle(e);
            });
        }
        if (pos != null) button.setAlignment(pos);
        return button;
    }

    /**
     * Create a friend box UI component
     * @param friendName Friend's username
     * @param lastMessage Last message text
     * @param notifCount Notification count
     * @param lastDate Last message timestamp
     * @param onClickCallback Callback when friend box is clicked
     * @return BorderPane containing friend info
     */
    public static BorderPane friendBox(String friendName, String lastMessage,
                                       String notifCount, String lastDate,
                                       Runnable onClickCallback) {
        String style = "-fx-border-color: #949494; -fx-border-width: 0.5px 0px 0.5px 0px";
        BorderPane borderPane = createBorderPane(86, 237, style, Cursor.HAND, friendName);

        Image friendPhoto = returnPhoto(friendName);
        Circle friendProfilePhoto = createPhotoCircle(friendPhoto, 21, 0);
        BorderPane.setAlignment(friendProfilePhoto, Pos.CENTER);
        BorderPane.setMargin(friendProfilePhoto, new Insets(0, 0, 30, 10));

        Label friend = createLabel(0, 0, friendName, 30, 246, 15, Color.WHITE, null, null);
        friend.setPadding(new Insets(10, 0, 0, 70));
        BorderPane.setAlignment(friend, Pos.CENTER_LEFT);

        Label lstMsg = createLabel(0, 1.7976931348623157E308, lastMessage, 18, 49, 0,
                null, "#949494", null);
        BorderPane.setAlignment(lstMsg, Pos.CENTER_LEFT);
        BorderPane.setMargin(lstMsg, new Insets(0, 0, 10, 15));

        double[] array = {0, 0, 10, 0};
        HBox hBox = createHBox(0, 1.7976931348623157E308, 42, 87, null, Pos.CENTER_LEFT, array);
        BorderPane.setAlignment(hBox, Pos.CENTER_LEFT);

        Circle notifCircle = new Circle(9);
        notifCircle.setFill(Color.web("#ff6f00"));
        notifCircle.setStroke(Color.web("#ff6f00"));
        Text text = new Text(notifCount);
        text.setFill(Color.WHITE);
        text.setBoundsType(TextBoundsType.VISUAL);
        StackPane stack = new StackPane();
        stack.getChildren().addAll(notifCircle, text);

        Label lstDt = createLabel(0, 1.7976931348623157E308, lastDate, 18, 71, 0,
                null, "#949494", Pos.CENTER);
        BorderPane.setMargin(lstDt, new Insets(0, 10, 0, 0));

        if (Integer.parseInt(notifCount) > 0) {
            hBox.getChildren().addAll(stack, lstDt);
        } else {
            hBox.getChildren().addAll(lstDt);
        }

        borderPane.setLeft(friendProfilePhoto);
        borderPane.setTop(friend);
        borderPane.setCenter(lstMsg);
        borderPane.setRight(hBox);

        // Use callback instead of static Function call
        if (onClickCallback != null) {
            borderPane.setOnMouseClicked(event -> onClickCallback.run());
        }

        return borderPane;
    }

    /**
     * Legacy version for backward compatibility
     * TODO: Remove after MainPanelController is fully updated
     */
    @Deprecated
    public static BorderPane friendBox(String friendName, String lastMessage,
                                       String notifCount, String lastDate) {
        return friendBox(friendName, lastMessage, notifCount, lastDate, () -> {
            Image friendPhoto = returnPhoto(friendName);
            // This uses old Function.getClickedFriend - will be removed
            Function.getClickedFriend(friendPhoto, friendName, null);
        });
    }

    /**
     * Create a friend request box UI component
     * @param requesterName Username of person who sent request
     * @param onAccept Callback when accept button clicked
     * @param onReject Callback when reject button clicked
     * @return BorderPane containing request info
     */
    public static BorderPane requestBox(String requesterName,
                                        EventHandler<MouseEvent> onAccept,
                                        EventHandler<MouseEvent> onReject) {
        String style = "-fx-background-color: #ffb700; -fx-border-color: #ffaa00; -fx-border-width: 1.5px";
        String style1 = "-fx-background-color: #ff6f00";
        String style2 = "-fx-background-color: #1c1b1b";

        BorderPane requestPane = createBorderPane(75, 237, style, null, null);

        Image image = returnPhoto(requesterName);
        Circle circle = createPhotoCircle(image, 21, 0);
        BorderPane.setAlignment(circle, Pos.CENTER);
        BorderPane.setMargin(circle, new Insets(10, 0, 0, 10));

        Label senderName = createLabel(0, 0, requesterName, 24, 158, 14, null, null, null);
        senderName.setPadding(new Insets(10, 0, 0, 0));
        BorderPane.setAlignment(senderName, Pos.CENTER_LEFT);
        BorderPane.setMargin(senderName, new Insets(0, 0, 0, 20));

        double[] array = {0, 0, 5, 0};
        HBox hBox = createHBox(0, 0, 38, 166, null, Pos.CENTER_LEFT, array);
        hBox.setSpacing(15);
        BorderPane.setMargin(hBox, new Insets(0, 0, 0, 55));
        BorderPane.setAlignment(hBox, Pos.CENTER);

        Button acceptButton = createButton(26, 65, "Accept", style1, Color.WHITE, onAccept, null);
        Button rejectButton = createButton(26, 59, "Reject", style2, Color.WHITE, onReject, null);
        hBox.getChildren().addAll(acceptButton, rejectButton);

        requestPane.setLeft(circle);
        requestPane.setCenter(senderName);
        requestPane.setBottom(hBox);

        return requestPane;
    }

    /**
     * Legacy version for backward compatibility
     * TODO: Remove after MainPanelController is fully modernized
     */
    @Deprecated
    public static BorderPane requestBox(String requesterName) {
        // Create default handlers that use old ServerFunctions
        EventHandler<MouseEvent> acceptHandler = event -> {
            String addedName = ServerFunctions.encodeURL(requesterName);
            String response = ServerFunctions.HTMLRequest(
                    ServerFunctions.serverURL + "/friends/accept",
                    "adder=" + ServerFunctions.encodeURL(GlobalVariables.getLoggedUser()) +
                            "&added=" + addedName
            );

            if (response.equals("addfriend successful")) {
                WarningWindowController.warningMessage("Friend added!");
            } else {
                WarningWindowController.warningMessage("Friend could not be added!");
            }
            Function.getFriendRequests();
            Function.getFriends();
        };

        EventHandler<MouseEvent> rejectHandler = event -> {
            String blockedName = ServerFunctions.encodeURL(requesterName);
            String response = ServerFunctions.HTMLRequest(
                    ServerFunctions.serverURL + "/friends/reject",
                    "blocker=" + ServerFunctions.encodeURL(GlobalVariables.getLoggedUser()) +
                            "&blockedUser=" + blockedName
            );

            if (response.equals("rejection successful")) {
                WarningWindowController.warningMessage("Request rejected!");
            } else {
                WarningWindowController.warningMessage("Request could not be rejected!");
            }
            Function.getFriendRequests();
        };

        return requestBox(requesterName, acceptHandler, rejectHandler);
    }

    /**
     * Create a user search result box
     * @param userName Username of found user
     * @param onAddClick Callback when add button clicked
     * @return HBox containing user info
     */
    public static HBox userBox(String userName, EventHandler<MouseEvent> onAddClick) {
        String backgroundStyle = "-fx-background-color: #ff7d1a; -fx-border-color: #ff6f00; -fx-border-width: 1.5px";
        String buttonStyle = "-fx-background-color: #1c1b1b";

        HBox hBox = createHBox(0, 0, 59, 237, backgroundStyle, Pos.CENTER_LEFT, null);

        Image photo = returnPhoto(userName);
        Circle userPhoto = createPhotoCircle(photo, 21, 0);
        HBox.setMargin(userPhoto, new Insets(0, 0, 0, 10));

        Label username = createLabel(0, 1.7976931348623157E308, userName, 20, 87, 14,
                Color.WHITE, null, Pos.CENTER);
        HBox.setMargin(username, new Insets(0, 10, 0, 10));

        Button addButton = createButton(26, 51, "+ Add", buttonStyle, Color.WHITE, onAddClick, null);
        HBox.setMargin(addButton, new Insets(0, 10, 0, 0));

        hBox.getChildren().addAll(userPhoto, username, addButton);

        return hBox;
    }

    /**
     * Legacy version for backward compatibility
     * TODO: Remove after search functionality is modernized
     */
    @Deprecated
    public static HBox userBox(String userName) {
        EventHandler<MouseEvent> addHandler = event -> {
            String receiverUser = ServerFunctions.encodeURL(userName);
            String response = ServerFunctions.HTMLRequest(
                    ServerFunctions.serverURL + "/friends/send-request",
                    "sender=" + ServerFunctions.encodeURL(GlobalVariables.getLoggedUser()) +
                            "&receiver=" + receiverUser
            );

            if (response.equals("already friends")) {
                WarningWindowController.warningMessage("You're already friends!");
            } else if (response.equals("request sent")) {
                WarningWindowController.warningMessage("New request sent");
            } else if (response.equals("already sent")) {
                WarningWindowController.warningMessage("You already sent request!");
            }
        };

        return userBox(userName, addHandler);
    }
}