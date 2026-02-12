package goksoft.chat.app.controller.main;

import goksoft.chat.app.config.Environment;
import goksoft.chat.app.controller.auth.LoginController;
import goksoft.chat.app.controller.dialog.WarningWindowController;
import goksoft.chat.app.service.ServiceManager;
import goksoft.chat.app.ui.components.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.prefs.Preferences;

public class MainPanelController1 {

    private static final Logger logger = LoggerFactory.getLogger(MainPanelController1.class);

    // Layout structure
    @FXML private SplitPane splitPane;
    @FXML private BorderPane chatBorderPane;
    @FXML private BorderPane settingsBorderPane;
    @FXML private VBox chatEmptyState;

    // Sidebar
    @FXML public HBox operationsHBox;
    @FXML public VBox contentContainer;
    @FXML public VBox friendListPanel;
    @FXML public VBox notificationsPanel;
    @FXML public VBox addFriendListPanel;
    @FXML private VBox friendsVBox;
    @FXML private VBox notificationVBox;
    @FXML private VBox usersVBox;
    @FXML private TextField searchUserField;

    // Sidebar header
    @FXML private StackPane sidebarAvatar;
    @FXML private Label sidebarUsername;

    // Nav items
    @FXML private HBox chatsNavItem;
    @FXML private HBox requestsNavItem;
    @FXML private HBox findPeopleNavItem;

    // Nav badges
    @FXML private StackPane chatsBadge;
    @FXML private Label chatsBadgeLabel;
    @FXML private StackPane requestsBadge;
    @FXML private Label requestsBadgeLabel;
    @FXML private Label requestsSectionLabel;

    // Avatars
    @FXML private StackPane chatHeaderAvatar;
    @FXML private StackPane settingsAvatar;

    // Settings
    @FXML private Label settingsUsername;

    // Chat area
    @FXML public Label chatFriendName;
    @FXML public TextField messageField;
    @FXML public ListView<String> listView;

    // Empty state labels
    @FXML private Label noFriendLabel;
    @FXML private Label noNotifLabel;
    @FXML private Label noUserLabel;

    // Instance state
    private String currentFriend;
    private final ArrayList<String> friendsNameList = new ArrayList<>();
    private final ArrayList<String> friendRequestsNameList = new ArrayList<>();
    private final List<Object> friendArray = new ArrayList<>();
    private int currentTimer;

    // Services & scheduling
    private final ServiceManager serviceManager = ServiceManager.getInstance();
    private ScheduledExecutorService scheduler;
    private ScheduledExecutorService messagePollingScheduler;

    @FXML
    public void initialize() {
        noUserLabel.setPadding(new Insets(25, 0, 0, 0));

        // Load initial data
        loadFriends();
        loadFriendRequests();

        // Set up user info in sidebar header
        String currentUser = serviceManager.getCurrentUser();
        if (currentUser != null) {
            initAvatar(sidebarAvatar, currentUser, 18);
            initAvatar(settingsAvatar, currentUser, 44);
            settingsUsername.setText(currentUser);
            sidebarUsername.setText(currentUser);
        }

        // Enter key sends message
        messageField.setOnKeyReleased(event -> {
            if (event.getCode() == KeyCode.ENTER) sendMessage();
        });

        // Message bubble cell factory
        listView.setCellFactory(MessageBubbleFactory.create(
                currentUser != null ? currentUser : ""));

        // Initialize scheduler for polling
        scheduler = Executors.newScheduledThreadPool(2);
        startFriendStatsPolling();
        startFriendRequestsPolling();

        // Cleanup on close
        setupWindowCloseHandler();

        // Lock splitPane divider
        setupSplitPaneLock();

        // Set initial active nav
        setActiveNav("chats");
    }

    /**
     * Populate a StackPane with a gradient avatar (circle + initials).
     */
    private void initAvatar(StackPane container, String name, double radius) {
        if (container == null || name == null) return;
        container.getChildren().clear();
        StackPane avatar = AvatarFactory.create(name, radius);
        container.getChildren().add(avatar);
    }

    /**
     * Highlight the active nav item and deactivate others.
     */
    private void setActiveNav(String nav) {
        // Reset all
        chatsNavItem.getStyleClass().removeAll("nav-btn", "nav-btn-active");
        requestsNavItem.getStyleClass().removeAll("nav-btn", "nav-btn-active");
        findPeopleNavItem.getStyleClass().removeAll("nav-btn", "nav-btn-active");

        chatsNavItem.getStyleClass().add("chats".equals(nav) ? "nav-btn-active" : "nav-btn");
        requestsNavItem.getStyleClass().add("requests".equals(nav) ? "nav-btn-active" : "nav-btn");
        findPeopleNavItem.getStyleClass().add("search".equals(nav) ? "nav-btn-active" : "nav-btn");
    }

