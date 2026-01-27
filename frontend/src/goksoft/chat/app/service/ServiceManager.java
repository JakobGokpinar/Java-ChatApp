package goksoft.chat.app.service;

import goksoft.chat.app.api.ApiClient;

public class ServiceManager {

    private static ServiceManager instance;

    private final ApiClient apiClient;
    private final AuthService authService;
    private final FriendService friendService;
    private final MessageService messageService;
    private final UserService userService;

    private ServiceManager() {
        this.apiClient = new ApiClient();
        this.authService = new AuthService(apiClient);
        this.friendService = new FriendService(apiClient);
        this.messageService = new MessageService(apiClient);
        this.userService = new UserService(apiClient);
    }

    public static synchronized ServiceManager getInstance() {
        if (instance == null) {
            instance = new ServiceManager();
        }
        return instance;
    }

    public ApiClient getApiClient() {
        return apiClient;
    }

    public AuthService getAuthService() {
        return authService;
    }

    public FriendService getFriendService() {
        return friendService;
    }

    public MessageService getMessageService() {
        return messageService;
    }

    public UserService getUserService() {
        return userService;
    }
}