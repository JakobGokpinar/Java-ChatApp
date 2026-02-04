package goksoft.chat.app.controller.main;

import goksoft.chat.app.config.Environment;
import goksoft.chat.app.controller.dialog.WarningWindowController;
import goksoft.chat.app.controller.auth.LoginController;
import goksoft.chat.app.service.ServiceManager;
import goksoft.chat.app.ui.components.FriendBoxComponent;
import goksoft.chat.app.ui.components.ProfilePhotoLoader;
import goksoft.chat.app.ui.components.RequestBoxComponent;
import goksoft.chat.app.ui.components.UserBoxComponent;
import goksoft.chat.app.util.UIUtil;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
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
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.prefs.Preferences;

public class MainPanelController {

    private static final Logger logger = LoggerFactory.getLogger(MainPanelController.class);

    // Layout structure
    @FXML private SplitPane splitPane;
    @FXML private BorderPane chatBorderPane;
    @FXML private BorderPane settingsBorderPane;
    @FXML private VBox chatEmptyState;

    // Sidebar
    @FXML private TextField searchFriendField;
    @FXML private ScrollPane friendScrollPane;
    @FXML public HBox operationsHBox;
    @FXML public VBox contentContainer;
    @FXML public VBox friendListPanel;
    @FXML public VBox notificationsPanel;
    @FXML public VBox addfriendListPanel;
    @FXML private VBox friendsVBox;
    @FXML private VBox notificationVBox;
    @FXML private VBox usersVBox;
    @FXML private TextField searchUserField;
    @FXML public Button mailboxButton;

    // Profile & Settings
    @FXML public Circle profilePhoto;
    @FXML public Circle settingsButton;
    @FXML private VBox settingsTopVBox;
    @FXML private Label settingsUsername;

    // Chat area
    @FXML public Circle chatFriendProfilePhoto;
    @FXML public Label chatFriendName;
    @FXML public TextField messageField;
    @FXML public ListView<String> listView;

    // Empty state labels
    @FXML private Label noFriendLabel;
    @FXML private Label noNotifLabel;
    @FXML private Label noUserLabel;

    // Instance state
    private String currentFriend;
    private BorderPane currentPane;
    private ArrayList<String> friendsNameList = new ArrayList<>();
    private ArrayList<String> friendRequestsNameList = new ArrayList<>();
    private List<Object> friendArray = new ArrayList<>();
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
        loadProfilePhoto(false);
        loadFriendRequests();

        settingsUsername.setText(serviceManager.getCurrentUser());

        // Enter key sends message
        messageField.setOnKeyReleased(event -> {
            if (event.getCode() == KeyCode.ENTER) sendMessage();
        });

        // Initialize scheduler for polling
        scheduler = Executors.newScheduledThreadPool(2);
        startFriendStatsPolling();
        startFriendRequestsPolling();

        // Cleanup on close
        setupWindowCloseHandler();

