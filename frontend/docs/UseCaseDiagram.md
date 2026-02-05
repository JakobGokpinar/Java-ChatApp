# Use Case Diagram

## Overview

This diagram captures all user-facing interactions available in the JavaFX chat application frontend. The system has a single actor — the **User** — who progresses through authentication into the main application, where they can manage friends, exchange messages, search for users, and configure their profile.

---

## Use Case Diagram

```mermaid
graph LR
    User((👤 User))

    subgraph Authentication
        UC1[Register Account]
        UC2[Login]
        UC3[Logout]
        UC4[Remember Username]
    end

    subgraph Messaging
        UC5[View Conversation]
        UC6[Send Message]
        UC7[Receive Unread Notifications]
        UC8[Mark Messages as Read]
    end

    subgraph Friend Management
        UC9[View Friends List]
        UC10[View Friend Requests]
        UC11[Send Friend Request]
        UC12[Accept Friend Request]
        UC13[Reject Friend Request]
    end

    subgraph User Discovery
        UC14[Search Users by Username]
    end

    subgraph Profile & Settings
        UC15[View Profile Photo]
        UC16[Upload Profile Photo]
        UC17[View Settings Panel]
    end

    User --- UC1
    User --- UC2
    User --- UC3
    User --- UC4
    User --- UC5
    User --- UC6
    User --- UC7
    User --- UC8
    User --- UC9
    User --- UC10
    User --- UC11
    User --- UC12
    User --- UC13
    User --- UC14
    User --- UC15
    User --- UC16
    User --- UC17
```

---

## Use Case Details

### Authentication

| ID | Use Case | Precondition | Postcondition | Service |
|---|---|---|---|---|
| UC1 | Register Account | None | Account created, user redirected to login | `AuthService.register()` |
| UC2 | Login | Account exists | JWT token stored, main panel loaded | `AuthService.login()` |
| UC3 | Logout | Logged in | JWT cleared, return to login screen | `AuthService.logout()` |
| UC4 | Remember Username | On login screen | Username pre-filled on next app launch | Java `Preferences` API |

### Messaging

| ID | Use Case | Precondition | Postcondition | Service |
|---|---|---|---|---|
| UC5 | View Conversation | Logged in, friend selected | Chat history displayed | `MessageService.getMessages()` |
| UC6 | Send Message | In active conversation | Message sent, appears in chat | `MessageService.sendMessage()` |
| UC7 | Receive Unread Notifications | Logged in | Badge count shown on friend list items | `MessageService.checkNotification()` |
| UC8 | Mark Messages as Read | Open conversation | Unread count resets to 0 | Automatic on `getMessages()` |

### Friend Management

| ID | Use Case | Precondition | Postcondition | Service |
|---|---|---|---|---|
| UC9 | View Friends List | Logged in | Friends displayed with last message and unread count | `FriendService.getFriendsWithDetails()` |
| UC10 | View Friend Requests | Logged in | Pending requests listed with accept/reject buttons | `FriendService.getFriendRequests()` |
| UC11 | Send Friend Request | Target user found via search | PENDING friendship created on backend | `FriendService.sendFriendRequest()` |
| UC12 | Accept Friend Request | Pending request received | Friendship status → ACCEPTED, user appears in friends list | `FriendService.acceptFriendRequest()` |
| UC13 | Reject Friend Request | Pending request received | Friendship status → REJECTED, request removed from list | `FriendService.rejectFriendRequest()` |

### User Discovery

| ID | Use Case | Precondition | Postcondition | Service |
|---|---|---|---|---|
| UC14 | Search Users by Username | Logged in, on "Add Friends" tab | Matching usernames listed with "Add" button | `UserService.searchUsers()` |

### Profile & Settings

| ID | Use Case | Precondition | Postcondition | Service |
|---|---|---|---|---|
| UC15 | View Profile Photo | Logged in | Photo displayed in sidebar and settings | `ProfilePhotoLoader.loadPhoto()` |
| UC16 | Upload Profile Photo | Logged in, in settings | Photo uploaded to backend, UI refreshed | `UserService` (upload endpoint) |
| UC17 | View Settings Panel | Logged in | Settings panel visible with username and photo | Controller navigation |

---

