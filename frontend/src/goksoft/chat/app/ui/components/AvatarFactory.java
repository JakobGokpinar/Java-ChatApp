package goksoft.chat.app.ui.components;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * Creates unique gradient avatars with initials for each user.
 * <p>
 * Each username produces a deterministic gradient pair so the same user
 * always gets the same color. Replaces the old default-icon approach
 * where every user had the same gray circle.
 * <p>
 * Usage:
 *   StackPane avatar = AvatarFactory.create("Emma Chen", 22);
 *   sidebar.getChildren().add(avatar);
 */
public class AvatarFactory {

    /**
     * Gradient color pairs — each pair produces a distinct diagonal gradient.
     * Matched from the React prototype's color palette.
     */
    private static final String[][] GRADIENT_PAIRS = {
            {"#5B7FFF", "#8B5CF6"},  // indigo → purple
            {"#FF6B8A", "#F472B6"},  // coral → pink
            {"#4ADE80", "#38BDF8"},  // green → cyan
            {"#FBBF24", "#FB923C"},  // amber → orange
            {"#A78BFA", "#5B7FFF"},  // violet → indigo
            {"#F472B6", "#FF6B8A"},  // pink → coral
            {"#38BDF8", "#4ADE80"},  // cyan → green
            {"#FB923C", "#FBBF24"},  // orange → amber
    };

    /**
     * Status dot colors.
     */
    private static final Color STATUS_ONLINE = Color.web("#4ADE80");
    private static final Color STATUS_AWAY = Color.web("#FBBF24");
    private static final Color STATUS_OFFLINE = Color.web("#55556A");

    private AvatarFactory() {
    }

    /**
     * Create a gradient avatar with initials.
     *
     * @param name   Full display name (e.g. "Emma Chen")
     * @param radius Circle radius in pixels
     * @return StackPane containing the gradient circle + initials label
     */
    public static StackPane create(String name, double radius) {
        return create(name, radius, null);
    }

    /**
     * Create a gradient avatar with initials and optional status dot.
     *
     * @param name   Full display name
     * @param radius Circle radius
     * @param status "online", "away", "offline", or null for no status
     * @return StackPane containing avatar layers
     */
    public static StackPane create(String name, double radius, String status) {
        StackPane container = new StackPane();
        container.setMinSize(radius * 2, radius * 2);
        container.setMaxSize(radius * 2, radius * 2);
        container.setPrefSize(radius * 2, radius * 2);

        // — Gradient circle —
        Circle circle = new Circle(radius);
        circle.setFill(getGradient(name));

        // — Initials —
        Label initials = new Label(getInitials(name));
        initials.setTextFill(Color.WHITE);
        initials.setFont(Font.font("System", FontWeight.SEMI_BOLD, radius * 0.75));
        initials.setMouseTransparent(true);

        container.getChildren().addAll(circle, initials);
        StackPane.setAlignment(initials, Pos.CENTER);

        // — Status dot —
        if (status != null && !status.isEmpty()) {
            Circle dot = createStatusDot(status, radius);
            container.getChildren().add(dot);
            StackPane.setAlignment(dot, Pos.BOTTOM_RIGHT);
            StackPane.setMargin(dot, new Insets(0, 1, 1, 0));
        }

        return container;
    }

    /**
     * Get a deterministic gradient for a username.
     */
    public static LinearGradient getGradient(String name) {
        if (name == null || name.isBlank()) name = "?";
        int hash = 0;
        for (char c : name.toCharArray()) {
            hash += c;
        }
        int idx = Math.abs(hash) % GRADIENT_PAIRS.length;

        Color start = Color.web(GRADIENT_PAIRS[idx][0]);
        Color end = Color.web(GRADIENT_PAIRS[idx][1]);

        return new LinearGradient(
                0, 0, 1, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, start),
                new Stop(1, end)
        );
    }

    /**
     * Extract 1–2 character initials from a name.
     * "Emma Chen" → "EC", "jakobg" → "JA"
     */
    public static String getInitials(String name) {
        if (name == null || name.isBlank()) return "?";

        String[] parts = name.trim().split("\\s+");
        if (parts.length >= 2) {
            return ("" + parts[0].charAt(0) + parts[parts.length - 1].charAt(0)).toUpperCase();
        } else {
            String single = parts[0];
            if (single.length() >= 2) {
                return single.substring(0, 2).toUpperCase();
            }
            return single.substring(0, 1).toUpperCase();
        }
    }

    /**
     * Create a small status indicator dot.
     */
    private static Circle createStatusDot(String status, double avatarRadius) {
        double dotRadius = Math.max(avatarRadius * 0.2, 4);

        Circle dot = new Circle(dotRadius);
        dot.setFill(switch (status.toLowerCase()) {
            case "online" -> STATUS_ONLINE;
            case "away" -> STATUS_AWAY;
            default -> STATUS_OFFLINE;
        });

        // Border ring (matches panel background)
        dot.setStroke(Color.web("#141418"));
        dot.setStrokeWidth(2);

        dot.setMouseTransparent(true);
        return dot;
    }

    /**
     * Apply a gradient fill directly to an existing Circle node.
     * Useful for updating @FXML Circle elements like settingsButton and profilePhoto.
     */
    public static void applyGradient(Circle circle, String name) {
        circle.setFill(getGradient(name));
    }
}
