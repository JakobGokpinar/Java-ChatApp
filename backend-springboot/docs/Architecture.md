# Backend Architecture Documentation

## 🏗️ Overview

The backend follows a **layered architecture** pattern with clear separation of concerns. Built with Spring Boot 4.0 and Java 21, it provides a RESTful API for a real-time chat application.

## 📊 Architecture Layers
```
┌─────────────────────────────────────────┐
│         REST API Layer                   │
│      (Controllers + DTOs)                │
├─────────────────────────────────────────┤
│         Business Logic Layer             │
│           (Services)                     │
├─────────────────────────────────────────┤
│         Data Access Layer                │
│         (Repositories)                   │
├─────────────────────────────────────────┤
│         Domain Model Layer               │
│           (Entities)                     │
├─────────────────────────────────────────┤
│         Database Layer                   │
│          (MySQL 8.0)                     │
└─────────────────────────────────────────┘
```

## 🔄 Request Flow
```
1. Client → HTTP Request
2. SecurityFilter → JWT Validation
3. Controller → Route to Service
4. Service → Business Logic
5. Repository → Database Query
6. Entity → ORM Mapping
7. Database → Execute SQL
   ↓
8. Database → Return Results
9. Entity → Map to Java Objects
10. Repository → Return to Service
11. Service → Map to DTO
12. Controller → HTTP Response
13. Client → Receive JSON
```

---

## 📦 Layer Details

### 1. Controller Layer (`controller/`)

**Responsibility:** Handle HTTP requests/responses

**Components:**
- `AuthController` - Authentication endpoints
- `FriendController` - Friend management
- `MessageController` - Messaging operations
- `UserController` - User operations

**Key Features:**
- RESTful endpoint design
- Request validation
- Response formatting
- CORS handling

**Example:**
```java
@RestController
@RequestMapping("/api/friends")
public class FriendController {
    
    @Autowired
    private FriendService friendService;
    
    @PostMapping("/get")
    public ResponseEntity<ApiResponse<List<String>>> getFriends() {
        String username = SecurityUtils.getCurrentUsername(); // From JWT
        ApiResponse<List<String>> response = friendService.getFriends(username);
        return ResponseEntity.ok(response);
    }
}
```

---

### 2. Service Layer (`service/`)

**Responsibility:** Business logic and orchestration

**Components:**
- `AuthService` - Authentication logic
- `FriendService` - Friendship operations
- `MessageService` - Message handling
- `UserService` - User management

**Key Features:**
- Business rule enforcement
- Transaction management
- Cross-cutting concerns (logging, validation)
- DTO mapping

**Design Principles:**
- Single Responsibility Principle
- Dependency Injection
- Transaction boundaries

**Example:**
```java
@Service
public class FriendService {
    
    @Autowired
    private FriendshipRepository friendshipRepository;
    
    @Transactional
    public ApiResponse<String> sendFriendRequest(String sender, String receiver) {
        // Business logic: validate, check duplicates, create friendship
        if (friendshipRepository.areFriends(sender, receiver)) {
            throw new DuplicateResourceException("Already friends");
        }
        
        Friendship friendship = new Friendship(sender, receiver, sender);
        friendshipRepository.save(friendship);
        
        return ApiResponse.success("Friend request sent", null);
    }
}
```

---

### 3. Repository Layer (`repository/`)

**Responsibility:** Database access and queries

**Components:**
- `UserRepository`
- `FriendshipRepository`
- `MessageRepository`

**Technology:** Spring Data JPA

**Key Features:**
- JPQL custom queries
- Automatic CRUD operations
- Query method naming conventions
- Type-safe database access

**Example:**
```java
@Repository
public interface FriendshipRepository extends JpaRepository<Friendship, Integer> {
    
    @Query("SELECT f FROM Friendship f WHERE " +
           "(f.user1 = :username OR f.user2 = :username) " +
           "AND f.status = 'ACCEPTED'")
    List<Friendship> findAcceptedFriendships(@Param("username") String username);
}
```

---

### 4. Entity Layer (`model/`)

**Responsibility:** Domain model representation

**Components:**
- `User` - User accounts
- `Friendship` - Friend relationships
- `Message` - Chat messages

**Technology:** JPA/Hibernate

**Key Features:**
- Automatic table mapping
- Relationship management
- Lifecycle callbacks
- Optimistic locking

