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
            // Determine title
            String title = "Chat";
            if (targetSection.getId() != null) {
                if (targetSection.getId().equals("notificationsPanel")) title = "Requests";
                else if (targetSection.getId().equals("addFriendListPanel")) title = "Find People";
            }
            final String finalTitle = title;

            // Hide other panels, show target
            friendListPanel.setVisible(false);
            friendListPanel.setManaged(false);
            otherSection.setVisible(false);
            otherSection.setManaged(false);

            targetSection.setManaged(true);
            targetSection.setVisible(true);
            targetSection.setTranslateY(targetSection.getHeight());

            Timeline timeline = new Timeline();
            KeyValue kv = new KeyValue(targetSection.translateYProperty(), 0, Interpolator.EASE_IN);
            KeyFrame kf = new KeyFrame(Duration.seconds(0.3), kv);
            timeline.getKeyFrames().add(kf);
            timeline.setOnFinished(e -> stage.setTitle(finalTitle));
            timeline.play();
        } else {
            // Close and return to friend list
            stage.setTitle("Chat");
            targetSection.setVisible(false);
            targetSection.setManaged(false);
            friendListPanel.setVisible(true);
            friendListPanel.setManaged(true);
        }
    }
}