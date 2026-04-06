package az.user_ms.service;

import az.user_ms.domain.user.User;
import az.user_ms.domain.user.UserRepository;
import az.user_ms.service.exception.InvalidCredentialsException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final JWTService jwtService;

    @Transactional(readOnly = true)
    public LoginResponse login(String email, String password) {
        try {
            // Authenticate user
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, password));

            // Get user from database
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));

            // Generate tokens with roles
            String accessToken = jwtService.generateToken(user, false);
            String refreshToken = jwtService.generateToken(user, true);

            log.info("User {} logged in successfully", email);

            return LoginResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .userId(user.getId())
                    .email(user.getEmail())
                    .fullName(user.getFullName())
                    .build();

        } catch (AuthenticationException e) {
            log.warn("Failed login attempt for email: {}", email);
            throw new InvalidCredentialsException("Invalid email or password");
        }
    }

    @lombok.Builder
    @lombok.Getter
    public static class LoginResponse {
        private java.util.UUID userId;
        private String email;
        private String fullName;
        private String accessToken;
        private String refreshToken;
    }
}