**Example:**
```java
@Entity
@Table(name = "friendships")
public class Friendship {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @Enumerated(EnumType.STRING)
    private FriendshipStatus status;
    
    // ... fields, getters, setters
}
```

---

## 🔐 Security Architecture

### Authentication Flow
```
┌─────────┐     1. Login Request      ┌──────────────┐
│         │ ────────────────────────> │              │
│  Client │                            │ AuthService  │
│         │ <──────────────────────── │              │
└─────────┘     2. JWT Token          └──────────────┘
     │                                        │
     │ 3. Store Token                         │ 2a. Verify BCrypt
     │                                        │ 2b. Generate JWT
     ▼                                        ▼
┌─────────┐                            ┌──────────────┐
│ Local   │                            │   Database   │
│ Storage │                            └──────────────┘
└─────────┘
     │
     │ 4. Send Token in Header
     │    Authorization: Bearer <token>
     ▼
┌──────────────────┐
│ JwtAuthFilter    │ ──> 5. Validate Token
│                  │ ──> 6. Extract Username
│                  │ ──> 7. Set SecurityContext
└──────────────────┘
     │
     │ 8. Username from Token
     ▼
┌──────────────────┐
│   Controllers    │
└──────────────────┘
```

### Security Components

**1. SecurityConfig**
- Configures Spring Security
- Defines public/protected endpoints
- Sets up JWT filter chain
- Disables session management (stateless)

**2. JwtUtil**
- Token generation
- Token validation
- Username extraction
- Expiration handling (24 hours)

**3. JwtAuthenticationFilter**
- Intercepts all requests
- Validates JWT tokens
- Sets authentication context
- Handles token errors

**4. BCryptPasswordEncoder**
- Password hashing on registration
- Password verification on login
- Adaptive work factor

### Protected Endpoints

All endpoints except `/api/auth/login` and `/api/auth/register` require:
- Valid JWT token in `Authorization` header
- Token format: `Bearer <token>`
- Username extracted from token (cannot be faked)

---

## 🎯 Design Patterns

### 1. Repository Pattern
**Location:** `repository/`

**Purpose:** Abstraction over data access

**Benefits:**
- Decouples business logic from data access
- Easy to mock for testing
- Consistent data access interface

---

### 2. Service Layer Pattern
**Location:** `service/`

**Purpose:** Encapsulate business logic

**Benefits:**
- Reusable business rules
- Transaction boundaries
- Clear separation of concerns

---

### 3. DTO Pattern
**Location:** `dto/`

**Purpose:** Data transfer between layers

**Benefits:**
- Hides internal entity structure
- API versioning support
- Validation separation

**Implementation:** Java 21 Records (immutable)

---

### 4. Dependency Injection
**Framework:** Spring IoC Container

**Benefits:**
- Loose coupling
- Easy testing
- Configuration management

**Usage:** `@Autowired` annotation

---

### 5. Builder Pattern
**Location:** `ApiResponse`, JWT tokens

**Purpose:** Complex object construction

**Benefits:**
- Fluent API
- Immutable objects
- Optional parameters

---

## 🚨 Error Handling Architecture

### Global Exception Handler
```
Exception Thrown
     │
     ▼
┌────────────────────────┐
│ GlobalExceptionHandler │
│   @ControllerAdvice    │
└────────────────────────┘
     │
     ├─> AuthenticationException → 401
     ├─> ResourceNotFoundException → 404
     ├─> ValidationException → 400
     ├─> DuplicateResourceException → 409
     └─> Exception → 500
     │
     ▼
┌────────────────────────┐
│   ErrorResponse DTO    │
│  - success: false      │
│  - message: "..."      │
│  - error: "CODE"       │
│  - status: 4xx/5xx     │
│  - timestamp           │
└────────────────────────┘
```

### Custom Exceptions

**1. ChatAppException** (Base)
- Parent for all custom exceptions

**2. AuthenticationException**
- Invalid credentials
- Token validation failures

**3. ResourceNotFoundException**
- User not found
- Friend request not found

**4. ValidationException**
- Input validation errors
- Business rule violations

**5. DuplicateResourceException**
- Username already exists
- Friend request already sent

---

## 📝 Logging Strategy

### Log Levels

**INFO:**
- Successful operations
- Service method entry/exit
- Business events

**WARN:**
- Failed authentication attempts
- Validation failures
- Recoverable errors

