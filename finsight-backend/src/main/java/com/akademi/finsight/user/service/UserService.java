package com.akademi.finsight.user.service;

import com.akademi.finsight.user.dto.request.CreateUserRequest;
import com.akademi.finsight.user.dto.request.UpdateUserRequest;
import com.akademi.finsight.user.dto.response.UserResponse;
import com.akademi.finsight.user.dto.response.UserStatsResponse;
import com.akademi.finsight.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface UserService {

    Page<UserResponse> getUsers(String search, Boolean enabled, Pageable pageable);

    void createUser(CreateUserRequest request);

    UserResponse getCurrentUser(String email);

    UserResponse getUserById(UUID id);

    UserResponse updateUser(UUID id, UpdateUserRequest request);

    void changeUserStatus(UUID id, boolean enabled);

    void deleteUser(UUID id);

    User findByEmail(String email);

    User findByIdentifier(String identifier);

    void updateLastLogin(User user);

    void updatePassword(User user, String encodedPassword, boolean clearFirstLogin);

    UserStatsResponse getUserStats();
}
