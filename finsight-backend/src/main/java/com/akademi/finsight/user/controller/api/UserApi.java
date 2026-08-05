package com.akademi.finsight.user.controller.api;

import com.akademi.finsight.common.constants.ApiEndpoints;
import com.akademi.finsight.common.response.ApiStandardResponse;
import com.akademi.finsight.common.response.PageResponse;
import com.akademi.finsight.user.dto.request.CreateUserRequest;
import com.akademi.finsight.user.dto.request.UpdateUserRequest;
import com.akademi.finsight.user.dto.response.UserResponse;
import com.akademi.finsight.user.dto.response.UserStatsResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RequestMapping(ApiEndpoints.Users.BASE)
@Tag(
        name = "User Management",
        description = "User profile and admin operations"
)
public interface UserApi {

    @Operation(
            summary = "List users (Admin only)",
            description = "Returns a paginated list of users with optional search and status filter. Requires ADMIN role."
    )
    @ApiResponse(responseCode = "200", description = "Users retrieved successfully")
    @ApiResponse(responseCode = "403", description = "Access denied — requires ADMIN role")
    @GetMapping
    ResponseEntity<ApiStandardResponse<PageResponse<UserResponse>>> getUsers(
            @Parameter(description = "Search by email, username, first name, or last name") @RequestParam(required = false) String search,
            @Parameter(description = "Filter by enabled status") @RequestParam(required = false) Boolean enabled,
            @ParameterObject @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable);

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
            summary = "Get user by ID (Admin only)",
            description = "Returns a specific user's profile. Requires ADMIN role."
    )
    @ApiResponse(responseCode = "200", description = "User retrieved successfully")
    @ApiResponse(responseCode = "403", description = "Access denied — requires ADMIN role")
    @ApiResponse(responseCode = "404", description = "User not found")
    @GetMapping(ApiEndpoints.Users.BY_ID)
    ResponseEntity<ApiStandardResponse<UserResponse>> getUserById(@PathVariable UUID id);

    @Operation(
            summary = "Update user (Admin only)",
            description = "Updates a user's profile information. Requires ADMIN role."
    )
    @ApiResponse(responseCode = "200", description = "User updated successfully")
    @ApiResponse(responseCode = "400", description = "Validation error")
    @ApiResponse(responseCode = "403", description = "Access denied — requires ADMIN role")
    @ApiResponse(responseCode = "404", description = "User not found")
    @ApiResponse(responseCode = "409", description = "Phone number already exists")
    @PutMapping(ApiEndpoints.Users.BY_ID)
    ResponseEntity<ApiStandardResponse<UserResponse>> updateUser(@PathVariable UUID id,
                                                                  @Valid @RequestBody UpdateUserRequest request);

    @Operation(
            summary = "Toggle user status (Admin only)",
            description = "Enables or disables a user account. Requires ADMIN role."
    )
    @ApiResponse(responseCode = "200", description = "User status updated")
    @ApiResponse(responseCode = "403", description = "Access denied — requires ADMIN role")
    @ApiResponse(responseCode = "404", description = "User not found")
    @PatchMapping(ApiEndpoints.Users.STATUS)
    ResponseEntity<ApiStandardResponse<Void>> changeUserStatus(@PathVariable UUID id,
                                                                @RequestParam boolean enabled);

    @Operation(
            summary = "Delete user (Admin only)",
            description = "Soft-deletes a user account and revokes all sessions. Requires ADMIN role."
    )
    @ApiResponse(responseCode = "200", description = "User deleted successfully")
    @ApiResponse(responseCode = "403", description = "Access denied — requires ADMIN role")
    @ApiResponse(responseCode = "404", description = "User not found")
    @DeleteMapping(ApiEndpoints.Users.BY_ID)
    ResponseEntity<ApiStandardResponse<Void>> deleteUser(@PathVariable UUID id);


    @Operation(
            summary = "Resend verification email (Admin only)",
            description = "Resends the verification email with a new temporary password. Requires ADMIN role."
    )
    @ApiResponse(responseCode = "200", description = "Verification email resent")
    @ApiResponse(responseCode = "403", description = "Access denied — requires ADMIN role")
    @ApiResponse(responseCode = "404", description = "User not found")
    @ApiResponse(responseCode = "409", description = "Email is already verified")
    @PostMapping(ApiEndpoints.Users.RESEND_VERIFICATION)
    ResponseEntity<ApiStandardResponse<Void>> resendVerification(@PathVariable UUID id);

    @Operation(
            summary = "Get user statistics (Admin only)",
            description = "Returns user count statistics. Requires ADMIN role."
    )
    @ApiResponse(responseCode = "200", description = "Statistics retrieved successfully")
    @ApiResponse(responseCode = "403", description = "Access denied — requires ADMIN role")
    @GetMapping(ApiEndpoints.Users.STATS)
    ResponseEntity<ApiStandardResponse<UserStatsResponse>> getUserStats();
}