**ERROR:**
- Unexpected exceptions
- Database errors
- Critical failures

### Log Format
```
2025-12-18 10:30:45 INFO  [AuthService] Login attempt for user: alice
2025-12-18 10:30:45 INFO  [AuthService] Login successful for user: alice
2025-12-18 10:30:50 WARN  [AuthService] Login failed - invalid password for user: bob
```

---

## 🔄 Transaction Management

### Strategy
- **Propagation:** REQUIRED (default)
- **Isolation:** READ_COMMITTED
- **Rollback:** On RuntimeException

### Usage
```java
@Transactional
public ApiResponse<String> acceptFriendRequest(String accepter, String requester) {
    // Multiple database operations in single transaction
    Friendship friendship = friendshipRepository.findByUsers(accepter, requester);
    friendship.setStatus(FriendshipStatus.ACCEPTED);
    friendshipRepository.save(friendship);
    
    // Auto-commit or rollback on exception
}
```

---

## 🎨 API Design Principles

### 1. RESTful Conventions
- HTTP methods: POST (for all operations, due to frontend constraints)
- Resource-based URLs: `/api/friends`, `/api/messages`
- HTTP status codes: 200, 400, 401, 404, 409, 500

### 2. Consistent Response Format
All responses use `ApiResponse<T>`:
```json
{
  "success": true/false,
  "message": "Human-readable message",
  "data": <actual data>,
  "timestamp": "ISO-8601 format"
}
```

### 3. Stateless Design
- No server-side sessions
- JWT tokens for authentication
- Each request is independent

### 4. CORS Support
- Enabled for all origins (development)
- Configurable for production

---

## 🧪 Testing Strategy

### Unit Tests
- Service layer logic
- Repository queries
- Utility methods

### Integration Tests
- Controller endpoints
- Database operations
- Security filters

### Test Data
- H2 in-memory database
- @DataJpaTest for repositories
- @WebMvcTest for controllers

---

## 📊 Performance Considerations

### Database Optimization
- Indexed columns: username, status
- Eager/lazy loading configuration
- Connection pooling (HikariCP)
- Query optimization with JPQL

### Caching Strategy
- (Future) Redis for JWT blacklist
- (Future) Cache frequently accessed data

### Scalability
- Stateless design (horizontal scaling)
- Database connection pooling
- Async processing (future)

---

## 🔮 Future Architecture Enhancements

### Phase 1 (Completed)
- ✅ Layered architecture
- ✅ JWT authentication
- ✅ Global error handling
- ✅ Comprehensive logging

### Phase 2 (Planned)
- [ ] WebSocket for real-time messaging
- [ ] Redis caching layer
- [ ] Rate limiting
- [ ] API versioning

### Phase 3 (Future)
- [ ] Microservices architecture
- [ ] Event-driven messaging (Kafka)
- [ ] CQRS pattern
- [ ] GraphQL API

---

## 📚 Technology Stack

| Layer | Technology | Version |
|-------|-----------|---------|
| Language | Java | 21 LTS |
| Framework | Spring Boot | 4.0.0 |
| Security | Spring Security | 6.x |
| ORM | Hibernate/JPA | 6.x |
| Database | MySQL | 8.0 |
| Auth | JWT (JJWT) | 0.12.6 |
| Build Tool | Maven | 3.9+ |

---

## 🎓 Key Architectural Decisions

### 1. Why Spring Boot 4.0?
- Latest version (Nov 2025)
- Java 21 support
- Modern Spring Framework 7
- Long-term support

### 2. Why Java 21 Records for DTOs?
- Immutable by default
- Concise syntax (3 lines vs 20+)
- Type-safe
- Modern Java showcase

### 3. Why JWT over Sessions?
- Stateless (scales horizontally)
- Mobile-friendly
- Industry standard
- No server-side storage

### 4. Why Unified Friendships Table?
- Simpler than separate tables
- Clear state machine (enum)
- Fewer bugs
- Better query performance

### 5. Why @Transactional on Service Layer?
- Clear transaction boundaries
- Business logic controls transactions
- Easy rollback management

---

## 📖 References

- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Spring Security JWT](https://docs.spring.io/spring-security/reference/)
- [Java 21 Features](https://openjdk.org/projects/jdk/21/)
- [REST API Best Practices](https://restfulapi.net/)