    /**
     * Update the nav badge counts.
     */
    private void updateChatsBadge(int count) {
        if (count > 0) {
            chatsBadge.setVisible(true);
            chatsBadge.setManaged(true);
            chatsBadgeLabel.setText(count > 99 ? "99+" : String.valueOf(count));
        } else {
            chatsBadge.setVisible(false);
            chatsBadge.setManaged(false);
        }
    }

    private void updateRequestsBadge(int count) {
        if (count > 0) {
            requestsBadge.setVisible(true);
            requestsBadge.setManaged(true);
            requestsBadgeLabel.setText(count > 99 ? "99+" : String.valueOf(count));
            requestsSectionLabel.setText("PENDING · " + count);
        } else {
            requestsBadge.setVisible(false);
            requestsBadge.setManaged(false);
            requestsSectionLabel.setText("PENDING · 0");
        }
    }

    private void setupWindowCloseHandler() {
        chatBorderPane.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.windowProperty().addListener((obsWindow, oldWindow, newWindow) -> {
                    if (newWindow != null) {
                        newWindow.setOnCloseRequest(event -> cleanup());
                    }
                });
            }
        });
    }

    private void setupSplitPaneLock() {
        var divider = splitPane.getDividers().getFirst();
        final double pos = divider.getPosition();
        divider.positionProperty().addListener(
                (observableValue, number, t1) -> divider.setPosition(pos)
        );
    }

    // ===== FRIEND MANAGEMENT =====

    private BorderPane createFriendBox(List<String> friendData) {
        String username = friendData.get(0);
        String notifCount = friendData.get(1);
        String lastMsg = friendData.get(2);
        String passedTime = friendData.get(3);
        Image photo = ProfilePhotoLoader.loadPhoto(username);

        return FriendBoxComponent.create(
                username, lastMsg, notifCount, passedTime, photo,
                () -> onFriendClicked(username)
        );
    }

    private void loadFriends() {
        friendsNameList.clear();
        friendArray.clear();
        friendsVBox.getChildren().clear();

        serviceManager.getFriendService().getFriendsWithDetails()
                .thenAccept(friendsList -> Platform.runLater(() -> {
                    int totalUnread = 0;
                    for (List<String> friendData : friendsList) {
                        if (friendData.size() >= 4) {
                            BorderPane friendBox = createFriendBox(friendData);
                            String username = friendData.getFirst();
                            friendArray.add(friendBox);
                            friendsNameList.add(username);
                            friendsVBox.getChildren().add(friendBox);

                            // Sum unread for badge
                            try {
                                totalUnread += Integer.parseInt(friendData.get(1));
                            } catch (NumberFormatException ignored) {}
                        }
                    }
                    updateChatsBadge(totalUnread);
                    checkNoResult(friendsList.isEmpty(), noFriendLabel);
                }))
                .exceptionally(ex -> {
                    logger.error("Error loading friends", ex);
                    Platform.runLater(() -> checkNoResult(true, noFriendLabel));
                    return null;
                });
    }

    private void loadFriendRequests() {
        serviceManager.getFriendService().getFriendRequests()
                .thenAccept(requests -> Platform.runLater(() -> {
                    notificationVBox.getChildren().clear();
                    friendRequestsNameList.clear();

                    for (String username : requests) {
                        Image photo = ProfilePhotoLoader.loadPhoto(username);
                        BorderPane requestBox = RequestBoxComponent.create(
                                username, photo,
                                event -> acceptFriendRequest(username),
                                event -> rejectFriendRequest(username)
                        );
                        notificationVBox.getChildren().addFirst(requestBox);
                        friendRequestsNameList.add(username);
                    }

                    updateRequestsBadge(requests.size());
                    checkNoResult(requests.isEmpty(), noNotifLabel);
                }))
                .exceptionally(ex -> {
                    logger.error("Error loading friend requests", ex);
                    Platform.runLater(() -> checkNoResult(true, noNotifLabel));
                    return null;
                });
    }

    private void acceptFriendRequest(String requester) {
        Node requestNode = notificationVBox.lookup("#" + requester);

        serviceManager.getFriendService().acceptFriendRequest(requester)
                .thenAccept(response -> Platform.runLater(() -> {
                    if (response.isSuccess()) {
                        if (requestNode != null) {
                            AnimationUtils.slideOutRight(requestNode, () -> {
                                loadFriendRequests();
                                loadFriends();
                            });
                        } else {
                            loadFriendRequests();
                            loadFriends();
                        }
                        WarningWindowController.warningMessage("Friend added!");
                    } else {
                        WarningWindowController.warningMessage(
                                "Could not add friend: " + response.getMessage());
                    }
                }))
                .exceptionally(ex -> {
                    logger.error("Error accepting friend request", ex);
                    Platform.runLater(() ->
                            WarningWindowController.warningMessage("Connection error"));
                    return null;
                });
    }

    private void rejectFriendRequest(String requester) {
        Node requestNode = notificationVBox.lookup("#" + requester);

        serviceManager.getFriendService().rejectFriendRequest(requester)
                .thenAccept(response -> Platform.runLater(() -> {
                    if (response.isSuccess()) {
                        if (requestNode != null) {
                            AnimationUtils.slideOutRight(requestNode, this::loadFriendRequests);
                        } else {
                            loadFriendRequests();
                        }
                        WarningWindowController.warningMessage("Request declined");
                    } else {
                        WarningWindowController.warningMessage(
                                "Could not decline request: " + response.getMessage());
                    }
                }))
                .exceptionally(ex -> {
                    logger.error("Error rejecting friend request", ex);
                    Platform.runLater(() ->
                            WarningWindowController.warningMessage("Connection error"));
                    return null;
                });
    }

    // ===== MESSAGING =====

    public void onFriendClicked(String friendName) {
        // Highlight selected friend in sidebar
        for (Node child : friendsVBox.getChildren()) {
            child.getStyleClass().remove("friend-item-selected");
            if (!child.getStyleClass().contains("friend-item")) {
                child.getStyleClass().add("friend-item");
            }
        }
        Node selected = friendsVBox.lookup("#" + friendName);
        if (selected != null) {
            selected.getStyleClass().remove("friend-item");
            selected.getStyleClass().add("friend-item-selected");
        }

        // Update chat header
        chatFriendName.setText(friendName);
        initAvatar(chatHeaderAvatar, friendName, 20);

        // Show chat, hide empty state and settings
        chatBorderPane.setVisible(true);
        if (chatEmptyState != null) chatEmptyState.setVisible(false);
        settingsBorderPane.setVisible(false);

        currentFriend = friendName;

        loadMessages();
        startMessagePollingForCurrentFriend();
    }

    private void loadMessages() {
        if (currentFriend == null) return;

        serviceManager.getMessageService().getMessages(currentFriend)
                .thenAccept(messages -> Platform.runLater(() -> {
                    listView.getItems().clear();
                    for (List<String> msgData : messages) {
                        if (msgData.size() >= 2) {
                            String sender = msgData.get(0);
                            String message = msgData.get(1);
                            // Use formatted message with timestamp if available
                            if (msgData.size() >= 3 && msgData.get(2) != null) {
                                listView.getItems().add(
                                        MessageBubbleFactory.formatMessage(sender, message, msgData.get(2)));
                            } else {
                                listView.getItems().add(sender + ": " + message);
                            }
                        }
                    }
                    if (!listView.getItems().isEmpty()) {
                        listView.scrollTo(listView.getItems().size() - 1);
                    }
                }))
                .exceptionally(ex -> {
                    logger.error("Error loading messages", ex);
                    return null;
                });
    }

    /** Called by Enter key in messageField */
    public void sendMessage() {
        if (currentFriend == null || messageField.getText().trim().isEmpty()) return;

        String message = messageField.getText();
        String loggedUser = serviceManager.getCurrentUser();

        // Optimistic UI update with timestamp
        listView.getItems().add(MessageBubbleFactory.formatMessage(loggedUser, message));
        listView.scrollTo(listView.getItems().size() - 1);
        messageField.clear();

        serviceManager.getMessageService().sendMessage(currentFriend, message)
                .thenAccept(response -> {
                    if (!response.isSuccess()) {
                        logger.warn("Message send reported failure: {}", response.getMessage());
                    }
                })
                .exceptionally(ex -> {
                    logger.error("Error sending message", ex);
                    Platform.runLater(() ->
                            WarningWindowController.warningMessage("Failed to send message"));
                    return null;
                });
    }

    /** Called by the send button in FXML */
    public void sendMessageButton(MouseEvent event) {
        sendMessage();
    }

    private void startMessagePollingForCurrentFriend() {
        if (messagePollingScheduler != null && !messagePollingScheduler.isShutdown()) {
            messagePollingScheduler.shutdown();
        }

        currentTimer = (int) (Math.random() * 1000);
        final int timerSnapshot = currentTimer;

        messagePollingScheduler = Executors.newSingleThreadScheduledExecutor();
        messagePollingScheduler.scheduleAtFixedRate(() -> {
            if (timerSnapshot != currentTimer || currentFriend == null) return;

            serviceManager.getMessageService().checkNotification(currentFriend)
                    .thenAccept(count -> {
                        if (count > 0) {
                            Platform.runLater(this::loadMessages);
                        }
                    })
                    .exceptionally(ex -> {
                        logger.error("Error checking notifications", ex);
                        return null;
                    });
        }, 1, 2, TimeUnit.SECONDS);
    }

    // ===== USER SEARCH =====

    public void searchUsers(KeyEvent event) {
        String searchTerm = searchUserField.getText();

        if (searchTerm.trim().isEmpty()) {
            usersVBox.getChildren().clear();
            checkNoResult(true, noUserLabel);
            return;
        }

        serviceManager.getUserService().searchUsers(searchTerm)
                .thenAccept(users -> Platform.runLater(() -> {
                    usersVBox.getChildren().clear();
                    for (String username : users) {
                        Image photo = ProfilePhotoLoader.loadPhoto(username);
                        HBox userBox = UserBoxComponent.create(
                                username, photo,
                                event2 -> sendFriendRequest(username)
                        );
                        usersVBox.getChildren().addFirst(userBox);
                    }
                    checkNoResult(users.isEmpty(), noUserLabel);
                }))
                .exceptionally(ex -> {
                    logger.error("Error searching users", ex);
                    Platform.runLater(() -> checkNoResult(true, noUserLabel));
                    return null;
                });
    }

    private void sendFriendRequest(String username) {
        serviceManager.getFriendService().sendFriendRequest(username)
                .thenAccept(response -> Platform.runLater(() -> {
                    if (response.isSuccess()) {
                        WarningWindowController.warningMessage("Friend request sent!");
                    } else {
                        WarningWindowController.warningMessage(response.getMessage());
                    }
                }))
                .exceptionally(ex -> {
                    logger.error("Error sending friend request", ex);
                    Platform.runLater(() ->
                            WarningWindowController.warningMessage("Connection error"));
                    return null;
                });
    }

    // ===== POLLING =====

    private void startFriendStatsPolling() {
        scheduler.scheduleAtFixedRate(() -> {
            try {
                serviceManager.getFriendService().getFriendsWithDetails()
                        .thenAccept(friendsList ->
                                Platform.runLater(() -> updateFriendsUI(friendsList)))
                        .exceptionally(ex -> {
                            logger.error("Error polling friend stats", ex);
                            return null;
                        });
            } catch (Exception e) {
                logger.error("Unexpected error in friend stats polling", e);
            }
        }, 0, Environment.MESSAGE_POLL_INTERVAL_MS, TimeUnit.MILLISECONDS);
    }

    private void startFriendRequestsPolling() {
        scheduler.scheduleAtFixedRate(() -> {
            try {
                serviceManager.getFriendService().getFriendRequests()
                        .thenAccept(requests ->
                                Platform.runLater(() -> updateFriendRequestsUI(requests)))
                        .exceptionally(ex -> {
                            logger.error("Error polling friend requests", ex);
                            return null;
                        });
            } catch (Exception e) {
                logger.error("Unexpected error in friend requests polling", e);
            }
        }, 0, Environment.FRIEND_REQUEST_POLL_INTERVAL_MS, TimeUnit.MILLISECONDS);
    }

    private void updateFriendsUI(List<List<String>> friendsList) {
        int index = 0;
        int totalUnread = 0;

        for (List<String> friendData : friendsList) {
            if (friendData.size() >= 4) {

                BorderPane friendBox = createFriendBox(friendData);
                String username = friendData.getFirst();

                try {
                    totalUnread += Integer.parseInt(friendData.get(1));
                } catch (NumberFormatException ignored) {}

                boolean found = false;
                for (int j = 0; j < friendsVBox.getChildren().size(); j++) {
                    Node child = friendsVBox.getChildren().get(j);
                    if (child.getId() != null && child.getId().equals(username)) {
                        friendsVBox.getChildren().remove(j);
                        friendsVBox.getChildren().add(index, friendBox);
                        index++;
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    friendsVBox.getChildren().add(index, friendBox);
                    index++;
                }
            }
        }

        updateChatsBadge(totalUnread);
    }

    private void updateFriendRequestsUI(List<String> requests) {
        for (String username : requests) {
            if (!friendRequestsNameList.contains(username)) {
                Image photo = ProfilePhotoLoader.loadPhoto(username);
                BorderPane requestBox = RequestBoxComponent.create(
                        username, photo,
                        event -> acceptFriendRequest(username),
                        event -> rejectFriendRequest(username)
                );

                notificationVBox.getChildren().addFirst(requestBox);
                friendRequestsNameList.add(username);
                logger.info("New friend request from: {}", username);
            }
        }
        updateRequestsBadge(friendRequestsNameList.size());
    }

    // ===== UI HELPERS =====

    private void checkNoResult(boolean isEmpty, Label label) {
        label.setManaged(isEmpty);
        label.setVisible(isEmpty);
    }

    // ===== UI EVENT HANDLERS =====

    public Stage getStage() {
        return (Stage) chatBorderPane.getScene().getWindow();
    }

    public void showChatsPanel(MouseEvent event) {
        setActiveNav("chats");
        switchSidebarPanel(friendListPanel);
    }

    public void showUserSearchPanel(MouseEvent event) {
        setActiveNav("search");
        switchSidebarPanel(addFriendListPanel);
    }

    public void showNotificationsPanel(MouseEvent event) {
        setActiveNav("requests");
        switchSidebarPanel(notificationsPanel);
    }

    private void switchSidebarPanel(VBox target) {
        VBox[] panels = { friendListPanel, notificationsPanel, addFriendListPanel };
        for (VBox panel : panels) {
            boolean show = (panel == target);
            panel.setVisible(show);
            panel.setManaged(show);
        }
        if (target == friendListPanel) {
            getStage().setTitle("Chat");
        } else if (target == notificationsPanel) {
            getStage().setTitle("Requests");
        } else if (target == addFriendListPanel) {
            getStage().setTitle("Find People");
        }
    }

    public void toggleSettingsPanel(MouseEvent event) {
        if (!settingsBorderPane.isVisible()) {
            settingsBorderPane.setVisible(true);
            chatBorderPane.setVisible(false);
            if (chatEmptyState != null) chatEmptyState.setVisible(false);
            getStage().setTitle("Settings");
        } else {
            settingsBorderPane.setVisible(false);
            if (currentFriend != null) {
                chatBorderPane.setVisible(true);
            } else if (chatEmptyState != null) {
                chatEmptyState.setVisible(true);
            }
            getStage().setTitle("Chat");
        }
    }

    public void logOff(MouseEvent event) {
        cleanup();
        serviceManager.clearCurrentUser();
        try {
            FXMLLoader loader = new FXMLLoader(
                    MainPanelController1.class.getResource("../../view/auth/login.fxml")
            );
            Parent loginPanel = loader.load();
            Scene scene = new Scene(loginPanel);
            Stage window = (Stage) chatBorderPane.getScene().getWindow();
            window.close();
            Stage newWindow = new Stage();
            newWindow.setScene(scene);
            newWindow.setResizable(false);
            newWindow.setFullScreen(false);
            newWindow.setTitle("Login");
            newWindow.show();

            Preferences prefs = Preferences.userNodeForPackage(LoginController.class);
            prefs.putBoolean("rememberMe", false);
            prefs.remove("username");

            newWindow.setOnCloseRequest(windowEvent -> System.exit(0));
        } catch (IOException e) {
            logger.error("Failed to load login panel", e);
        }
    }

    // ===== CLEANUP =====

    public void cleanup() {
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
            try {
                if (!scheduler.awaitTermination(2, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
                logger.info("Main scheduler shut down successfully");
            } catch (InterruptedException e) {
                scheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }

        if (messagePollingScheduler != null && !messagePollingScheduler.isShutdown()) {
            messagePollingScheduler.shutdown();
            try {
                if (!messagePollingScheduler.awaitTermination(2, TimeUnit.SECONDS)) {
                    messagePollingScheduler.shutdownNow();
                }
                logger.info("Message polling scheduler shut down successfully");
            } catch (InterruptedException e) {
                messagePollingScheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }
}
