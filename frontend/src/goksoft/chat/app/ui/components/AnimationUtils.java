package goksoft.chat.app.ui.components;

import javafx.animation.*;
import javafx.scene.Node;
import javafx.util.Duration;

/**
 * Reusable animation utilities for smooth UI transitions.
 *
 * Animations:
 *   fadeSlideIn    — panel entrance (fade + slide up)
 *   fadeSlideOut   — panel exit (fade + slide down)
 *   slideOutRight  — request card dismissal (slide right + fade)
 *   scaleBounce    — micro-interaction (send button, badge pop)
 *   fadeIn / fadeOut — simple opacity transitions
 */
public class AnimationUtils {

    /** Spring-like interpolator for natural-feeling animations */
    private static final Interpolator SPRING = Interpolator.SPLINE(0.34, 1.56, 0.64, 1.0);

    /** Standard ease-out for exits */
    private static final Interpolator EASE_OUT = Interpolator.SPLINE(0.0, 0.0, 0.2, 1.0);

    private AnimationUtils() {
    }

    /**
     * Fade + slide up entrance animation.
     * Use for panel switches (Chats → Requests → Find People).
     */
    public static void fadeSlideIn(Node node, double durationMs) {
        node.setOpacity(0);
        node.setTranslateY(12);
        node.setVisible(true);
        node.setManaged(true);

        TranslateTransition slide = new TranslateTransition(Duration.millis(durationMs), node);
        slide.setFromY(12);
        slide.setToY(0);
        slide.setInterpolator(SPRING);

        FadeTransition fade = new FadeTransition(Duration.millis(durationMs), node);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.setInterpolator(EASE_OUT);

        new ParallelTransition(slide, fade).play();
    }

    /**
     * Fade + slide up with default 300ms duration.
     */
    public static void fadeSlideIn(Node node) {
        fadeSlideIn(node, 300);
    }

    /**
     * Fade + slide down exit animation.
     */
    public static void fadeSlideOut(Node node, Runnable onComplete) {
        TranslateTransition slide = new TranslateTransition(Duration.millis(200), node);
        slide.setFromY(0);
        slide.setToY(8);
        slide.setInterpolator(EASE_OUT);

        FadeTransition fade = new FadeTransition(Duration.millis(200), node);
        fade.setFromValue(1);
        fade.setToValue(0);
        fade.setInterpolator(EASE_OUT);

        ParallelTransition pt = new ParallelTransition(slide, fade);
        pt.setOnFinished(e -> {
            node.setVisible(false);
            node.setManaged(false);
            node.setTranslateY(0);
            node.setOpacity(1);
            if (onComplete != null) onComplete.run();
        });
        pt.play();
    }

    /**
     * Slide right + fade out — for dismissing request cards.
     */
    public static void slideOutRight(Node node, Runnable onComplete) {
        TranslateTransition slide = new TranslateTransition(Duration.millis(300), node);
        slide.setFromX(0);
        slide.setToX(60);
        slide.setInterpolator(EASE_OUT);

        FadeTransition fade = new FadeTransition(Duration.millis(300), node);
        fade.setFromValue(1);
        fade.setToValue(0);
        fade.setInterpolator(EASE_OUT);

        ParallelTransition pt = new ParallelTransition(slide, fade);
        pt.setOnFinished(e -> {
            node.setManaged(false);
            node.setVisible(false);
            node.setTranslateX(0);
            node.setOpacity(1);
            if (onComplete != null) onComplete.run();
        });
        pt.play();
    }

    /**
     * Scale bounce — micro-interaction for buttons, badges, new messages.
     */
    public static void scaleBounce(Node node) {
        ScaleTransition st = new ScaleTransition(Duration.millis(250), node);
        st.setFromX(0.85);
        st.setFromY(0.85);
        st.setToX(1.0);
        st.setToY(1.0);
        st.setInterpolator(SPRING);
        st.play();
    }

    /**
     * Simple fade in.
     */
    public static void fadeIn(Node node, double durationMs) {
        node.setOpacity(0);
        node.setVisible(true);
        node.setManaged(true);

        FadeTransition ft = new FadeTransition(Duration.millis(durationMs), node);
        ft.setFromValue(0);
        ft.setToValue(1);
        ft.setInterpolator(EASE_OUT);
        ft.play();
    }

    /**
     * Simple fade out.
     */
    public static void fadeOut(Node node, double durationMs, Runnable onComplete) {
        FadeTransition ft = new FadeTransition(Duration.millis(durationMs), node);
        ft.setFromValue(1);
        ft.setToValue(0);
        ft.setInterpolator(EASE_OUT);
        ft.setOnFinished(e -> {
            node.setVisible(false);
            node.setManaged(false);
            node.setOpacity(1);
            if (onComplete != null) onComplete.run();
        });
        ft.play();
    }
}
