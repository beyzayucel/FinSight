package com.akademi.finsight.user.controller;

import com.akademi.finsight.common.controller.BaseController;
import com.akademi.finsight.common.response.ApiStandardResponse;
import com.akademi.finsight.common.response.PageResponse;
import com.akademi.finsight.user.controller.api.UserApi;
import com.akademi.finsight.user.dto.request.CreateUserRequest;
import com.akademi.finsight.user.dto.request.UpdateUserRequest;
import com.akademi.finsight.user.dto.response.UserResponse;
import com.akademi.finsight.user.dto.response.UserStatsResponse;
import com.akademi.finsight.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class UserController extends BaseController implements UserApi {

    private final UserService userService;

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiStandardResponse<PageResponse<UserResponse>>> getUsers(String search, Boolean enabled, Pageable pageable) {
        return ok(PageResponse.of(userService.getUsers(search, enabled, pageable)));
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiStandardResponse<Void>> createUser(@Valid @RequestBody CreateUserRequest request) {
        userService.createUser(request);
        return created();
    }

    @Override
    public ResponseEntity<ApiStandardResponse<UserResponse>> getCurrentUser(@AuthenticationPrincipal String email) {
        return ok(userService.getCurrentUser(email));
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiStandardResponse<UserResponse>> getUserById(@PathVariable UUID id) {
        return ok(userService.getUserById(id));
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiStandardResponse<UserResponse>> updateUser(@PathVariable UUID id,
                                                                        @Valid @RequestBody UpdateUserRequest request) {
        return ok(userService.updateUser(id, request));
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiStandardResponse<Void>> changeUserStatus(@PathVariable UUID id,
                                                                       @RequestParam boolean enabled,
                                                                       @AuthenticationPrincipal String email) {
        userService.changeUserStatus(id, enabled, email);
        return ok();
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiStandardResponse<Void>> deleteUser(@PathVariable UUID id,
                                                                 @AuthenticationPrincipal String email) {
        userService.deleteUser(id, email);
        return ok();
    }


    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiStandardResponse<Void>> resendVerification(@PathVariable UUID id) {
        userService.resendVerification(id);
        return ok();
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiStandardResponse<UserStatsResponse>> getUserStats() {
        return ok(userService.getUserStats());
    }
}
