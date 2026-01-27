package goksoft.chat.app;

import goksoft.chat.app.config.Environment;
import goksoft.chat.app.service.ServiceManager;
import goksoft.chat.app.util.UIUtil;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Node;
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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MainPanelController extends HBox {

    private static final Logger logger = LoggerFactory.getLogger(MainPanelController.class);

    @FXML private SplitPane splitPane;
    @FXML private TextField searchFriendField;
    @FXML private BorderPane chatBorderPane;
    @FXML private ScrollPane friendScrollPane;
    @FXML public BorderPane settingsBorderPane;
    @FXML public HBox operationsHBox;
    @FXML public VBox mixedVBox;
    @FXML public VBox friendSection;
    @FXML public VBox mailboxSection;
    @FXML public VBox addFriendSection;
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
    @FXML private TextField newPassField;
    @FXML private TextField newNameField;
    @FXML private Label changeLabel;
    @FXML private RadioButton darkThemeButton;

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

        settingsUsername.setText(LoginController.loggedUser);

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
                                BorderPane friendBox = GUIComponents.friendBox(
                                        username, lastMsg, notifCount, passedTime,
                                        () -> {
                                            Image photo = GUIComponents.returnPhoto(username);
                                            onFriendClicked(photo, username, friendBox);
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
     * Load friend requests from server
     */
    private void loadFriendRequests() {
        serviceManager.getFriendService().getFriendRequests()
                .thenAccept(requests -> {
                    Platform.runLater(() -> {
                        notificationVBox.getChildren().clear();
                        friendRequestsNameList.clear();

                        for (String username : requests) {
                            // Create request box with modern callbacks
                            BorderPane requestBox = GUIComponents.requestBox(
                                    username,
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
        String loggedUser = LoginController.loggedUser;

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
                            HBox userBox = GUIComponents.userBox(
                                    username,
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
        Image image = GUIComponents.returnPhoto(LoginController.loggedUser);

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

    /**
     * Change profile photo
     */
    public void changeProfilePhoto(MouseEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Profile Photo");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
        );

        Stage stage = (Stage) profilePhoto.getScene().getWindow();
        File file = fileChooser.showOpenDialog(stage);

        if (file != null) {
            // TODO: Implement photo upload with modern service
            logger.info("Selected file: {}", file.getAbsolutePath());
            // For now, use old ServerFunctions
            String response = ServerFunctions.FILERequest(
                    ServerFunctions.serverURL + "/users/photo?username=",
                    file,
                    "photo"
            );
            logger.info("Photo upload response: {}", response);
            loadProfilePhoto(false);
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
     * Polls friend requests from server and updates UI
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
     * Update friends UI during polling
     */
    private void updateFriendsUI(List<List<String>> friendsList) {
        int index = 0;

        for (List<String> friendData : friendsList) {
            if (friendData.size() >= 4) {
                String username = friendData.get(0);
                String notifCount = friendData.get(1);
                String lastMsg = friendData.get(2);
                String passedTime = friendData.get(3);

                BorderPane friendBox = GUIComponents.friendBox(
                        username, lastMsg, notifCount, passedTime,
                        () -> {
                            Image photo = GUIComponents.returnPhoto(username);
                            onFriendClicked(photo, username, friendBox);
                        }
                );

                // Find and update existing friend box
                for (int j = 0; j < friendsVBox.getChildren().size(); j++) {
                    if (friendsVBox.getChildren().get(j).getId() != null &&
                            friendsVBox.getChildren().get(j).getId().equals(username)) {
                        friendsVBox.getChildren().remove(j);
                        friendsVBox.getChildren().add(index, friendBox);
                        index++;
                        break;
                    }
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
                BorderPane requestBox = GUIComponents.requestBox(
                        username,
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

    public void openAddFriend(MouseEvent event) {
        UIUtil.openAndCloseSections(addFriendSection.isManaged(), addFriendSection,
                mixedVBox, friendSection, mailboxSection, getStage());
    }

    public void openMailbox(MouseEvent event) {
        UIUtil.openAndCloseSections(mailboxSection.isManaged(), mailboxSection,
                mixedVBox, friendSection, addFriendSection, getStage());
    }

    public void openSettings(MouseEvent event) {
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
        Function.logOff(event);
    }

    public void contactUs(MouseEvent event) {
        Function.contactUs(event);
    }

    // ===== CLEANUP =====

    /**
     * Cleanup method to properly shutdown scheduled tasks
     */
    public void cleanup() {
        GlobalVariables.setIsThread(false);

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