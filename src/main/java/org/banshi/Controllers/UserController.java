package org.banshi.Controllers;

import org.banshi.Dtos.*;
import org.banshi.Entities.User;
import org.banshi.Services.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user/")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;

    // SignUp
    @PostMapping("/signUp")
    public ResponseEntity<ApiResponse<SignUpResponse>> signUp(@RequestBody SignUpRequest signUpRequest) {
        logger.info("Received sign-up request for email: {}", signUpRequest.getEmail());
        try {
            SignUpResponse response = userService.signUp(signUpRequest);
            logger.info("User signed up successfully: {}", response);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse<>("SUCCESS", "Sign Up Successfully", response));
        } catch (Exception e) {
            logger.error("Error during sign-up for email {}: {}", signUpRequest.getEmail(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>("ERROR", e.getMessage(), null));
        }
    }

    // SignIn
    @PostMapping("/signIn")
    public ResponseEntity<ApiResponse<SignInResponse>> signIn(@RequestBody SignInRequest signInRequest) {
        logger.info("Received sign-in request for phone: {}", signInRequest.getPhone());
        try {
            SignInResponse response = userService.signIn(signInRequest);
            logger.info("User signed in successfully: {}", response.getUserId());
            return ResponseEntity.ok(new ApiResponse<>("SUCCESS", "Signin successful", response));
        } catch (Exception e) {
            logger.warn("Sign-in failed for phone {}: {}", signInRequest.getPhone(), e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>("ERROR", e.getMessage(), null));
        }
    }

    // Update user
    @PutMapping("/{userId}/update")
    public ResponseEntity<ApiResponse<User>> updateUser(@PathVariable Long userId, @RequestBody UserUpdateRequest request) {
        logger.info("Received update request for userId: {}", userId);
        try {
            User updatedUser = userService.updateUser(userId, request);
            logger.info("User updated successfully: {}", userId);
            return ResponseEntity.ok(new ApiResponse<>("SUCCESS", "User updated successfully", updatedUser));
        } catch (Exception e) {
            logger.error("Error updating user {}: {}", userId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>("ERROR", e.getMessage(), null));
        }
    }

    // Change password
    @PutMapping("/{userId}/change-password")
    public ResponseEntity<ApiResponse<String>> changePassword(@PathVariable Long userId, @RequestBody ChangePasswordRequest request) {
        logger.info("Received password change request for userId: {}", userId);
        try {
            userService.changePassword(userId, request);
            logger.info("Password changed successfully for userId: {}", userId);
            return ResponseEntity.ok(new ApiResponse<>("SUCCESS", "Password changed successfully", null));
        } catch (Exception e) {
            logger.warn("Failed to change password for userId {}: {}", userId, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>("ERROR", e.getMessage(), null));
        }
    }

    // Get user by ID
    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<User>> getUserByUserId(@PathVariable Long userId) {
        logger.info("Fetching user by userId: {}", userId);
        try {
            User user = userService.getUserByUserId(userId);
            logger.info("User fetched successfully: {}", userId);
            return ResponseEntity.ok(new ApiResponse<>("SUCCESS", "User fetched successfully", user));
        } catch (Exception e) {
            logger.error("User not found with userId {}: {}", userId, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>("ERROR", e.getMessage(), null));
        }
    }

    // Get user by phone
    @GetMapping("/phone/{phone}")
    public ResponseEntity<ApiResponse<User>> getUserByPhone(@PathVariable String phone) {
        logger.info("Fetching user by phone: {}", phone);
        try {
            User user = userService.getUserByPhone(phone);
            logger.info("User fetched successfully by phone: {}", phone);
            return ResponseEntity.ok(new ApiResponse<>("SUCCESS", "User fetched successfully", user));
        } catch (Exception e) {
            logger.warn("User not found with phone {}: {}", phone, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>("ERROR", e.getMessage(), null));
        }
    }

    // Get user by email
    @GetMapping("/email/{email}")
    public ResponseEntity<ApiResponse<User>> getUserByEmail(@PathVariable String email) {
        logger.info("Fetching user by email: {}", email);
        try {
            User user = userService.getUserByEmail(email);
            logger.info("User fetched successfully by email: {}", email);
            return ResponseEntity.ok(new ApiResponse<>("SUCCESS", "User fetched successfully", user));
        } catch (Exception e) {
            logger.warn("User not found with email {}: {}", email, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>("ERROR", e.getMessage(), null));
        }
    }

    // Get all users
    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<User>>> getAllUsers() {
        logger.info("Fetching all users");
        try {
            List<User> users = userService.getAllUsers();
            if (users.isEmpty()) {
                logger.warn("No users found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ApiResponse<>("ERROR", "No users found", null));
            }
            logger.info("Fetched {} users", users.size());
            return ResponseEntity.ok(new ApiResponse<>("SUCCESS", "Users fetched successfully", users));
        } catch (Exception e) {
            logger.error("Error fetching users: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>("ERROR", e.getMessage(), null));
        }
    }

    // Get user balance
    @GetMapping("/{userId}/balance")
    public ResponseEntity<ApiResponse<Double>> getUserBalance(@PathVariable Long userId) {
        logger.info("Fetching balance for userId: {}", userId);
        try {
            double balance = userService.getUserBalance(userId);
            logger.info("Balance for userId {}: {}", userId, balance);
            return ResponseEntity.ok(new ApiResponse<>("SUCCESS", "User balance fetched", balance));
        } catch (Exception e) {
            logger.warn("Failed to fetch balance for userId {}: {}", userId, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>("ERROR", e.getMessage(), null));
        }
    }
}
