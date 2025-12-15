package com.goksoft.chat_backend.repository;

import com.goksoft.chat_backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {

    // Find user by username and password (for login)
    // findByUsername(String username) → Spring auto-creates SELECT * FROM users WHERE username = ?
    Optional<User> findByUsernameAndPassword(String username, String password);

    // Check if username exists (for registration)
    boolean existsByUsername(String username);
}