        // Lock splitPane divider
        setupSplitPaneLock();
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
        final double pos = splitPane.getDividers().get(0).getPosition();
        splitPane.getDividers().get(0).positionProperty().addListener(
                (observableValue, number, t1) ->
                        splitPane.getDividers().get(0).setPosition(pos)
        );
    }

    // ===== FRIEND MANAGEMENT =====

    private void loadFriends() {
        friendsNameList.clear();
        friendArray.clear();
        friendsVBox.getChildren().clear();

        serviceManager.getFriendService().getFriendsWithDetails()
                .thenAccept(friendsList -> {
                    Platform.runLater(() -> {
                        for (List<String> friendData : friendsList) {
                            if (friendData.size() >= 4) {
                                String username = friendData.get(0);
                                String notifCount = friendData.get(1);
                                String lastMsg = friendData.get(2);
                                String passedTime = friendData.get(3);

                                Image photo = ProfilePhotoLoader.loadPhoto(username);

                                BorderPane friendBox = FriendBoxComponent.create(
                                        username, lastMsg, notifCount, passedTime, photo,
                                        () -> {
                                            Image friendPhoto = ProfilePhotoLoader.loadPhoto(username);
                                            BorderPane actualPane = findPaneById(friendsVBox, username);
                                            onFriendClicked(friendPhoto, username, actualPane);
                                        }
                                );

                                friendArray.add(friendBox);
                                friendsNameList.add(username);
                                friendsVBox.getChildren().add(friendBox);
                            }
                        }
                        checkNoResult(friendsList.isEmpty(), noFriendLabel);
                    });
                })
                .exceptionally(ex -> {
                    logger.error("Error loading friends", ex);
                    Platform.runLater(() -> checkNoResult(true, noFriendLabel));
                    return null;
                });
    }

    private void loadFriendRequests() {
        serviceManager.getFriendService().getFriendRequests()
                .thenAccept(requests -> {
                    Platform.runLater(() -> {
                        notificationVBox.getChildren().clear();
                        friendRequestsNameList.clear();

                        for (String username : requests) {
                            Image photo = ProfilePhotoLoader.loadPhoto(username);
                            BorderPane requestBox = RequestBoxComponent.create(
                                    username, photo,
                                    event -> acceptFriendRequest(username),
                                    event -> rejectFriendRequest(username)
                            );
                            notificationVBox.getChildren().add(0, requestBox);
                            friendRequestsNameList.add(username);
                        }

                        // Subtle highlight on requests button if there are pending requests
                        if (!friendRequestsNameList.isEmpty()) {
                            mailboxButton.getStyleClass().remove("nav-btn");
                            mailboxButton.getStyleClass().add("nav-btn-active");
                        }

                        checkNoResult(requests.isEmpty(), noNotifLabel);
                    });
                })
                .exceptionally(ex -> {
                    logger.error("Error loading friend requests", ex);
                    Platform.runLater(() -> checkNoResult(true, noNotifLabel));
                    return null;
                });
    }

    private void acceptFriendRequest(String requester) {
        serviceManager.getFriendService().acceptFriendRequest(requester)
                .thenAccept(response -> {
                    Platform.runLater(() -> {
                        if (response.isSuccess()) {
                            WarningWindowController.warningMessage("Friend added!");
                            loadFriendRequests();
                            loadFriends();
                        } else {
                            WarningWindowController.warningMessage(
                                    "Could not add friend: " + response.getMessage());
                        }
                    });
                })
                .exceptionally(ex -> {
                    logger.error("Error accepting friend request", ex);
                    Platform.runLater(() ->
                            WarningWindowController.warningMessage("Connection error"));
                    return null;
                });
    }

    private void rejectFriendRequest(String requester) {
        serviceManager.getFriendService().rejectFriendRequest(requester)
                .thenAccept(response -> {
                    Platform.runLater(() -> {
                        if (response.isSuccess()) {
                            WarningWindowController.warningMessage("Request declined");
                            loadFriendRequests();
                        } else {
                            WarningWindowController.warningMessage(
                                    "Could not decline request: " + response.getMessage());
                        }
                    });
                })
                .exceptionally(ex -> {
                    logger.error("Error rejecting friend request", ex);
                    Platform.runLater(() ->
                            WarningWindowController.warningMessage("Connection error"));
                    return null;
                });
    }

    public void searchFriend(KeyEvent event) {
        String searchTerm = searchFriendField.getText().toLowerCase();
        friendsVBox.getChildren().clear();

        for (int i = 0; i < friendsNameList.size(); i++) {
            if (friendsNameList.get(i).toLowerCase().contains(searchTerm)) {
                friendsVBox.getChildren().add((Node) friendArray.get(i));
            }
        }
    }

    // ===== MESSAGING =====

    public void onFriendClicked(Image friendPhoto, String friendName, BorderPane pane) {
        chatFriendName.setText(friendName);
        if (friendPhoto != null && !friendPhoto.isError()) {
            chatFriendProfilePhoto.setFill(new ImagePattern(friendPhoto));
        }
        chatFriendProfilePhoto.setStrokeWidth(0);

        // Show chat, hide empty state and settings
        chatBorderPane.setVisible(true);
        if (chatEmptyState != null) chatEmptyState.setVisible(false);
        settingsBorderPane.setVisible(false);

        currentFriend = friendName;
        currentPane = pane;

        loadMessages();
        startMessagePollingForCurrentFriend();
    }

    private void loadMessages() {
        if (currentFriend == null) return;

        serviceManager.getMessageService().getMessages(currentFriend)
                .thenAccept(messages -> {
                    Platform.runLater(() -> {
                        listView.getItems().clear();
                        for (List<String> msgData : messages) {
                            if (msgData.size() >= 2) {
                                String sender = msgData.get(0);
                                String message = msgData.get(1);
                                listView.getItems().add(sender + ": " + message);
                            }
                        }
                        if (!listView.getItems().isEmpty()) {
                            listView.scrollTo(listView.getItems().size() - 1);
                        }
                    });
                })
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

        // Optimistic UI update
        listView.getItems().add(loggedUser + ": " + message);
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
                .thenAccept(users -> {
                    Platform.runLater(() -> {
                        usersVBox.getChildren().clear();
                        for (String username : users) {
                            Image photo = ProfilePhotoLoader.loadPhoto(username);
                            HBox userBox = UserBoxComponent.create(
                                    username, photo,
                                    event2 -> sendFriendRequest(username)
                            );
                            usersVBox.getChildren().add(0, userBox);
                        }
                        checkNoResult(users.isEmpty(), noUserLabel);
                    });
                })
                .exceptionally(ex -> {
                    logger.error("Error searching users", ex);
                    Platform.runLater(() -> checkNoResult(true, noUserLabel));
                    return null;
                });
    }

    private void sendFriendRequest(String username) {
        serviceManager.getFriendService().sendFriendRequest(username)
                .thenAccept(response -> {
                    Platform.runLater(() -> {
                        if (response.isSuccess()) {
                            WarningWindowController.warningMessage("Friend request sent!");
                        } else {
                            WarningWindowController.warningMessage(response.getMessage());
                        }
                    });
                })
                .exceptionally(ex -> {
                    logger.error("Error sending friend request", ex);
                    Platform.runLater(() ->
                            WarningWindowController.warningMessage("Connection error"));
                    return null;
                });
    }

    // ===== PROFILE PHOTO =====

    private void loadProfilePhoto(boolean showHoverState) {
        Image image = ProfilePhotoLoader.loadPhoto(serviceManager.getCurrentUser());

        Platform.runLater(() -> {
            if (image != null && !image.isError()) {
                profilePhoto.setFill(new ImagePattern(image));
                settingsButton.setFill(new ImagePattern(image));
            } else {
                profilePhoto.getStyleClass().add("profile-circle-default");
                settingsButton.getStyleClass().add("profile-circle-default");
            }

            if (showHoverState) {
                profilePhoto.setFill(Color.web("#48484C"));
                Tooltip.install(profilePhoto, new Tooltip("Change Profile Photo"));
            }
        });
    }

    public void changeProfilePhoto(MouseEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Profile Photo");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
        );

        Stage stage = (Stage) profilePhoto.getScene().getWindow();
        File file = fileChooser.showOpenDialog(stage);

        if (file != null) {
            logger.info("Selected file: {}", file.getAbsolutePath());
            WarningWindowController.warningMessage("Photo upload coming soon!");
        }
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
        for (List<String> friendData : friendsList) {
            if (friendData.size() >= 4) {
                String username = friendData.get(0);
                String notifCount = friendData.get(1);
                String lastMsg = friendData.get(2);
                String passedTime = friendData.get(3);
                Image photo = ProfilePhotoLoader.loadPhoto(username);

                BorderPane friendBox = FriendBoxComponent.create(
                        username, lastMsg, notifCount, passedTime, photo,
                        () -> {
                            Image friendPhoto = ProfilePhotoLoader.loadPhoto(username);
                            BorderPane actualPane = findPaneById(friendsVBox, username);
                            onFriendClicked(friendPhoto, username, actualPane);
                        }
                );

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

                notificationVBox.getChildren().add(0, requestBox);
                friendRequestsNameList.add(username);
                mailboxButton.getStyleClass().remove("nav-btn");
                mailboxButton.getStyleClass().add("nav-btn-active");
                logger.info("New friend request from: {}", username);
            }
        }
    }

    // ===== UI HELPERS =====

    private void checkNoResult(boolean isEmpty, Label label) {
        label.setManaged(isEmpty);
        label.setVisible(isEmpty);
    }

    /** Find a child BorderPane by its fx:id within a container */
    private BorderPane findPaneById(VBox container, String id) {
        for (Node child : container.getChildren()) {
            if (child.getId() != null && child.getId().equals(id) && child instanceof BorderPane) {
                return (BorderPane) child;
            }
        }
        return null;
    }

    // ===== UI EVENT HANDLERS =====

    public Stage getStage() {
        return (Stage) chatBorderPane.getScene().getWindow();
    }

    public void showUserSearchPanel(MouseEvent event) {
        UIUtil.openAndCloseSections(addfriendListPanel.isManaged(), addfriendListPanel,
                contentContainer, friendListPanel, notificationsPanel, getStage());
    }

    public void showNotificationsPanel(MouseEvent event) {
        UIUtil.openAndCloseSections(notificationsPanel.isManaged(), notificationsPanel,
                contentContainer, friendListPanel, addfriendListPanel, getStage());
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

    public void onMouseEnterProfilePhoto(MouseEvent event) {
        loadProfilePhoto(true);
    }

    public void onMouseExitProfilePhoto(MouseEvent event) {
        loadProfilePhoto(false);
    }

    public void logOff(MouseEvent event) {
        cleanup();
        serviceManager.clearCurrentUser();
        try {
            FXMLLoader loader = new FXMLLoader(
                    MainPanelController.class.getResource("../../view/auth/login.fxml")
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