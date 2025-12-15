# Java-ChatApp

A modern desktop messaging application built with **Spring Boot** backend and **JavaFX** frontend. This project represents a complete modernization of a legacy PHP-based chat application, now featuring a RESTful API architecture, proper separation of concerns, and a maintainable codebase.

![Java](https://img.shields.io/badge/Java-17-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2-brightgreen)
![JavaFX](https://img.shields.io/badge/JavaFX-21-blue)
![MySQL](https://img.shields.io/badge/MySQL-8.0-blue)

---

## 📋 Table of Contents

- [Features](#features)
- [Tech Stack](#tech-stack)
- [Architecture](#architecture)
- [Prerequisites](#prerequisites)
- [Installation & Setup](#installation--setup)
- [Project Structure](#project-structure)
- [API Endpoints](#api-endpoints)
- [Screenshots](#screenshots)
- [Development Roadmap](#development-roadmap)
- [Legacy Repositories](#legacy-repositories)
- [Contributing](#contributing)
- [License](#license)

---

## ✨ Features

- **User Authentication**: Secure registration and login
- **Friend System**: Send, accept, and reject friend requests
- **Real-time Messaging**: Send and receive messages with notification system
- **User Search**: Find and connect with other users
- **Profile Management**: Customize profile with photo uploads
- **Desktop Application**: Native JavaFX desktop client for Windows, macOS, and Linux

---

## 🛠 Tech Stack

### Backend
- **Spring Boot 3.2** - Modern Java framework for REST API
- **Spring Data JPA** - Database abstraction and ORM
- **MySQL 8.0** - Relational database
- **Maven** - Dependency management and build tool

### Frontend
- **JavaFX 21** - Cross-platform desktop UI framework
- **JSON Simple** - JSON parsing for API responses

### Development Tools
- **IntelliJ IDEA** - Primary IDE
- **MAMP** - Local MySQL database server
- **Git/GitHub** - Version control

---

## 🏗 Architecture

This application follows a modern **layered architecture** with clear separation of concerns:

```
┌─────────────────────────────────────────────────────────────┐
│                    JavaFX Frontend                          │
│              (Desktop Application - UI)                     │
└────────────────────────┬────────────────────────────────────┘
                         │ HTTP REST API
                         ▼
┌─────────────────────────────────────────────────────────────┐
│                   Spring Boot Backend                       │
│                                                             │
│  ┌─────────────┐  ┌─────────────┐  ┌──────────────┐      │
│  │ Controllers │  │  Services   │  │ Repositories │      │
│  │ (REST API)  │─▶│  (Logic)    │─▶│  (Data)      │      │
│  └─────────────┘  └─────────────┘  └──────┬───────┘      │
│                                            │               │
└────────────────────────────────────────────┼───────────────┘
                                             ▼
                                    ┌─────────────────┐
                                    │  MySQL Database │
                                    │   (localhost)   │
                                    └─────────────────┘
```

### Key Components

**Backend Layers:**
- **Controller Layer**: REST endpoints (`@RestController`)
- **Repository Layer**: Database access with Spring Data JPA
- **Model Layer**: Entity classes mapping to database tables

**Frontend Structure:**
- **Controllers**: Handle UI interactions
- **ServerFunctions**: HTTP request management
- **GUIComponents**: Reusable UI elements
- **FXML**: UI layout definitions

---

## 📦 Prerequisites

Before you begin, ensure you have the following installed:

- **Java 17+** - [Download](https://www.oracle.com/java/technologies/downloads/)
- **Maven 3.6+** - [Download](https://maven.apache.org/download.cgi)
- **MySQL 8.0+** - [Download](https://dev.mysql.com/downloads/mysql/) or use [MAMP](https://www.mamp.info/)
- **JavaFX SDK 21** - [Download](https://openjfx.io/)
- **Git** - [Download](https://git-scm.com/downloads)

---

## 🚀 Installation & Setup

### 1. Clone the Repository

```bash
git clone https://github.com/JakobGokpinar/Java-ChatApp.git
cd Java-ChatApp
```

### 2. Database Setup

**Option A: Using MAMP (Recommended for macOS)**

1. Install and start MAMP
2. Open phpMyAdmin: `http://localhost:8888/phpMyAdmin/`
3. Create database: `goksoft_chat_app`
4. Import schema: Use `backend/sql/schema.sql` (if provided)

**Option B: Using MySQL directly**

```bash
mysql -u root -p
CREATE DATABASE goksoft_chat_app;
USE goksoft_chat_app;
SOURCE path/to/schema.sql;
```

**Database Tables:**
- `users` - User accounts
- `friends` - Friend relationships
- `requeststable` - Friend requests
- `messagetable` - Messages
- `notiftable` - Notification counts

### 3. Backend Setup (Spring Boot)

```bash
cd backend

# Update application.properties with your database credentials
# Edit: src/main/resources/application.properties

spring.datasource.url=jdbc:mysql://localhost:8889/goksoft_chat_app
spring.datasource.username=root
spring.datasource.password=root

# Build and run
mvn clean install
mvn spring-boot:run
```

Backend will start on: `http://localhost:8080`

### 4. Frontend Setup (JavaFX)

**Configure JavaFX in IntelliJ IDEA:**

1. Download JavaFX SDK 21 and extract to a location (e.g., `~/javafx-21`)
2. Open `frontend` folder in IntelliJ IDEA
3. **File → Project Structure → Libraries**
4. Add JavaFX library: `+` → Java → Select `javafx-21/lib`
5. Add JSON Simple: Download `json-simple-1.1.1.jar` to `frontend/libs/` and add as library

**Create Run Configuration:**

1. **Run → Edit Configurations → + → Application**
2. **Main class**: `goksoft.chat.app.Main`
3. **VM options**:
   ```
   --module-path /path/to/javafx-21/lib --add-modules javafx.controls,javafx.fxml
   ```
4. Click **Apply** → **OK**

**Run the application:**

Click the green ▶️ Run button or:
```bash
# From IntelliJ's terminal
./mvnw javafx:run
```

---

## 📁 Project Structure

```
Java-ChatApp/
├── backend/                          # Spring Boot REST API
│   ├── src/main/java/com/goksoft/chat_backend/
│   │   ├── controller/              # REST endpoints
│   │   │   ├── AuthController.java
│   │   │   ├── FriendController.java
│   │   │   ├── MessageController.java
│   │   │   └── UserController.java
│   │   ├── model/                   # Entity classes (JPA)
│   │   │   ├── User.java
│   │   │   ├── Friend.java
│   │   │   ├── FriendRequest.java
│   │   │   ├── Message.java
│   │   │   └── Notification.java
│   │   └── repository/              # Data access (Spring Data JPA)
│   │       ├── UserRepository.java
│   │       ├── FriendRepository.java
│   │       ├── FriendRequestRepository.java
│   │       ├── MessageRepository.java
│   │       └── NotificationRepository.java
│   ├── src/main/resources/
│   │   └── application.properties   # Database config
│   └── pom.xml                      # Maven dependencies
│
├── frontend/                         # JavaFX Desktop Client
│   ├── src/goksoft/chat/app/
│   │   ├── Main.java                # Application entry point
│   │   ├── ServerFunctions.java     # HTTP request handler
│   │   ├── LoginController.java     # Login screen logic
│   │   ├── RegisterController.java  # Registration logic
│   │   ├── MainPanelController.java # Main chat interface
│   │   ├── Function.java            # Core app functions
│   │   ├── GUIComponents.java       # Reusable UI components
│   │   └── userinterfaces/          # FXML layouts
│   └── libs/                        # External JARs
│
└── README.md                         # This file
```

---

## 🔌 API Endpoints

### Authentication
- `POST /api/auth/register` - Register new user
- `POST /api/auth/login` - User login

### Friends
- `POST /api/friends/get` - Get friends list
- `POST /api/friends/requests` - Get friend requests
- `POST /api/friends/accept` - Accept friend request
- `POST /api/friends/reject` - Reject friend request
- `POST /api/friends/send-request` - Send friend request

### Messages
- `POST /api/messages/send` - Send message
- `POST /api/messages/get` - Get conversation
- `POST /api/messages/check-notif` - Check notification count

### Users
- `POST /api/users/search` - Search users by username
- `GET /api/users/photo/{username}` - Get profile photo
- `POST /api/users/photo` - Upload profile photo

**Example Request:**
```bash
curl -X POST "http://localhost:8080/api/auth/login?username=testuser&password=test123"
```

---

## 📸 Screenshots

### Login Screen
![Login Screen](screenshots/login-screen.png)

### Main Chat Interface
![Main Chat](screenshots/main-chat.png)

### Settings
![Settings](screenshots/settings.png)

*Note: Screenshots will be added soon*

---

## 🗺 Development Roadmap

### ✅ Phase 1: Backend Migration (Completed)
- [x] Set up Spring Boot project
- [x] Create entity models (User, Friend, Message, etc.)
- [x] Implement repositories with Spring Data JPA
- [x] Build REST API controllers
- [x] Replace PHP endpoints with Spring Boot

### 🚧 Phase 2: Integration & Testing (In Progress)
- [x] Update frontend to call new REST API
- [ ] Complete endpoint migration
- [ ] Test all features end-to-end
- [ ] Fix bugs and edge cases

### 📋 Phase 3: Code Improvements (Planned)
- [ ] Add Service layer for business logic
- [ ] Implement proper error handling
- [ ] Add input validation
- [ ] Refactor notification system
- [ ] Improve code naming conventions
- [ ] Add comprehensive comments

### 🎨 Phase 4: Frontend Modernization (Planned)
- [ ] Add Gradle/Maven for frontend dependencies
- [ ] Modernize UI design
- [ ] Implement better MVC/MVVM pattern
- [ ] Add modern JavaFX libraries (MaterialFX, AtlantaFX)

### 🚀 Phase 5: Advanced Features (Future)
- [ ] JWT authentication
- [ ] Password hashing (BCrypt)
- [ ] WebSocket for real-time messaging
- [ ] Group chats
- [ ] File sharing
- [ ] Docker containerization
- [ ] Unit and integration tests

---

## 📜 Legacy Repositories

This project is a modernized version of earlier work:

- **[Chat-App-Backend](https://github.com/JakobGokpinar/Chat-App-Backend)** - Original PHP backend (Archived)
- **[Chat-App-Frontend](https://github.com/JakobGokpinar/Chat-App-Frontend)** - Original JavaFX frontend with PHP integration (Archived)

The legacy repositories are preserved for historical reference and portfolio purposes.

---

## 🤝 Contributing

Contributions are welcome! Please feel free to submit issues or pull requests.

1. Fork the repository
2. Create your feature branch: `git checkout -b feature/amazing-feature`
3. Commit your changes: `git commit -m 'Add amazing feature'`
4. Push to the branch: `git push origin feature/amazing-feature`
5. Open a Pull Request

---

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

## 👨‍💻 Author

**Jakob Gökpınar**

- GitHub: [@JakobGokpinar](https://github.com/JakobGokpinar)
- Email: [Your email if you want to include it]

---

## 🙏 Acknowledgments

- Original project developed in 2020 as a learning exercise
- Modernized in 2024/2025 as part of software architecture studies at University of Oslo
- Built with passion for clean code and modern software design

---

**⭐ If you find this project helpful, please consider giving it a star!**
