package az.user_ms.web.controller;

import az.user_ms.domain.user.User;
import az.user_ms.service.UserService;
import az.user_ms.web.dto.ChangePasswordRequest;
import az.user_ms.web.dto.UpdateProfileRequest;
import az.user_ms.web.dto.UserDetailResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Tag(name = "User Management", description = "User profile and account management")
@SecurityRequirement(name = "Bearer Authentication")
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    @Operation(summary = "Get current user profile", description = "Retrieve authenticated user's profile information")
    public ResponseEntity<UserDetailResponse> getCurrentUser(Authentication authentication) {
        String email = authentication.getName();
        User user = userService.getUserByEmail(email);
        return ResponseEntity.ok(UserDetailResponse.fromUser(user));
    }

    @GetMapping("/{userId}")
    @Operation(summary = "Get user by ID", description = "Retrieve user profile by UUID")
    public ResponseEntity<UserDetailResponse> getUserById(@PathVariable UUID userId) {
        User user = userService.getUserById(userId);
        return ResponseEntity.ok(UserDetailResponse.fromUser(user));
    }

    @PutMapping("/me/profile")
    @Operation(summary = "Update profile", description = "Update current user's profile information")
    public ResponseEntity<UserDetailResponse> updateProfile(
            Authentication authentication,
            @Valid @RequestBody UpdateProfileRequest request) {
        String email = authentication.getName();
        User currentUser = userService.getUserByEmail(email);

        User updatedUser = userService.updateProfile(currentUser.getId(), request.getFullName());
        return ResponseEntity.ok(UserDetailResponse.fromUser(updatedUser));
    }

    @PostMapping("/me/change-password")
    @Operation(summary = "Change password", description = "Change current user's password")
    public ResponseEntity<Void> changePassword(
            Authentication authentication,
            @Valid @RequestBody ChangePasswordRequest request) {
        String email = authentication.getName();
        User currentUser = userService.getUserByEmail(email);

        userService.changePassword(
                currentUser.getId(),
                request.getCurrentPassword(),
                request.getNewPassword());

        return ResponseEntity.noContent().build();
    }
}
