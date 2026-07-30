package com.akademi.finsight.user.controller.api;


import com.akademi.finsight.common.constants.ApiEndpoints;
import com.akademi.finsight.common.response.ApiStandardResponse;
import com.akademi.finsight.user.dto.CreateUserRequest;
import com.akademi.finsight.user.dto.UpdateProfileRequest;
import com.akademi.finsight.user.dto.UserResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RequestMapping(ApiEndpoints.Users.BASE)
@Tag(
        name = "User Management",
        description = "User profile and admin operations"
)
public interface UserApi {

    @Operation(
            summary = "Create a new user (Admin only)",
            description = "Creates a new user account with an auto-generated username and temporary password. Requires ADMIN role."
    )
    @ApiResponse(responseCode = "201", description = "User created successfully")
    @ApiResponse(responseCode = "400", description = "Validation error")
    @ApiResponse(responseCode = "403", description = "Access denied — requires ADMIN role")
    @ApiResponse(responseCode = "409", description = "Email or phone number already exists")
    @PostMapping
    ResponseEntity<ApiStandardResponse<Void>> createUser(@Valid @RequestBody CreateUserRequest request);

    @Operation(
            summary = "Get current user profile",
            description = "Returns the authenticated user's profile information."
    )
    @ApiResponse(responseCode = "200", description = "Profile retrieved successfully")
    @ApiResponse(responseCode = "401", description = "Not authenticated")
    @GetMapping(ApiEndpoints.Users.ME)
    ResponseEntity<ApiStandardResponse<UserResponse>> getCurrentUser(@AuthenticationPrincipal String email);

    @Operation(
            summary = "Update current user profile",
            description = "Updates the authenticated user's profile. Email and username cannot be changed."
    )
    @ApiResponse(responseCode = "200", description = "Profile updated successfully")
    @ApiResponse(responseCode = "400", description = "Validation error")
    @ApiResponse(responseCode = "401", description = "Not authenticated")
    @PutMapping(ApiEndpoints.Users.ME)
    ResponseEntity<ApiStandardResponse<UserResponse>> updateCurrentUser(@Valid @RequestBody UpdateProfileRequest request,
                                                                @AuthenticationPrincipal String email);

    @Operation(
            summary = "Delete current user account",
            description = "Soft-deletes the authenticated user's account and revokes all sessions."
    )
    @ApiResponse(responseCode = "200", description = "Account deleted successfully")
    @ApiResponse(responseCode = "401", description = "Not authenticated")
    @DeleteMapping(ApiEndpoints.Users.ME)
    ResponseEntity<ApiStandardResponse<Void>> deleteCurrentUser(@AuthenticationPrincipal String email);
}
