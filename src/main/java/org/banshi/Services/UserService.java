package org.banshi.Services;

import org.banshi.Dtos.*;
import org.banshi.Entities.User;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface UserService {

    SignUpResponse signUp(SignUpRequest signUpRequest);

    SignInResponse signIn(SignInRequest signInRequest);

    User updateUser(Long userId, UserUpdateRequest request);

    void changePassword(Long userId, ChangePasswordRequest request);

    User getUserByPhone(String phone);

    User getUserByEmail(String email);

    User getUserByUserId(Long userId);

    List<User> getAllUsers();

    double getUserBalance(Long userId);
}
