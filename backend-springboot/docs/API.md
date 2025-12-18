# API Documentation

Complete reference for all REST API endpoints.

## Base URL
```
http://localhost:8080
```

## Authentication

All endpoints except `/api/auth/login` and `/api/auth/register` require JWT authentication.

**Header Format:**
```
Authorization: Bearer <your_jwt_token>
```

---

## 📋 Authentication Endpoints

### Register User
Creates a new user account.

**Endpoint:** `POST /api/auth/register`

**Parameters:**
- `username` (string, required) - Min 3 characters
- `password` (string, required) - Min 4 characters

**Example Request:**
```bash
curl -X POST "http://localhost:8080/api/auth/register?username=alice&password=secret123"
```

**Success Response (200):**
```json
{
  "success": true,
  "message": "Registration successful",
  "data": {
    "username": "alice",
    "token": null
  },
  "timestamp": "2025-12-18T10:30:00"
}
```

**Error Responses:**
- `400` - Username/password validation failed
- `409` - Username already exists

---

### Login
Authenticates user and returns JWT token.

**Endpoint:** `POST /api/auth/login`

**Parameters:**
- `username` (string, required)
- `password` (string, required)

**Example Request:**
```bash
curl -X POST "http://localhost:8080/api/auth/login?username=alice&password=secret123"
```

**Success Response (200):**
```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "username": "alice",
    "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhbGljZSIsImlhdCI6MTY..."
  },
  "timestamp": "2025-12-18T10:30:00"
}
```

**Error Responses:**
- `400` - Missing username/password
- `401` - Invalid credentials

---

## 👥 Friends Endpoints

### Get Friends List
Returns list of accepted friends.

**Endpoint:** `POST /api/friends/get`

**Headers:** Requires JWT token

**Example Request:**
```bash
curl -X POST "http://localhost:8080/api/friends/get" \
  -H "Authorization: Bearer eyJhbGciOi..."
```

**Success Response (200):**
```json
{
  "success": true,
  "message": "Friends retrieved",
  "data": ["bob", "charlie", "diana"],
  "timestamp": "2025-12-18T10:30:00"
}
```

---

### Get Friend Requests
Returns pending friend requests received by current user.

**Endpoint:** `POST /api/friends/requests`

**Headers:** Requires JWT token

**Success Response (200):**
```json
{
  "success": true,
  "message": "Friend requests retrieved",
  "data": ["eve", "frank"],
  "timestamp": "2025-12-18T10:30:00"
}
```

---

### Send Friend Request
Sends friend request to another user.

**Endpoint:** `POST /api/friends/send-request`

**Headers:** Requires JWT token

**Parameters:**
- `receiver` (string, required) - Username to send request to

**Example Request:**
```bash
curl -X POST "http://localhost:8080/api/friends/send-request?receiver=bob" \
  -H "Authorization: Bearer eyJhbGciOi..."
```

**Error Responses:**
- `409` - Already friends or request already sent
- `400` - Cannot send request to yourself

---

### Accept Friend Request
Accepts a pending friend request.

**Endpoint:** `POST /api/friends/accept`

**Headers:** Requires JWT token

**Parameters:**
- `requester` (string, required) - Username who sent the request

**Example Request:**
```bash
curl -X POST "http://localhost:8080/api/friends/accept?requester=bob" \
  -H "Authorization: Bearer eyJhbGciOi..."
```

**Error Responses:**
- `404` - Friend request not found
- `400` - Request already processed

---

### Reject Friend Request
Rejects a pending friend request.

**Endpoint:** `POST /api/friends/reject`

**Headers:** Requires JWT token

**Parameters:**
- `requester` (string, required) - Username who sent the request

---

## 💬 Messages Endpoints

### Send Message
Sends a message to another user.

**Endpoint:** `POST /api/messages/send`

**Headers:** Requires JWT token

**Parameters:**
- `receiver` (string, required) - Recipient username
- `message` (string, required) - Message content

**Example Request:**
```bash
curl -X POST "http://localhost:8080/api/messages/send?receiver=bob&message=Hello!" \
  -H "Authorization: Bearer eyJhbGciOi..."
```

**Error Responses:**
- `400` - Message cannot be empty

---

### Get Messages
Retrieves conversation with another user and marks messages as read.

**Endpoint:** `POST /api/messages/get`

**Headers:** Requires JWT token

**Parameters:**
- `receiver` (string, required) - Other user in conversation

**Example Request:**
```bash
curl -X POST "http://localhost:8080/api/messages/get?receiver=bob" \
  -H "Authorization: Bearer eyJhbGciOi..."
```

**Success Response (200):**
```json
{
  "success": true,
  "message": "Messages retrieved",
  "data": [
    {
      "sender": "alice",
      "message": "Hi Bob!"
    },
    {
      "sender": "bob",
      "message": "Hey Alice!"
    }
  ],
  "timestamp": "2025-12-18T10:30:00"
}
```

---

### Check Unread Count
Returns number of unread messages from specific user.

**Endpoint:** `POST /api/messages/check-notif`

**Headers:** Requires JWT token

**Parameters:**
- `chatter` (string, required) - Username to check unread from

**Success Response (200):**
```json
{
  "success": true,
  "message": "Notification count",
  "data": 5,
  "timestamp": "2025-12-18T10:30:00"
}
```

---

## 👤 Users Endpoints

### Search Users
Search for users by username.

**Endpoint:** `POST /api/users/search`

**Headers:** Requires JWT token

**Parameters:**
- `username` (string, required) - Search term (partial match)

**Example Request:**
```bash
curl -X POST "http://localhost:8080/api/users/search?username=ali" \
  -H "Authorization: Bearer eyJhbGciOi..."
```

**Success Response (200):**
```json
{
  "success": true,
  "message": "Users found",
  "data": ["alice", "alicia", "alistair"],
  "timestamp": "2025-12-18T10:30:00"
}
```

**Note:** Returns max 20 results.

---

### Get Profile Photo
Retrieves profile photo for a user.

**Endpoint:** `GET /api/users/photo/{username}`

**Headers:** Requires JWT token

**Example Request:**
```bash
curl "http://localhost:8080/api/users/photo/alice" \
  -H "Authorization: Bearer eyJhbGciOi..."
```

**Success Response (200):**
- Content-Type: `image/png`
- Body: Binary image data

---

### Update Profile Photo
Updates current user's profile photo.

**Endpoint:** `POST /api/users/photo`

**Headers:** Requires JWT token

**Content-Type:** `multipart/form-data`

**Form Data:**
- `photo` (file, required) - Image file

**Example Request:**
```bash
curl -X POST "http://localhost:8080/api/users/photo" \
  -H "Authorization: Bearer eyJhbGciOi..." \
  -F "photo=@/path/to/image.png"
```

---

## 🔴 Error Responses

All errors follow this format:
```json
{
  "success": false,
  "message": "Human-readable error message",
  "error": "ERROR_CODE",
  "status": 400,
  "timestamp": "2025-12-18T10:30:00"
}
```

**Error Codes:**
- `VALIDATION_ERROR` (400) - Invalid input
- `AUTHENTICATION_ERROR` (401) - Invalid credentials
- `RESOURCE_NOT_FOUND` (404) - Resource doesn't exist
- `DUPLICATE_RESOURCE` (409) - Resource already exists
- `INTERNAL_SERVER_ERROR` (500) - Unexpected error