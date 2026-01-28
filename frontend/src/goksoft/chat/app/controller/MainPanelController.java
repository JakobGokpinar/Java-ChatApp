package goksoft.chat.app.controller;

import goksoft.chat.app.config.Environment;
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

    @FXML private SplitPane splitPane;
    @FXML private TextField searchFriendField;
    @FXML private BorderPane chatBorderPane;
    @FXML private ScrollPane friendScrollPane;
    @FXML public BorderPane settingsBorderPane;
    @FXML public HBox operationsHBox;
    @FXML public VBox contentContainer;
    @FXML public VBox friendListPanel;
    @FXML public VBox notificationsPanel;
    @FXML public VBox addfriendListPanel;
    @FXML private VBox friendsVBox;
    @FXML private VBox notificationVBox;
    @FXML private VBox usersVBox;
    @FXML private VBox settingsTopVBox;
    @FXML private TextField searchUserField;
    @FXML public Circle profilePhoto;
    @FXML public Circle settingsButton;
    @FXML public Button mailboxButton;
    @FXML public Circle chatFriendProfilePhoto;
    @FXML public Label chatFriendName;
    @FXML public TextField messageField;
    @FXML public ListView<String> listView;
    @FXML public ChoiceBox<String> languageChoiceBox;
    @FXML private Label settingsUsername;
    @FXML private Label noFriendLabel;
    @FXML private Label noNotifLabel;
    @FXML private Label noUserLabel;

    // Instance fields (no longer static!)
    private String currentFriend;
    private BorderPane currentPane;
    private ArrayList<String> friendsNameList = new ArrayList<>();
    private ArrayList<String> friendRequestsNameList = new ArrayList<>();
    private List<Object> friendArray = new ArrayList<>();
    private int currentTimer;

    // Modern services
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
        setupLanguages();

        settingsUsername.setText(serviceManager.getCurrentUser());

        // Bind Enter key to send messages
        messageField.setOnKeyReleased(event -> {
            if (event.getCode() == KeyCode.ENTER) sendMessage();
        });

        // Initialize scheduler for polling tasks
        scheduler = Executors.newScheduledThreadPool(2);

        // Start polling tasks
        startFriendStatsPolling();
        startFriendRequestsPolling();

        // Setup cleanup on window close
        setupWindowCloseHandler();

        // Prevent splitPane divider from moving
        setupSplitPaneLock();
    }

    /**
     * Setup handler to cleanup resources when window closes
     */
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

    /**
     * Lock the split pane divider position
     */
    private void setupSplitPaneLock() {
        final double pos = splitPane.getDividers().get(0).getPosition();
        splitPane.getDividers().get(0).positionProperty().addListener(
                new ChangeListener<Number>() {
                    @Override
                    public void changed(ObservableValue<? extends Number> observableValue,
                                        Number number, Number t1) {
                        splitPane.getDividers().get(0).setPosition(pos);
                    }
                }
        );
    }

    // ===== FRIEND MANAGEMENT =====

    /**
     * Load friends list from server
     */
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

                                // Create friend box with callback
                                // We need to capture these as final for the lambda
                                final String finalUsername = username;
                                Image photo = ProfilePhotoLoader.loadPhoto(username);

                                BorderPane friendBox = FriendBoxComponent.create(
                                        username, lastMsg, notifCount, passedTime, photo,
                                        () -> {
                                            Image friendPhoto = ProfilePhotoLoader.loadPhoto(finalUsername);
                                            // Find the actual pane after it's added to UI
                                            BorderPane actualPane = null;
                                            for (int k = 0; k < friendsVBox.getChildren().size(); k++) {
                                                Node child = friendsVBox.getChildren().get(k);
                                                if (child.getId() != null && child.getId().equals(finalUsername)) {
                                                    actualPane = (BorderPane) child;
                                                    break;
                                                }
                                            }
                                            onFriendClicked(photo, finalUsername, actualPane);
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

    /**
     * Load friend requests from the server
     */
    private void loadFriendRequests() {
        serviceManager.getFriendService().getFriendRequests()
                .thenAccept(requests -> {
                    Platform.runLater(() -> {
                        notificationVBox.getChildren().clear();
                        friendRequestsNameList.clear();

                        for (String username : requests) {
                            // Create request box with modern callbacks
                            Image photo = ProfilePhotoLoader.loadPhoto(username);
                            BorderPane requestBox = RequestBoxComponent.create(
                                    username, photo,
                                    event -> acceptFriendRequest(username),
                                    event -> rejectFriendRequest(username)
                            );

                            notificationVBox.getChildren().add(0, requestBox);
                            friendRequestsNameList.add(username);
                        }

                        // Show red shadow on mailbox if there are requests
                        if (!friendRequestsNameList.isEmpty()) {
                            UIUtil.dropShadowEffect(Color.RED, 0.60, 1, 1, 15, mailboxButton);
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

    /**
     * Accept a friend request
     */
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
                                    "Could not add friend: " + response.getMessage()
                            );
                        }
                    });
                })
                .exceptionally(ex -> {
                    logger.error("Error accepting friend request", ex);
                    Platform.runLater(() ->
                            WarningWindowController.warningMessage("Connection error")
                    );
                    return null;
                });
    }

    /**
     * Reject a friend request
     */
    private void rejectFriendRequest(String requester) {
        serviceManager.getFriendService().rejectFriendRequest(requester)
                .thenAccept(response -> {
                    Platform.runLater(() -> {
                        if (response.isSuccess()) {
                            WarningWindowController.warningMessage("Request rejected!");
                            loadFriendRequests();
                        } else {
                            WarningWindowController.warningMessage(
                                    "Could not reject request: " + response.getMessage()
                            );
                        }
                    });
                })
                .exceptionally(ex -> {
                    logger.error("Error rejecting friend request", ex);
                    Platform.runLater(() ->
                            WarningWindowController.warningMessage("Connection error")
                    );
                    return null;
                });
    }

    /**
     * Search through friends list
     */
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

    /**
     * Handle clicking on a friend to start chatting
     */
    public void onFriendClicked(Image friendPhoto, String friendName, BorderPane pane) {
        chatFriendName.setText(friendName);
        chatFriendProfilePhoto.setFill(new ImagePattern(friendPhoto));
        chatFriendProfilePhoto.setStrokeWidth(0);
        chatBorderPane.setVisible(true);
        settingsBorderPane.setVisible(false);
        currentFriend = friendName;
        currentPane = pane;

        // Load messages for this friend
        loadMessages();

        // Start polling for new messages
        startMessagePollingForCurrentFriend();
    }

    /**
     * Load messages with current friend
     */
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

                        // Scroll to bottom
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

    /**
     * Send a message to current friend
     */
    public void sendMessage() {
        if (currentFriend == null || messageField.getText().trim().isEmpty()) {
            return;
        }

        String message = messageField.getText();
        String loggedUser = serviceManager.getCurrentUser();

        // Add message to UI immediately (optimistic update)
        listView.getItems().add(loggedUser + ": " + message);
        messageField.clear();

        // Send to server
        serviceManager.getMessageService().sendMessage(currentFriend, message)
                .thenAccept(response -> {
                    if (!response.isSuccess()) {
                        logger.warn("Message send reported failure: {}", response.getMessage());
                    }
                })
                .exceptionally(ex -> {
                    logger.error("Error sending message", ex);
                    Platform.runLater(() ->
                            WarningWindowController.warningMessage("Failed to send message")
                    );
                    return null;
                });

        friendScrollPane.setVvalue(friendScrollPane.getHmin());
    }

    /**
     * Start polling for new messages with current friend
     */
    private void startMessagePollingForCurrentFriend() {
        // Stop previous polling if exists
        if (messagePollingScheduler != null && !messagePollingScheduler.isShutdown()) {
            messagePollingScheduler.shutdown();
        }

        currentTimer = (int) (Math.random() * 1000);
        final int timerSnapshot = currentTimer;

        messagePollingScheduler = Executors.newSingleThreadScheduledExecutor();
        messagePollingScheduler.scheduleAtFixedRate(() -> {
            // Stop if we switched friends
            if (timerSnapshot != currentTimer || currentFriend == null) {
                return;
            }

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

    /**
     * Search for users
     */
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
                            // Create user box with modern callback
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

    /**
     * Send friend request to a user
     */
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
                            WarningWindowController.warningMessage("Connection error")
                    );
                    return null;
                });
    }

    // ===== PROFILE PHOTO =====

    /**
     * Load and display profile photo
     */
    private void loadProfilePhoto(boolean showBlackOverlay) {
        Image image = ProfilePhotoLoader.loadPhoto(serviceManager.getCurrentUser());

        Platform.runLater(() -> {
            if (image == null || image.isError()) {
                profilePhoto.setFill(Color.DODGERBLUE);
            } else {
                profilePhoto.setFill(new ImagePattern(image));
                settingsButton.setFill(new ImagePattern(image));
            }

            if (showBlackOverlay) {
                profilePhoto.setFill(Color.BLACK);
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
            // TODO: Implement proper file upload with modern service
            // For now, just log and show message
            logger.info("Selected file: {}", file.getAbsolutePath());
            WarningWindowController.warningMessage("Photo upload feature coming soon!");

            // Uncomment when file upload service is implemented:
            // serviceManager.getUserService().uploadProfilePhoto(file)
            //     .thenAccept(response -> {
            //         Platform.runLater(() -> {
            //             if (response.isSuccess()) {
            //                 loadProfilePhoto(false);
            //             }
            //         });
            //     });
        }
    }

    // ===== POLLING =====

    /**
     * Polls friend statistics from server and updates UI
     */
    private void startFriendStatsPolling() {
        scheduler.scheduleAtFixedRate(() -> {
            try {
                serviceManager.getFriendService().getFriendsWithDetails()
                        .thenAccept(friendsList -> {
                            Platform.runLater(() -> updateFriendsUI(friendsList));
                        })
                        .exceptionally(ex -> {
                            logger.error("Error polling friend stats", ex);
                            return null;
                        });
            } catch (Exception e) {
                logger.error("Unexpected error in friend stats polling", e);
            }
        }, 0, Environment.MESSAGE_POLL_INTERVAL_MS, TimeUnit.MILLISECONDS);
    }

    /**
     * Polls friend requests from the server and updates UI
     */
    private void startFriendRequestsPolling() {
        scheduler.scheduleAtFixedRate(() -> {
            try {
                serviceManager.getFriendService().getFriendRequests()
                        .thenAccept(requests -> {
                            Platform.runLater(() -> updateFriendRequestsUI(requests));
                        })
                        .exceptionally(ex -> {
                            logger.error("Error polling friend requests", ex);
                            return null;
                        });
            } catch (Exception e) {
                logger.error("Unexpected error in friend requests polling", e);
            }
        }, 0, Environment.FRIEND_REQUEST_POLL_INTERVAL_MS, TimeUnit.MILLISECONDS);
    }

    /**
     * Updates the friends list UI with latest data
     */
    private void updateFriendsUI(List<List<String>> friendsList) {
        int index = 0;

        for (List<String> friendData : friendsList) {
            if (friendData.size() >= 4) {
                String username = friendData.get(0);
                String notifCount = friendData.get(1);
                String lastMsg = friendData.get(2);
                String passedTime = friendData.get(3);
                Image photo = ProfilePhotoLoader.loadPhoto(username);
                // Create new friend box with callback
                BorderPane friendBox = FriendBoxComponent.create(
                        username, lastMsg, notifCount, passedTime, photo,
                        () -> {
                            Image friendPhoto = ProfilePhotoLoader.loadPhoto(username);
                            // Find the actual friendBox pane to pass
                            BorderPane actualPane = null;
                            for (int k = 0; k < friendsVBox.getChildren().size(); k++) {
                                if (friendsVBox.getChildren().get(k).getId() != null &&
                                        friendsVBox.getChildren().get(k).getId().equals(username)) {
                                    actualPane = (BorderPane) friendsVBox.getChildren().get(k);
                                    break;
                                }
                            }
                            onFriendClicked(photo, username, actualPane);
                        }
                );

                // Find and update existing friend box
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

                // If not found, it's a new friend - add it
                if (!found) {
                    friendsVBox.getChildren().add(index, friendBox);
                    index++;
                }
            }
        }
    }

    /**
     * Updates the friend requests UI with latest data
     */
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
                UIUtil.dropShadowEffect(Color.RED, 0.60, 1, 1, 15, mailboxButton);
                logger.info("New friend request from: {}", username);
            }
        }
    }

    // ===== UI HELPERS =====

    /**
     * Show/hide "no results" labels
     */
    private void checkNoResult(boolean isEmpty, Label label) {
        label.setManaged(isEmpty);
        label.setVisible(isEmpty);
    }

    /**
     * Setup language choices
     */
    private void setupLanguages() {
        languageChoiceBox.getItems().removeAll(languageChoiceBox.getItems());
        languageChoiceBox.getItems().addAll("Turkish-Türkçe", "English", "Norwegian-Norsk");
        languageChoiceBox.getSelectionModel().select("English");
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
            getStage().setTitle("Settings");
        } else {
            settingsBorderPane.setVisible(false);
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
                    MainPanelController.class.getResource("userinterfaces/login.fxml")
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

            // Clear stored credentials from Java Preferences
            Preferences prefs = Preferences.userNodeForPackage(LoginController.class);
            prefs.putBoolean("rememberMe", false);
            prefs.remove("username");

            newWindow.setOnCloseRequest(windowEvent -> System.exit(0));
        } catch (IOException e) {
            logger.error("Failed to load login panel", e);
        }
    }

    // ===== CLEANUP =====

    /**
     * Cleanup method to properly shutdown scheduled tasks
     */
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
                logger.warn("Main scheduler shutdown interrupted", e);
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
                logger.warn("Message polling scheduler shutdown interrupted", e);
            }
        }
    }
}