## Use Case Relationships

```mermaid
graph TD
    UC2[Login] -->|includes| UC9[View Friends List]
    UC2 -->|includes| UC10[View Friend Requests]
    UC2 -->|includes| UC15[View Profile Photo]
    UC2 -->|extends| UC4[Remember Username]

    UC9 -->|includes| UC7[Receive Unread Notifications]
    UC9 -->|extends| UC5[View Conversation]

    UC5 -->|includes| UC8[Mark Messages as Read]
    UC5 -->|extends| UC6[Send Message]

    UC14[Search Users] -->|extends| UC11[Send Friend Request]

    UC10 -->|extends| UC12[Accept Friend Request]
    UC10 -->|extends| UC13[Reject Friend Request]

    UC12 -->|triggers| UC9

    UC3[Logout] -->|navigates to| UC2

    style UC2 fill:#e8f5e9
    style UC3 fill:#ffebee
    style UC9 fill:#e3f2fd
    style UC5 fill:#e3f2fd
```

The diagram above shows how use cases relate to each other. After **Login**, the friends list and friend requests are loaded automatically (includes). Selecting a friend **extends** into viewing the conversation, which **includes** marking messages as read. The search flow **extends** into sending a friend request when the user clicks "Add."

---

## Interaction Flow

This diagram shows the typical user journey through the application from launch to active messaging:

```mermaid
flowchart TD
    Start([App Launch]) --> HasRemembered{Username remembered?}
    HasRemembered -->|Yes| PreFilled[Pre-fill username field]
    HasRemembered -->|No| LoginScreen[Show login screen]
    PreFilled --> LoginScreen

    LoginScreen --> Choice{New user?}
    Choice -->|Yes| Register[Register Account]
    Register --> LoginScreen
    Choice -->|No| Login[Login with credentials]

    Login --> AuthResult{Auth success?}
    AuthResult -->|No| Error[Show error message]
    Error --> LoginScreen
    AuthResult -->|Yes| MainPanel[Load Main Panel]

    MainPanel --> FriendsLoaded[Friends list loaded]
    MainPanel --> RequestsLoaded[Friend requests loaded]
    MainPanel --> PollingStarted[Background polling started]

    FriendsLoaded --> UserAction{User action}
    UserAction -->|Select friend| ViewChat[View Conversation]
    UserAction -->|Tab: Requests| ViewRequests[View Friend Requests]
    UserAction -->|Tab: Add Friends| SearchUsers[Search Users]
    UserAction -->|Click profile| ViewSettings[View Settings]
    UserAction -->|Click logout| Logout[Logout]

    ViewChat --> SendMsg[Send Messages]
    SendMsg --> ViewChat

    ViewRequests --> AcceptReject{Accept or Reject?}
    AcceptReject -->|Accept| FriendsLoaded
    AcceptReject -->|Reject| ViewRequests

    SearchUsers --> SendRequest[Send Friend Request]
    SendRequest --> SearchUsers

    ViewSettings --> UploadPhoto[Upload Profile Photo]
    UploadPhoto --> ViewSettings

    Logout --> LoginScreen

    style MainPanel fill:#e8f5e9,stroke:#388e3c
    style Login fill:#e3f2fd
    style Register fill:#e3f2fd
    style Logout fill:#ffebee
```

---

## Polling Use Cases (Background)

These use cases execute automatically without direct user action, driven by `ScheduledExecutorService` in `MainPanelController`:

```mermaid
graph TD
    subgraph "Every 2 seconds"
        P1[Poll Friend Stats]
        P1 --> P1A[Update friend list items]
        P1 --> P1B[Update unread badges]
        P1 --> P1C[Update last message previews]
    end

    subgraph "Every 20 seconds"
        P2[Poll Friend Requests]
        P2 --> P2A[Update notification list]
        P2 --> P2B[Highlight mailbox button if pending]
    end

    subgraph "Every 2 seconds (when chat is open)"
        P3[Poll Active Chat Notifications]
        P3 --> P3A{New messages?}
        P3A -->|Yes| P3B[Refresh conversation view]
        P3A -->|No| P3C[No action]
    end
```

These background tasks are started on `MainPanelController.initialize()` and stopped on window close via `cleanup()`.
