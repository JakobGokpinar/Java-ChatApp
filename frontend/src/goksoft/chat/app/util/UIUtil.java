package goksoft.chat.app.util;

import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.scene.Node;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * Utility class for common UI operations
 */
public class UIUtil {

    /**
     * Apply drop shadow effect to a UI component
     * @param shadowColor Color of the shadow
     * @param spread Spread of the shadow
     * @param duration1 Duration of first keyframe
     * @param duration2 Duration of second keyframe
     * @param cycleCount Number of cycles
     * @param node Target node
     */
    public static void dropShadowEffect(Color shadowColor, double spread,
                                        int duration1, int duration2,
                                        int cycleCount, Node node) {
        DropShadow shadow = new DropShadow();
        shadow.setColor(shadowColor);
        shadow.setSpread(spread);

        Timeline shadowAnimation = new Timeline(
                new KeyFrame(Duration.millis(1000 * duration1),
                        new KeyValue(shadow.radiusProperty(), 0d)),
                new KeyFrame(Duration.millis(1000 * duration2),
                        new KeyValue(shadow.radiusProperty(), 20d))
        );
        shadowAnimation.setCycleCount(cycleCount);

        node.setEffect(shadow);
        shadowAnimation.setOnFinished(evt -> node.setEffect(null));
        shadowAnimation.play();
    }

    /**
     * Open or close sections with animation
     * @param isOpen Current open state
     * @param targetSection Section to toggle
     * @param contentContainer Container VBox
     * @param friendListPanel Friend list section
     * @param otherSection Other section to manage
     * @param stage Current stage
     */
    public static void openAndCloseSections(boolean isOpen, VBox targetSection,
                                            VBox contentContainer, VBox friendListPanel,
                                            VBox otherSection, Stage stage) {
        if (!isOpen) {
            Timeline timeline = new Timeline();

            // Determine which section we're opening
            boolean isMailbox = targetSection.getId() != null &&
                    targetSection.getId().equals("notificationsPanel");
            boolean isAddFriend = targetSection.getId() != null &&
                    targetSection.getId().equals("addfriendListPanel");

            // Reorganize sections
            if (isMailbox && !contentContainer.getChildren().get(1).getId().equals("addfriendListPanel")) {
                contentContainer.getChildren().remove(otherSection);
                contentContainer.getChildren().add(1, otherSection);
                timeline.setOnFinished(actionEvent -> {
                    stage.setTitle("Mailbox");
                    otherSection.setVisible(false);
                    friendListPanel.setVisible(false);
                });
            } else if (isAddFriend && !contentContainer.getChildren().get(1).getId().equals("notificationsPanel")) {
                contentContainer.getChildren().remove(otherSection);
                contentContainer.getChildren().add(1, otherSection);
                timeline.setOnFinished(actionEvent -> {
                    stage.setTitle("Add Friend");
                    otherSection.setVisible(false);
                    friendListPanel.setVisible(false);
                });
            }

            // Animate opening
            friendListPanel.setManaged(false);
            otherSection.setManaged(false);
            targetSection.setManaged(true);
            targetSection.setVisible(true);
            targetSection.translateYProperty().set(targetSection.getHeight());

            KeyValue kv = new KeyValue(targetSection.translateYProperty(), 0, Interpolator.EASE_IN);
            KeyFrame kf = new KeyFrame(Duration.seconds(1), kv);
            timeline.getKeyFrames().add(kf);
            timeline.play();
        } else {
            // Close section and return to friend list
            stage.setTitle("Chat");
            targetSection.setVisible(false);
            targetSection.setManaged(false);
            friendListPanel.setVisible(true);
            friendListPanel.setManaged(true);
        }
    }
}