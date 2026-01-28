package goksoft.chat.app.ui.components;

import goksoft.chat.app.config.Environment;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URI;

/**
 * Utility class for loading user profile photos from the server
 */
public class ProfilePhotoLoader {

    private static final Logger logger = LoggerFactory.getLogger(ProfilePhotoLoader.class);

    /**
     * Load profile photo for a given username
     * @param username The username to load photo for
     * @return JavaFX Image, or null if loading fails
     */
    public static Image loadPhoto(String username) {
        if (username == null || username.isEmpty()) {
            logger.warn("Cannot load photo: username is null or empty");
            return null;
        }

        try {
            String encodedUsername = java.net.URLEncoder.encode(
                    username,
                    java.nio.charset.StandardCharsets.UTF_8
            );
            String photoUrl = Environment.getServerUrl() + "/users/photo/" + encodedUsername;

            BufferedImage bufferedImage = ImageIO.read(new URI(photoUrl).toURL());
            if (bufferedImage != null) {
                return SwingFXUtils.toFXImage(bufferedImage, null);
            } else {
                logger.debug("No photo found for user: {}", username);
                return null;
            }
        } catch (IOException | java.net.URISyntaxException e) {
            logger.debug("Failed to load photo for user: {}", username);
            return null;
        }
    }
}