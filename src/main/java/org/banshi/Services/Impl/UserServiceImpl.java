package org.banshi.Services.Impl;

import lombok.RequiredArgsConstructor;
import org.banshi.Dtos.*;
import org.banshi.Entities.Enums.Role;
import org.banshi.Entities.User;
import org.banshi.Repositories.UserRepository;
import org.banshi.Services.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public SignUpResponse signUp(SignUpRequest request) {
        if (userRepository.existsByPhone(request.getPhone())) {
            throw new IllegalArgumentException("Phone number already registered.");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email is already registered.");
        }

        User user = User.builder()
                .name(request.getName())
                .phone(request.getPhone())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .balance(0.0)
                .role(Role.USER)
                .build();

        user = userRepository.save(user);

        return SignUpResponse.builder()
                .userId(user.getUserId())
                .name(user.getName())
                .phone(user.getPhone())
                .email(user.getEmail())
                .build();
    }

    @Override
    public SignInResponse signIn(SignInRequest request) {
        User user = userRepository.findByPhone(request.getPhone())
                .orElseThrow(() -> new IllegalArgumentException("User not found with phone: " + request.getPhone()));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Incorrect password.");
        }

        return SignInResponse.builder()
                .userId(user.getUserId())
                .name(user.getName())
                .phone(user.getPhone())
                .email(user.getEmail())
                .build();
    }

    @Override
    public User updateUser(Long userId, UserUpdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));

        user.setName(request.getName());

        return userRepository.save(user);
    }

    @Override
    public void changePassword(Long userId, ChangePasswordRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Old password does not match.");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    @Override
    public User getUserByUserId(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));
    }

    @Override
    public User getUserByPhone(String phone) {
        return userRepository.findByPhone(phone)
                .orElseThrow(() -> new IllegalArgumentException("User not found with phone: " + phone));
    }

    @Override
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found with email: " + email));
    }

    @Override
    public List<User> getAllUsers() {
        List<User> users = userRepository.findAll();
        if (users.isEmpty()) {
            throw new IllegalArgumentException("No users found.");
        }
        return users;
    }

    @Override
    public double getUserBalance(Long userId) {
        User user = getUserByUserId(userId);

        if (user.getBalance() == null) {
            throw new IllegalArgumentException("User balance is null.");
        }
        return user.getBalance();
    }
}
