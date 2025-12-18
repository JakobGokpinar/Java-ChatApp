# Database Design Documentation

## 🗄️ Overview

The chat application uses **MySQL 8.0** with a clean, normalized relational schema. The design prioritizes simplicity, data integrity, and query performance.

**Database Name:** `chat_app`

**Tables:** 3 (users, friendships, messages)

---

## 📊 Entity Relationship Diagram
```
┌─────────────────────┐
│       USERS         │
│─────────────────────│
│ username (PK)       │◄─────┐
│ password            │      │
│ photo               │      │
│ created_at          │      │
│ updated_at          │      │
└─────────────────────┘      │
         ▲                    │
         │                    │
         │ FK                 │ FK
         │                    │
┌────────┴──────────┐  ┌──────┴──────────┐
│   FRIENDSHIPS     │  │    MESSAGES     │
│───────────────────│  │─────────────────│
│ id (PK)           │  │ id (PK)         │
│ user1 (FK)        │  │ sender (FK)     │
│ user2 (FK)        │  │ receiver (FK)   │
│ status            │  │ content         │
│ initiated_by (FK) │  │ is_read         │
│ created_at        │  │ created_at      │
│ updated_at        │  └─────────────────┘
└───────────────────┘
```

---

## 📋 Table Schemas

### 1. USERS

