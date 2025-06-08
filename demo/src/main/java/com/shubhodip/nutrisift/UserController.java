package com.shubhodip.nutrisift;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserDAO userDAO;

    @PostMapping
    public ResponseEntity<String> saveUser(@RequestBody User user) {
        // Check if email already exists
        if (userDAO.emailExists(user.getEmail())) {
            return ResponseEntity.status(409).body("Email already exists.");
        }

        // Generate unique userid
        String userid;
        do {
            userid = UUID.randomUUID().toString();
        } while (userDAO.isUserIdExists(userid));

        user.setUserid(userid);

        // Encrypt password before saving
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String encryptedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(encryptedPassword);

        int rows = userDAO.saveUser(user);
        if (rows > 0) {
            return ResponseEntity.ok("User saved successfully with userid: " + userid);
        } else {
            return ResponseEntity.status(500).body("Failed to save user");
        }
    }

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @GetMapping("/verify/{userId}")
    public ResponseEntity<?> verifyUser(@PathVariable String userId) {
        try {
            String sql = "SELECT email FROM users WHERE userid = ?";
            String email = jdbcTemplate.queryForObject(sql, String.class, userId);
            
            if (email != null) {
                Map<String, String> response = new HashMap<>();
                response.put("email", email);
                response.put("userId", userId);
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Collections.singletonMap("error", "User not found"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Collections.singletonMap("error", "Error verifying user: " + e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<String> loginUser(@RequestBody LoginRequest loginRequest) {
        User user = userDAO.getUserByEmail(loginRequest.getEmail());

        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        if (user == null || !passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid email or password");
        }
        
        // Update last login time with current Indian time
        userDAO.updateLastLogin(user.getEmail());

        return ResponseEntity.ok("Login successful for userid: " + user.getUserid());
    }

    @GetMapping("/security-question")
    public ResponseEntity<?> getSecurityQuestion(@RequestParam String email) {
        try {
            User user = userDAO.getUserByEmail(email);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No account found with this email address.");
            }
            
            Map<String, String> response = new HashMap<>();
            response.put("securityQuestion", user.getSecurityQuestion());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An error occurred while retrieving security question: " + e.getMessage());
        }
    }

    @PostMapping("/verify-answer")
    public ResponseEntity<?> verifySecurityAnswer(@RequestBody SecurityAnswerRequest request) {
        try {
            User user = userDAO.getUserByEmail(request.getEmail());
            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
            }
            
            // Compare the provided answer with the stored answer (case-insensitive)
            if (user.getSecurityAnswer().equalsIgnoreCase(request.getAnswer())) {
                return ResponseEntity.ok().build(); // 200 OK with empty body for success
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Incorrect answer");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An error occurred while verifying answer: " + e.getMessage());
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody PasswordResetRequest request) {
        try {
            User user = userDAO.getUserByEmail(request.getEmail());
            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
            }
            
            // Encrypt the new password
            PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
            String encryptedPassword = passwordEncoder.encode(request.getNewPassword());
            
            // Update the password in the database
            boolean updated = userDAO.updatePassword(request.getEmail(), encryptedPassword);
            if (updated) {
                return ResponseEntity.ok("Password reset successful");
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to reset password");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An error occurred while resetting password: " + e.getMessage());
        }
    }
}