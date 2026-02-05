package goksoft.chat.app.util;

import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * Utility class for common UI operations
 */
public class UIUtil {
    /**
     * Open or close sections with animation
     *
     * @param isOpen           Current open state
     * @param targetSection    Section to toggle
     * @param contentContainer Container VBox
     * @param friendListPanel  Friend list section
     * @param otherSection     Other section to manage
     * @param stage            Current stage
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
                    targetSection.getId().equals("addFriendListPanel");

            // Reorganize sections
            if (isMailbox && !contentContainer.getChildren().get(1).getId().equals("addFriendListPanel")) {
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