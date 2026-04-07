package az.user_ms.web.controller;

import az.user_ms.config.JwtProperties;
import az.user_ms.domain.user.User;
import az.user_ms.service.AuthService;
import az.user_ms.service.UserService;
import az.user_ms.web.dto.LoginRequest;
import az.user_ms.web.dto.LoginResponse;
import az.user_ms.web.dto.RegisterRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "User registration and login")
public class AuthController {

    private final UserService userService;
    private final AuthService authService;
    private final JwtProperties jwtProperties;

    @PostMapping("/register")
    @Operation(summary = "Register new user", description = "Create a new user account with USER role")
    public ResponseEntity<LoginResponse> register(@Valid @RequestBody RegisterRequest request) {
        // Register user
        User user = userService.registerUser(
                request.getFullName(),
                request.getEmail(),
                request.getPassword());

        // Auto-login after registration
        AuthService.LoginResponse authResponse = authService.login(request.getEmail(), request.getPassword());

        return ResponseEntity.ok(LoginResponse.builder()
                .userId(authResponse.getUserId())
                .email(authResponse.getEmail())
                .fullName(authResponse.getFullName())
                .accessToken(authResponse.getAccessToken())
                .refreshToken(authResponse.getRefreshToken())
                .expiresIn(jwtProperties.getAccessTokenExpiryMinutes() * 60L)
                .build());
    }

    @PostMapping("/login")
    @Operation(summary = "User login", description = "Authenticate user and return JWT tokens with roles")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthService.LoginResponse authResponse = authService.login(request.getEmail(), request.getPassword());

        return ResponseEntity.ok(LoginResponse.builder()
                .userId(authResponse.getUserId())
                .email(authResponse.getEmail())
                .fullName(authResponse.getFullName())
                .accessToken(authResponse.getAccessToken())
                .refreshToken(authResponse.getRefreshToken())
                .expiresIn(jwtProperties.getAccessTokenExpiryMinutes() * 60L)
                .build());
    }
}
