package com.akademi.finsight.user.controller;

import com.akademi.finsight.common.controller.BaseController;
import com.akademi.finsight.common.response.ApiStandardResponse;
import com.akademi.finsight.user.controller.api.UserApi;
import com.akademi.finsight.user.dto.CreateUserRequest;
import com.akademi.finsight.user.dto.CreateUserResponse;
import com.akademi.finsight.user.dto.UpdateProfileRequest;
import com.akademi.finsight.user.dto.UserResponse;
import com.akademi.finsight.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class UserController extends BaseController implements UserApi {

    private final UserService userService;

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiStandardResponse<CreateUserResponse>> createUser(@Valid @RequestBody CreateUserRequest request) {
        return created(userService.createUser(request));
    }

    @Override
    public ResponseEntity<ApiStandardResponse<UserResponse>> getCurrentUser(@AuthenticationPrincipal String email) {
        return ok(userService.getCurrentUser(email));
    }

    @Override
    public ResponseEntity<ApiStandardResponse<UserResponse>> updateCurrentUser(@Valid @RequestBody UpdateProfileRequest request,
                                                                       @AuthenticationPrincipal String email) {
        return ok(userService.updateCurrentUser(email, request));
    }


}
