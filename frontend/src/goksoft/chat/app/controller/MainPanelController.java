package goksoft.chat.app.controller;

import goksoft.chat.app.Function;
import goksoft.chat.app.util.ExecutorManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.layout.BorderPane;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static goksoft.chat.app.Function.friendsVBox;

public class MainPanelController {
    private final ServiceManager services = ServiceManager.getInstance();

    @FXML
    public void initialize() {
        loadFriends();
        loadFriendRequests();
        startMessagePolling();
    }

    private void loadFriends() {
        services.getFriendService().getFriends()
                .thenAcceptAsync(friends -> {
                    Platform.runLater(() -> {
                        friendsVBox.getChildren().clear();
                        friends.forEach(friend -> {
                            BorderPane friendBox = GUIComponents.friendBox(
                                    friend.getUsername(),
                                    friend.getLastMessage(),
                                    String.valueOf(friend.getUnreadCount()),
                                    friend.getLastMessageTime()
                            );
                            friendsVBox.getChildren().add(friendBox);
                        });
                    });
                })
                .exceptionally(ex -> {
                    Platform.runLater(() -> showError("Failed to load friends"));
                    return null;
                });
    }

    private void startMessagePolling() {
        ScheduledExecutorService scheduler = ExecutorManager.getScheduledExecutor();
        scheduler.scheduleAtFixedRate(() -> {
            if (Function.currentFriend != null) {
                refreshMessages();
            }
        }, 0, 2, TimeUnit.SECONDS);
    }
}
