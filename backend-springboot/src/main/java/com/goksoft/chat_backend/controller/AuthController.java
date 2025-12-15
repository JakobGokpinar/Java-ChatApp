package com.goksoft.chat_backend.controller;

import com.goksoft.chat_backend.model.User;
import com.goksoft.chat_backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/*
    POST /api/auth/login
    POST /api/auth/register
*/
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*") // Allow frontend to call this
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestParam String username,
                                        @RequestParam String password) {
        var user = userRepository.findByUsernameAndPassword(username, password);

        if (user.isPresent()) {
            return ResponseEntity.ok("login successful");
        } else {
            return ResponseEntity.ok("login unsuccessful");
        }
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestParam String username,
                                           @RequestParam String password) {
        // Check if username already exists
        if (userRepository.existsByUsername(username)) {
            return ResponseEntity.ok("register unsuccessful");
        }

        // Create and save new user
        User newUser = new User(username, password);
        userRepository.save(newUser);

        return ResponseEntity.ok("register successful");
    }
}