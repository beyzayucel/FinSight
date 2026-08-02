package com.akademi.finsight.user.service;


import com.akademi.finsight.user.dto.CreateUserRequest;
import com.akademi.finsight.user.dto.UpdateProfileRequest;
import com.akademi.finsight.user.dto.UserResponse;
import com.akademi.finsight.user.entity.User;

public interface UserService {

    void createUser(CreateUserRequest request);

    UserResponse getCurrentUser(String email);

    User findByEmail(String email);

    User findByIdentifier(String identifier);

    void updateLastLogin(User user);

    void updatePassword(User user, String encodedPassword, boolean clearFirstLogin);

    UserResponse updateCurrentUser(String email, UpdateProfileRequest request);

    void deleteCurrentUser(String email);
}
