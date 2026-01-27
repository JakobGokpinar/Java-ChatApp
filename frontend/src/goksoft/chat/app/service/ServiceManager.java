package goksoft.chat.app.service;

public class ServiceManager {
    private static ServiceManager instance;
    
    private final ApiClient apiClient;
    private final FriendService friendService;
    private final MessageService messageService;
    private final UserService userService;
    
    private ServiceManager() {
        this.apiClient = new ApiClient();
        this.friendService = new FriendService(apiClient);
        this.messageService = new MessageService(apiClient);
        this.userService = new UserService(apiClient);
    }
    
    public static ServiceManager getInstance() {
        if (instance == null) {
            instance = new ServiceManager();
        }
        return instance;
    }
    
    public FriendService getFriendService() { return friendService; }
    public MessageService getMessageService() { return messageService; }
    public UserService getUserService() { return userService; }
    public ApiClient getApiClient() { return apiClient; }
}