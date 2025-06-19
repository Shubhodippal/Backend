package com.shubhodip.nutrisift.user;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.shubhodip.nutrisift.Web_token_util.JwtUtil;

import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserDAO userDAO;

    @PostMapping("/register")
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
                
                String token = jwtUtil.generateToken(userId, email);
                Map<String, String> response = new HashMap<>();
                response.put("token", token);
                response.put("message", "User verified successfully");
        
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

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody LoginRequest loginRequest) {
        User user = userDAO.getUserByEmail(loginRequest.getEmail());

        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        if (user == null || !passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid email or password");
        }
        
        userDAO.updateLastLogin(user.getEmail());

        // Generate JWT access token
        String accessToken = jwtUtil.generateToken(user.getUserid(), user.getEmail());
        
        // Generate refresh token with longer expiration
        String refreshToken = jwtUtil.generateRefreshToken(user.getUserid(), user.getEmail());
        
        // Create response with both tokens
        Map<String, String> response = new HashMap<>();
        response.put("token", accessToken);
        response.put("refreshToken", refreshToken);
        response.put("message", "Login successful");
        
        return ResponseEntity.ok(response);
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

    @PostMapping("/profile")
    public ResponseEntity<String> saveProfile(@RequestBody Profile profile) {
        // Validate the profile data if necessary
        if (profile.getMail() == null || profile.getMail().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Email is required");
        }

        User user = userDAO.getUserByEmail(profile.getMail());
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }

        // Save the profile using UserDAO
        int rows = userDAO.saveProfile(profile);
        if (rows > 0) {
            return ResponseEntity.ok("Profile saved successfully for email: " + profile.getMail());
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to save profile");
        }
    }

    @GetMapping("/profile/{email}")
    public ResponseEntity<Profile> getProfile(@PathVariable String email) {
        try {
            // First check if user exists
            User user = userDAO.getUserByEmail(email);
            if (user == null) {
                return ResponseEntity.ok(null);
            }
            Profile profile = userDAO.getProfileByEmail(email);            
            return ResponseEntity.ok(profile);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @PutMapping("/profile/{email}")
    public ResponseEntity<String> updateProfile(@PathVariable String email, @RequestBody Profile profile) {
        try {
            // First check if user exists
            User user = userDAO.getUserByEmail(email);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
            }
            
            // Ensure email in profile matches the path variable
            profile.setMail(email);
            
            // Check if profile exists
            Profile existingProfile = userDAO.getProfileByEmail(email);
            if (existingProfile == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Profile not found for user");
            }
            
            // Update the profile
            int rowsUpdated = userDAO.updateProfile(profile);
            if (rowsUpdated > 0) {
                return ResponseEntity.ok("Profile updated successfully");
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to update profile");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An error occurred while updating profile: " + e.getMessage());
        }
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(@RequestBody RefreshTokenRequest refreshTokenRequest) {
        String refreshToken = refreshTokenRequest.getRefreshToken();
        
        // Validate refresh token
        if (!jwtUtil.validateRefreshToken(refreshToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid or expired refresh token");
        }
        
        // Extract claims from refresh token
        String userId = jwtUtil.extractUserIdFromRefreshToken(refreshToken);
        String email = jwtUtil.extractEmailFromRefreshToken(refreshToken);
        
        // Generate new access token
        String newAccessToken = jwtUtil.generateToken(userId, email);
        
        // Create response with new access token
        Map<String, String> response = new HashMap<>();
        response.put("token", newAccessToken);
        response.put("message", "Token refreshed successfully");
        
        System.out.println("New access token: " + newAccessToken);
        return ResponseEntity.ok(response);
    }

    // Webmail configuration - Replace with your actual webmail settings
    @Value("${email.host}")
    private String emailHost;

    @Value("${email.port}")
    private int emailPort;

    @Value("${email.username}")
    private String emailUsername;

    @Value("${email.password}")
    private String emailPassword;

    @Value("${email.ssl}")
    private boolean useSSL;
    @PostMapping("/otp")
    public ResponseEntity<Map<String, Object>> generateOtp(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String type = request.get("type");
        Map<String, Object> response = new HashMap<>();
        
        if (email == null || email.isEmpty()) {
            response.put("status", "error");
            response.put("message", "Email is required");
            return ResponseEntity.badRequest().body(response);
        }
        
        if(type != null && type.equals("signup")){
             
            try {                
                // Generate OTP - 8 digit random number
                String otp = generateOtp();
                
                // Encode the OTP using BCrypt
                PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
                String encodedOtp = passwordEncoder.encode(otp);
                
                // Create a JWT token containing the encoded OTP with 10-minute expiration
                Map<String, Object> claims = new HashMap<>();
                claims.put("email", email);
                claims.put("encodedOtp", encodedOtp);
                claims.put("type", type);
                
                // Generate token with 10-minute expiration
                String otpToken = jwtUtil.generateTokenWithExpiration(claims, 10 * 60 * 1000); // 10 minutes in milliseconds
                
                // Send plaintext OTP via email (user needs to read it)
                sendOtpEmail(email, otp);
                
                // Return JWT token instead of plaintext OTP
                response.put("status", "success");
                response.put("otpToken", otpToken);
                
                return ResponseEntity.ok(response);
                
            } catch (Exception e) {
                response.put("status", "error");
                response.put("message", "Error: " + e.getMessage());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }
        } else if(type != null && type.equals("reset")){
            try {
                // Check if user exists
                boolean userExists = userDAO.emailExists(email);
                if (!userExists) {
                    response.put("status", "error");
                    response.put("message", "User not found");
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
                }
                
                // Generate OTP - 8 digit random number
                String otp = generateOtp();
                
                // Encode the OTP using BCrypt
                PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
                String encodedOtp = passwordEncoder.encode(otp);
                
                // Create a JWT token containing the encoded OTP with 10-minute expiration
                Map<String, Object> claims = new HashMap<>();
                claims.put("email", email);
                claims.put("encodedOtp", encodedOtp);
                claims.put("type", type);
                
                // Generate token with 10-minute expiration
                String otpToken = jwtUtil.generateTokenWithExpiration(claims, 10 * 60 * 1000); // 10 minutes in milliseconds
                
                // Send plaintext OTP via email (user needs to read it)
                sendOtpEmail(email, otp);
                
                // Return JWT token instead of plaintext OTP
                response.put("status", "success");
                response.put("otpToken", otpToken);
                
                return ResponseEntity.ok(response);
                
            } catch (Exception e) {
                response.put("status", "error");
                response.put("message", "Error: " + e.getMessage());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }
        }
        
        // If type is not "register" or "reset", return a bad request response
        response.put("status", "error");
        response.put("message", "Invalid request type");
        return ResponseEntity.badRequest().body(response);
    }

    private String generateOtp() {
        // Generate a random 8-digit number
        Random random = new Random();
        int otp = 10000000 + random.nextInt(90000000);
        return String.valueOf(otp);
    }

    private void sendOtpEmail(String to, String otp) throws MessagingException {
        Properties props = new Properties();
        
        // Configure email properties for webmail
        if (useSSL) {
            // SSL configuration
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.ssl.enable", "true");
            props.put("mail.smtp.host", emailHost);
            props.put("mail.smtp.port", emailPort);
        } else {
            // TLS configuration
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.host", emailHost);
            props.put("mail.smtp.port", emailPort);
        }
        
        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(emailUsername, emailPassword);
            }
        });
        
        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(emailUsername));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
        message.setSubject("Your NutriSift OTP Code");
        message.setText("Your OTP code is: " + otp + "\n\nThis code is valid for 10 minutes.");
        
        Transport.send(message);
    }
}