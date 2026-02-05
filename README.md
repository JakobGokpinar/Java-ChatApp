# Java Chat Application

A desktop messenger built with **JavaFX** and **Spring Boot**, featuring JWT authentication, friend management, and real-time messaging. This project is a complete modernization of a chat app originally built in 2020, transforming a non-functional legacy system into a production-ready application with modern Java architecture.

![Java 21](https://img.shields.io/badge/Java-21_LTS-orange)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.x-brightgreen)
![JavaFX](https://img.shields.io/badge/JavaFX-21-blue)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-336791)
![Railway](https://img.shields.io/badge/Deployed_on-Railway-blueviolet)

---

## Features

- **JWT Authentication** — Register and login with token-based security and BCrypt password hashing
- **Real-Time Messaging** — Send and receive messages with automatic polling and unread notifications
- **Friend System** — Send, accept, and reject friend requests using a unified friendship model
- **User Search** — Find and connect with other users by username
- **Profile Photos** — Upload and display profile pictures
- **Notification Badges** — Visual indicators for unread messages and pending friend requests
- **Remember Me** — Optional username persistence across sessions
- **Dev/Prod Toggle** — Seamless switching between local development and Railway cloud deployment

---

## Tech Stack

### Backend

| Technology | Purpose |
|---|---|
| Java 21 LTS | Language |
| Spring Boot 3.x | REST API framework |
| Spring Security | Authentication and authorization |
| JWT (JJWT 0.12.6) | Stateless token-based auth |
| BCrypt | Password hashing |
| Spring Data JPA / Hibernate | ORM and data access |
| PostgreSQL | Production database (Railway) |
| H2 | Local development database |

### Frontend

| Technology | Purpose |
|---|---|
| Java 21 LTS | Language |
| JavaFX 21.0.5 | Desktop UI framework |
| Java HttpClient | HTTP communication (built-in, Java 11+) |
| Gson 2.11.0 | JSON serialization/deserialization |
| SLF4J + Logback | Logging |
| CompletableFuture | Async operations |

### Development and Deployment

| Tool | Purpose |
|---|---|
| Maven | Build and dependency management |
| IntelliJ IDEA | IDE |
| Railway | Cloud hosting (backend + PostgreSQL) |
| Git / GitHub | Version control |

---

## Architecture

### Backend — Layered REST API

```
Client Request
    │
    ▼
┌──────────────────────┐
│   Security Filter    │  JWT validation, extract username from token
├──────────────────────┤
│   Controller Layer   │  REST endpoints, request/response handling
├──────────────────────┤
│   Service Layer      │  Business logic, validation, transactions
├──────────────────────┤
│   Repository Layer   │  Spring Data JPA, JPQL queries
├──────────────────────┤
│   Entity Layer       │  JPA entities (User, Friendship, Message)
├──────────────────────┤
│   PostgreSQL / H2    │  3 tables: users, friendships, messages
└──────────────────────┘
```

### Frontend — MVC with Service Layer

```
┌──────────────────────┐
│   FXML Views         │  UI layout definitions (login, register, main panel)
├──────────────────────┤
│   Controllers        │  JavaFX controllers, UI event handling
├──────────────────────┤
│   UI Components      │  Reusable components (FriendBox, RequestBox, UserBox)
├──────────────────────┤
│   Service Layer      │  AuthService, FriendService, MessageService, UserService
├──────────────────────┤
│   ApiClient          │  HttpClient with JWT token management
├──────────────────────┤
│   Environment Config │  Dev/Prod URL switching, timeouts, polling intervals
└──────────────────────┘
```

### Authentication Flow

```
1. User submits credentials
2. Backend validates and returns JWT token
3. Frontend stores token in ApiClient
4. All subsequent requests include Authorization: Bearer <token>
5. Backend extracts username from token (no impersonation possible)
6. Logout clears the token client-side
```

---

## Database Schema

The database uses 3 normalized tables, simplified from the original 5-table design.

```
┌─────────────────┐       ┌──────────────────┐       ┌─────────────────┐
│     USERS       │       │   FRIENDSHIPS    │       │    MESSAGES     │
│─────────────────│       │──────────────────│       │─────────────────│
│ username (PK)   │◄──────│ user1 (FK)       │       │ id (PK)         │
│ password        │◄──────│ user2 (FK)       │       │ sender (FK)  ───│──► users
│ photo           │◄──────│ initiated_by (FK)│       │ receiver (FK) ──│──► users
│ created_at      │       │ status (ENUM)    │       │ content         │
│ updated_at      │       │ created_at       │       │ is_read         │
└─────────────────┘       │ updated_at       │       │ created_at      │
                          └──────────────────┘       └─────────────────┘

Friendship Status: PENDING → ACCEPTED / REJECTED
```

Passwords are stored as BCrypt hashes (never plain text). The `is_read` boolean on messages replaced a separate notifications table. The unified `friendships` table with a status enum replaced the old `friends` + `requeststable` pair, making queries simpler and preventing inconsistent state.

---

## Project Structure

```
Java-ChatApp/
├── backend-springboot/
│   └── src/main/java/com/chatapp/backend/
│       ├── config/                    # Security, JWT, filters
│       │   ├── SecurityConfig.java
│       │   ├── JwtAuthenticationFilter.java
│       │   ├── JwtUtil.java
│       │   └── SecurityUtils.java
│       ├── controller/                # REST endpoints
│       │   ├── AuthController.java
│       │   ├── FriendController.java
│       │   ├── MessageController.java
│       │   └── UserController.java
│       ├── dto/
│       │   ├── request/               # LoginRequest, RegisterRequest
│       │   └── response/              # ApiResponse, FriendDetailsResponse, etc.
│       ├── exception/                 # Global error handling
│       │   ├── GlobalExceptionHandler.java
│       │   └── ...                    # Custom exceptions
│       ├── model/                     # JPA entities
│       │   ├── User.java
│       │   ├── Friendship.java
│       │   └── Message.java
│       ├── repository/                # Spring Data JPA interfaces
│       └── service/                   # Business logic
│       └── docs/                      # Backend documentation
│
├── frontend/
│   └── src/goksoft/chat/app/
│       ├── api/
│       │   └── ApiClient.java         # HttpClient with JWT token management
│       ├── config/
│       │   └── Environment.java       # Dev/Prod URLs, timeouts, polling
│       ├── controller/
│       │   ├── LoginController.java
│       │   ├── RegisterController.java
│       │   └── MainPanelController.java
│       ├── model/dto/                 # ApiResponse, User, Message, LoginResponse, etc.
│       ├── service/
│       │   ├── ServiceManager.java    # Singleton — single access point for all services
│       │   ├── AuthService.java
│       │   ├── FriendService.java
│       │   ├── MessageService.java
│       │   └── UserService.java
│       ├── ui/components/             # Reusable UI components
│       ├── util/
│       │   └── JsonUtil.java          # Gson wrapper with TypeToken support
│       └── resources/
│           └── userinterfaces/        # FXML layouts
│
└── README.md
```

---

## Getting Started

### Prerequisites

- **Java 21 LTS** — [Download](https://jdk.java.net/21/)
- **Maven 3.9+** — [Download](https://maven.apache.org/download.cgi)
- **IntelliJ IDEA** recommended (Community or Ultimate)

For local development, H2 runs in-memory automatically — no separate database install needed.

### Clone the Repository

```bash
git clone https://github.com/JakobGokpinar/Java-ChatApp.git
cd Java-ChatApp
```

### Backend Setup

```bash
cd backend-springboot
mvn spring-boot:run
```

The API starts at `http://localhost:8080`. H2 is used by default for local development. For production, the backend connects to a PostgreSQL instance on Railway via environment variables.

To run with a local PostgreSQL instead of H2, configure `application.properties`:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/chat_app
spring.datasource.username=your_username
spring.datasource.password=your_password
```

### Frontend Setup

```bash
cd frontend
mvn javafx:run -Dapp.env=dev
```

Or in IntelliJ: add VM option `-Dapp.env=dev` to your run configuration to connect to the local backend.

Without the flag, the frontend defaults to production mode and connects to the Railway deployment.

### Environment Configuration

The app uses a single VM argument to switch environments:

| Mode | VM Argument | Backend URL |
|---|---|---|
| Development | `-Dapp.env=dev` | `http://localhost:8080/api` |
| Production | *(none, default)* | `https://java-chatapp-production.up.railway.app/api` |

All timeouts and polling intervals are centralized in `Environment.java`:

| Setting | Value |
|---|---|
| Connect timeout | 10 seconds |
| Request timeout | 30 seconds |
| Message polling | Every 2 seconds |
| Friend request polling | Every 20 seconds |

---

## API Endpoints

All endpoints except `/api/auth/*` require a JWT token in the `Authorization: Bearer <token>` header.

### Authentication

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/auth/register` | Register a new user |
| POST | `/api/auth/login` | Login, returns JWT token |

### Friends

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/friends/get` | Get accepted friends list |
| POST | `/api/friends/get-details` | Get friends with last message, unread count, timestamps |
| POST | `/api/friends/requests` | Get pending friend requests |
| POST | `/api/friends/send-request?receiver=X` | Send a friend request |
| POST | `/api/friends/accept?requester=X` | Accept a friend request |
| POST | `/api/friends/reject?requester=X` | Reject a friend request |

### Messages

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/messages/send?receiver=X&message=Y` | Send a message |
| POST | `/api/messages/get?receiver=X` | Get conversation (marks messages as read) |
| POST | `/api/messages/check-notif?chatter=X` | Get unread message count |

### Users

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/users/search?username=X` | Search users by username (max 20 results) |
| GET | `/api/users/photo/{username}` | Get profile photo |
| POST | `/api/users/photo` | Upload profile photo (multipart) |

All responses use a standard wrapper:

```json
{
  "success": true,
  "message": "Login successful",
  "data": { "token": "eyJhbG...", "user": { "id": 1, "username": "alice" } },
  "timestamp": "2026-01-27T10:30:00"
}
```

---

## Security

- **JWT Authentication** — Stateless tokens with 24-hour expiry, validated on every request via `JwtAuthenticationFilter`
- **BCrypt Password Hashing** — Passwords are never stored in plain text
- **Identity from Token** — The backend extracts the username from the JWT on every request; users cannot impersonate others by passing a different username as a parameter
- **Spring Security** — All endpoints except `/api/auth/login` and `/api/auth/register` are protected
- **CORS** — Configured for cross-origin requests from the desktop client

---

## Testing

The project has comprehensive test coverage across both backend and frontend.

### Backend — 56 tests, ~90% coverage

```bash
cd backend-springboot
mvn test
```

Covers all service layer logic, JWT utilities, authentication flows, and global exception handling.

### Frontend — ~100 tests, ~85% coverage

```bash
cd frontend
mvn test
```

Covers the service layer (AuthService, FriendService, MessageService, UserService), ApiClient token management, JSON serialization/deserialization, and environment configuration. Service tests mock the `ApiClient` and use `CompletableFuture.completedFuture()` / `failedFuture()` to test async patterns without needing a running backend or JavaFX thread.

### Generate Coverage Reports

```bash
mvn test jacoco:report
# Report at target/site/jacoco/index.html
```

---

## The Modernization Story

This project started as one of my first coding projects in 2020. It stopped working when Heroku discontinued its free tier and my new M4 MacBook couldn't run the old JavaFX build. Rather than abandoning it, I chose to rebuild it from scratch using everything I've learned since then.

| Aspect | Original (2020) | Modernized (2026) |
|---|---|---|
| Backend | PHP | Java 21 + Spring Boot |
| Database | MySQL on Heroku ClearDB | PostgreSQL on Railway |
| Authentication | Session cookies | JWT tokens + BCrypt |
| API style | Individual PHP files | Layered REST API with DTOs |
| Frontend HTTP | Manual `HttpURLConnection` | Java 11+ `HttpClient` |
| JSON parsing | Manual / json-simple | Gson with generics and TypeToken |
| Async pattern | Daemon threads + `Thread.sleep()` | `CompletableFuture` |
| Error handling | `try-catch` with `printStackTrace()` | Global exception handler + SLF4J |
| Database schema | 5 tables with redundancy | 3 normalized tables |
| Code structure | 700-line God class | Focused classes, 50–150 lines each |
| Hosting | Heroku (defunct free tier) | Railway |

### Refactoring highlights

- **93% reduction in the God class** — `Function.java` went from 700+ lines mixing UI, networking, and business logic down to focused, single-responsibility services and utilities
- **Eliminated global static state** — Replaced mutable `GlobalVariables` with a `ServiceManager` singleton and proper instance-scoped state
- **Unified friendship model** — Consolidated separate `friends` and `requeststable` tables into a single `friendships` table with a status enum (`PENDING → ACCEPTED / REJECTED`)
- **Modern async patterns** — Replaced manual daemon threads with `CompletableFuture` chains and `Platform.runLater()` for JavaFX thread safety
- **Proper security** — JWT tokens prevent the impersonation attacks that were possible with the old cookie-based system

---

## Legacy Repositories

The original codebases are preserved as archived references:

- [Chat-App-Backend](https://github.com/JakobGokpinar/Chat-App-Backend) — Original PHP backend
- [Chat-App-Frontend](https://github.com/JakobGokpinar/Chat-App-Frontend) — Original JavaFX frontend

---

## License

This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.

---

## Author

**Jakob Gökpınar**

- GitHub: [@JakobGokpinar](https://github.com/JakobGokpinar)
- University of Oslo — Programming and System Architecture