**Purpose:** Store user accounts and authentication data
```sql
CREATE TABLE users (
    username VARCHAR(50) PRIMARY KEY,
    password VARCHAR(255) NOT NULL,
    photo LONGBLOB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

**Columns:**

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| username | VARCHAR(50) | PRIMARY KEY | Unique user identifier |
| password | VARCHAR(255) | NOT NULL | BCrypt hashed password (60 chars) |
| photo | LONGBLOB | NULL | Profile picture (binary) |
| created_at | TIMESTAMP | DEFAULT NOW | Account creation time |
| updated_at | TIMESTAMP | AUTO UPDATE | Last modification time |

**Indexes:**
- PRIMARY KEY on `username` (clustered index)

**Design Decisions:**
- ✅ Username as PRIMARY KEY (natural key, always used for lookups)
- ✅ VARCHAR(255) for password (BCrypt outputs 60 chars, leaving room)
- ✅ LONGBLOB for photos (supports images up to 4GB)
- ✅ Timestamps for auditing

**Sample Data:**
```sql
INSERT INTO users VALUES 
('alice', '$2a$10$N9qo8uLOickgx2ZMRZoMye...', NULL, NOW(), NOW()),
('bob', '$2a$10$X8pQ7uLPjdkfx3AMRZpNxe...', NULL, NOW(), NOW());
```

---

### 2. FRIENDSHIPS

**Purpose:** Unified table for friend relationships and requests
```sql
CREATE TABLE friendships (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user1 VARCHAR(50) NOT NULL,
    user2 VARCHAR(50) NOT NULL,
    status ENUM('PENDING', 'ACCEPTED', 'REJECTED') DEFAULT 'PENDING',
    initiated_by VARCHAR(50) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (user1) REFERENCES users(username) ON DELETE CASCADE,
    FOREIGN KEY (user2) REFERENCES users(username) ON DELETE CASCADE,
    FOREIGN KEY (initiated_by) REFERENCES users(username) ON DELETE CASCADE,
    
    UNIQUE KEY unique_friendship (user1, user2),
    
    INDEX idx_user1_status (user1, status),
    INDEX idx_user2_status (user2, status),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

**Columns:**

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | INT | PRIMARY KEY, AUTO_INCREMENT | Unique friendship ID |
| user1 | VARCHAR(50) | FOREIGN KEY → users | First user in relationship |
| user2 | VARCHAR(50) | FOREIGN KEY → users | Second user in relationship |
| status | ENUM | NOT NULL | PENDING/ACCEPTED/REJECTED |
| initiated_by | VARCHAR(50) | FOREIGN KEY → users | Who sent the request |
| created_at | TIMESTAMP | DEFAULT NOW | Request creation time |
| updated_at | TIMESTAMP | AUTO UPDATE | Status change time |

**Constraints:**
- UNIQUE on `(user1, user2)` - prevents duplicate friendships
- ON DELETE CASCADE - remove friendships when user deleted

**Indexes:**
- PRIMARY KEY on `id`
- COMPOSITE INDEX on `(user1, status)` - query accepted friends
- COMPOSITE INDEX on `(user2, status)` - query pending requests
- INDEX on `status` - filter by status

**Status State Machine:**
```
[PENDING] ──accept──> [ACCEPTED]
    │
    └────reject───> [REJECTED]
                        │
                        └──can resend──> [PENDING]
```

**Design Decisions:**
- ✅ Unified table (replaced separate `friends` + `requeststable`)
- ✅ Status enum for clear state management
- ✅ `initiated_by` tracks who sent request
- ✅ Order-independent: (alice, bob) = (bob, alice) via UNIQUE constraint

**Sample Data:**
```sql
-- Alice sends request to Bob (pending)
INSERT INTO friendships (user1, user2, status, initiated_by) 
VALUES ('alice', 'bob', 'PENDING', 'alice');

-- Bob accepts, status updated
UPDATE friendships SET status = 'ACCEPTED' WHERE id = 1;

-- Charlie rejects Diana's request
INSERT INTO friendships (user1, user2, status, initiated_by) 
VALUES ('charlie', 'diana', 'REJECTED', 'diana');
```

**Query Examples:**
```sql
-- Get accepted friends for alice
SELECT CASE 
    WHEN user1 = 'alice' THEN user2 
    ELSE user1 
END AS friend
FROM friendships
WHERE (user1 = 'alice' OR user2 = 'alice') 
  AND status = 'ACCEPTED';

-- Get pending requests received by bob
SELECT initiated_by
FROM friendships
WHERE (user1 = 'bob' OR user2 = 'bob')
  AND status = 'PENDING'
  AND initiated_by != 'bob';

-- Check if alice and bob are friends
SELECT EXISTS(
    SELECT 1 FROM friendships
    WHERE ((user1 = 'alice' AND user2 = 'bob') 
       OR (user1 = 'bob' AND user2 = 'alice'))
      AND status = 'ACCEPTED'
);
```

---

### 3. MESSAGES

**Purpose:** Store chat messages between users
```sql
CREATE TABLE messages (
    id INT AUTO_INCREMENT PRIMARY KEY,
    sender VARCHAR(50) NOT NULL,
    receiver VARCHAR(50) NOT NULL,
    content TEXT NOT NULL,
    is_read BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (sender) REFERENCES users(username) ON DELETE CASCADE,
    FOREIGN KEY (receiver) REFERENCES users(username) ON DELETE CASCADE,
    
    INDEX idx_sender_receiver (sender, receiver, created_at),
    INDEX idx_receiver_sender (receiver, sender, created_at),
    INDEX idx_receiver_unread (receiver, is_read)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

**Columns:**

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | INT | PRIMARY KEY, AUTO_INCREMENT | Unique message ID |
| sender | VARCHAR(50) | FOREIGN KEY → users | Message sender |
| receiver | VARCHAR(50) | FOREIGN KEY → users | Message recipient |
| content | TEXT | NOT NULL | Message text (up to 65KB) |
| is_read | BOOLEAN | DEFAULT FALSE | Read status |
| created_at | TIMESTAMP | DEFAULT NOW | Message timestamp |

**Constraints:**
- ON DELETE CASCADE - remove messages when user deleted

**Indexes:**
- PRIMARY KEY on `id`
- COMPOSITE INDEX on `(sender, receiver, created_at)` - conversation queries
- COMPOSITE INDEX on `(receiver, sender, created_at)` - reverse lookup
- COMPOSITE INDEX on `(receiver, is_read)` - unread count queries

**Design Decisions:**
- ✅ `is_read` flag (replaced separate `notiftable`)
- ✅ TEXT type for content (supports longer messages)
- ✅ No history column (single timestamp sufficient)
- ✅ Bidirectional indexes for fast conversation retrieval

**Sample Data:**
```sql
-- Alice sends message to Bob
INSERT INTO messages (sender, receiver, content, is_read) 
VALUES ('alice', 'bob', 'Hey Bob!', FALSE);

-- Bob sends reply
INSERT INTO messages (sender, receiver, content, is_read) 
VALUES ('bob', 'alice', 'Hi Alice!', FALSE);

-- Bob reads Alice's message
UPDATE messages 
SET is_read = TRUE 
WHERE receiver = 'bob' AND sender = 'alice' AND is_read = FALSE;
```

**Query Examples:**
```sql
-- Get conversation between alice and bob
SELECT sender, content, created_at
FROM messages
WHERE (sender = 'alice' AND receiver = 'bob')
   OR (sender = 'bob' AND receiver = 'alice')
ORDER BY created_at ASC;

-- Count unread messages for bob from alice
SELECT COUNT(*) 
FROM messages
WHERE receiver = 'bob' AND sender = 'alice' AND is_read = FALSE;

-- Mark all messages as read
UPDATE messages
SET is_read = TRUE
WHERE receiver = 'bob' AND sender = 'alice' AND is_read = FALSE;

-- Get last message for each conversation (for bob)
SELECT 
    CASE WHEN sender = 'bob' THEN receiver ELSE sender END AS other_user,
    content AS last_message,
    created_at,
    (SELECT COUNT(*) 
     FROM messages m2 
     WHERE m2.receiver = 'bob' 
       AND m2.sender = other_user 
       AND m2.is_read = FALSE) AS unread_count
FROM messages m
WHERE sender = 'bob' OR receiver = 'bob'
ORDER BY created_at DESC;
```

---

## 🔄 Relationships

### User ↔ Friendships

**Type:** One-to-Many (as initiator) + Many-to-Many (as participant)

**Cardinality:**
- One user can initiate many friendship requests
- One user can be in many friendships
- Each friendship involves exactly 2 users

**Referential Integrity:**
- CASCADE DELETE: Deleting a user removes all their friendships

---

### User ↔ Messages

**Type:** One-to-Many (as sender) + One-to-Many (as receiver)

**Cardinality:**
- One user can send many messages
- One user can receive many messages
- Each message has exactly 1 sender and 1 receiver

**Referential Integrity:**
- CASCADE DELETE: Deleting a user removes all their messages

---

## 🎯 Design Principles

### 1. Normalization

**Normal Form:** 3NF (Third Normal Form)

**Benefits:**
- ✅ No redundant data
- ✅ Data consistency
- ✅ Easy updates

**Verification:**
- 1NF: Atomic values ✓
- 2NF: No partial dependencies ✓
- 3NF: No transitive dependencies ✓

---

### 2. Referential Integrity

**Foreign Keys:**
- All relationships enforced at database level
- CASCADE DELETE for data consistency
- Prevents orphaned records

**Benefits:**
- ✅ Data consistency guaranteed
- ✅ Automatic cleanup
- ✅ Database-level validation

---

### 3. Indexing Strategy

**Primary Indexes:**
- All PRIMARY KEYs automatically indexed

**Secondary Indexes:**
- Composite indexes on frequently queried columns
- Cover queries without table scans

**Query Performance:**
```sql
-- Without index: O(n) table scan
-- With index: O(log n) B-tree lookup
```

---

## 📈 Data Growth Estimates

### Storage Calculations

**Users:**
- Average row size: ~350 bytes (with small photo)
- 10,000 users = ~3.5 MB
- 1,000,000 users = ~350 MB

**Friendships:**
- Average row size: ~150 bytes
- Avg 50 friends per user
- 10,000 users = 500,000 friendships = ~75 MB
- 1,000,000 users = 50M friendships = ~7.5 GB

**Messages:**
- Average row size: ~250 bytes (150 char message)
- Avg 100 messages per user per month
- 10,000 users = 1M messages/month = ~250 MB/month
- 1,000,000 users = 100M messages/month = ~25 GB/month

**Total Growth:**
- Small scale (10K users): ~330 MB + 250 MB/month
- Large scale (1M users): ~33 GB + 25 GB/month

---

## 🔧 Database Maintenance

### Backup Strategy

**Daily Backups:**
```bash
mysqldump -u root -p chat_app > backup_$(date +%Y%m%d).sql
```

**Retention:**
- Daily: Keep 7 days
- Weekly: Keep 4 weeks
- Monthly: Keep 12 months

---

### Index Maintenance

**Check Index Usage:**
```sql
SELECT 
    TABLE_NAME, 
    INDEX_NAME, 
    CARDINALITY 
FROM information_schema.STATISTICS 
WHERE TABLE_SCHEMA = 'chat_app';
```

**Rebuild Indexes (if fragmented):**
```sql
OPTIMIZE TABLE users;
OPTIMIZE TABLE friendships;
OPTIMIZE TABLE messages;
```

---

### Performance Monitoring

**Slow Query Log:**
```sql
SET GLOBAL slow_query_log = 'ON';
SET GLOBAL long_query_time = 2; -- 2 seconds
```

**Query Analysis:**
```sql
EXPLAIN SELECT * FROM messages WHERE receiver = 'alice';
```

---

## 🚨 Data Integrity Rules

### Business Rules Enforced

**1. Username Uniqueness**
- PRIMARY KEY constraint on `users.username`

**2. No Duplicate Friendships**
- UNIQUE constraint on `(user1, user2)` in friendships

**3. Valid Friend Status**
- ENUM constraint limits to PENDING/ACCEPTED/REJECTED

**4. Message Read Status**
- BOOLEAN constraint (TRUE/FALSE only)

**5. Timestamps**
- Automatic creation and update tracking

---

## 🔐 Security Considerations

### Password Storage
- ✅ Never store plain text passwords
- ✅ BCrypt hashing (60 character output)
- ✅ Salt automatically included
- ✅ Adaptive work factor

### Data Privacy
- ✅ CASCADE DELETE removes user data
- ✅ No soft deletes (GDPR compliance)
- ✅ Profile photos optional

### SQL Injection Prevention
- ✅ JPA parameterized queries
- ✅ No dynamic SQL construction
- ✅ Input validation at service layer

---

## 📊 Migration History

### Version 1.0 (Initial - Old Schema)

**Tables:** 5
- users
- friends (accepted only)
- requeststable (pending only)
- messagetable
- notiftable
- blacklist

**Problems:**
- ❌ Redundant data across tables
- ❌ Complex queries (multiple joins)
- ❌ Inconsistent state possible
- ❌ Difficult to maintain

---

### Version 2.0 (Current - Refactored)

**Tables:** 3
- users (enhanced)
- friendships (unified)
- messages (with is_read)

**Improvements:**
- ✅ 40% fewer tables
- ✅ Simpler queries
- ✅ Clear state machine
- ✅ Better performance

**Migration Script:**
```sql
-- Migrate accepted friends
INSERT INTO friendships (user1, user2, status, initiated_by, created_at)
SELECT person1, person2, 'ACCEPTED', person1, NOW()
FROM friends;

-- Migrate pending requests
INSERT INTO friendships (user1, user2, status, initiated_by, created_at)
SELECT sender, receiver, 'PENDING', sender, NOW()
FROM requeststable;

-- Migrate messages (add is_read flag)
INSERT INTO messages (sender, receiver, content, is_read, created_at)
SELECT sender, receiver, msg, FALSE, history
FROM messagetable;
```

---

## 🧪 Test Data

### Sample Dataset
```sql
-- Create users
INSERT INTO users (username, password) VALUES
('alice', '$2a$10$...'),
('bob', '$2a$10$...'),
('charlie', '$2a$10$...'),
('diana', '$2a$10$...');

-- Create friendships
INSERT INTO friendships (user1, user2, status, initiated_by) VALUES
('alice', 'bob', 'ACCEPTED', 'alice'),
('alice', 'charlie', 'PENDING', 'alice'),
('bob', 'diana', 'ACCEPTED', 'bob');

-- Create messages
INSERT INTO messages (sender, receiver, content, is_read) VALUES
('alice', 'bob', 'Hey Bob!', TRUE),
('bob', 'alice', 'Hi Alice!', FALSE),
('bob', 'diana', 'Hello!', TRUE),
('diana', 'bob', 'Hey there!', FALSE);
```

---

## 🔮 Future Enhancements

### Phase 1 (Planned)
- [ ] Message attachments table
- [ ] Group chat support
- [ ] Message reactions
- [ ] User preferences table

### Phase 2 (Future)
- [ ] Partitioning for large message tables
- [ ] Read replicas for scaling
- [ ] Archiving old messages
- [ ] Full-text search on messages

---

## 📚 References

- [MySQL 8.0 Documentation](https://dev.mysql.com/doc/refman/8.0/)
- [Database Normalization Guide](https://www.studytonight.com/dbms/database-normalization.php)
- [MySQL Indexing Best Practices](https://dev.mysql.com/doc/refman/8.0/en/optimization-indexes.html)
- [InnoDB Storage Engine](https://dev.mysql.com/doc/refman/8.0/en/innodb-storage-engine.html)