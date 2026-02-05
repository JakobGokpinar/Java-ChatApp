package goksoft.chat.app.ui.components;

import javafx.scene.image.Image;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;

public class AvatarComponent {

    public static Circle createAvatar(double radius, Image photo) {
        Circle avatar = new Circle(radius);
        avatar.setStrokeWidth(0);
        if (photo != null && !photo.isError()) {
            avatar.setFill(new ImagePattern(photo));
        } else {
            avatar.getStyleClass().add("profile-circle-default");
        }
        return avatar;
    }
}
