package az.user_ms.service;

import az.user_ms.domain.role.Role;
import az.user_ms.domain.role.RoleRepository;
import az.user_ms.domain.user.User;
import az.user_ms.domain.user.UserRepository;
import az.user_ms.domain.user.UserStatus;
import az.user_ms.service.exception.EmailAlreadyExistsException;
import az.user_ms.service.exception.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public User registerUser(String fullName, String email, String password) {
        // Check if email already exists
        if (userRepository.existsByEmail(email)) {
            throw new EmailAlreadyExistsException("Email already registered: " + email);
        }

        // Get or create USER role
        Role userRole = roleRepository.findByName("USER")
                .orElseGet(() -> {
                    Role newRole = Role.builder()
                            .name("USER")
                            .description("Regular user role")
                            .build();
                    return roleRepository.save(newRole);
                });

        Set<Role> roles = new HashSet<>();
        roles.add(userRole);

        // Create user with encoded password
        User user = User.builder()
                .fullName(fullName)
                .email(email)
                .password(passwordEncoder.encode(password))
                .status(UserStatus.ACTIVE)
                .emailVerified(false)
                .roles(roles)
                .build();

        User savedUser = userRepository.save(user);
        log.info("User registered successfully: {} with ID: {}", email, savedUser.getId());

        return savedUser;
    }

    @Transactional(readOnly = true)
    public User getUserById(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));
    }

    @Transactional(readOnly = true)
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));
    }

    @Transactional
    public User updateProfile(UUID userId, String fullName) {
        User user = getUserById(userId);
        user.setFullName(fullName);
        return userRepository.save(user);
    }

    @Transactional
    public void changePassword(UUID userId, String oldPassword, String newPassword) {
        User user = getUserById(userId);

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new IllegalArgumentException("Current password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        log.info("Password changed for user: {}", userId);
    }

    @Transactional
    public User addRoleToUser(UUID userId, String roleName) {
        User user = getUserById(userId);
        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new IllegalArgumentException("Role not found: " + roleName));

        user.getRoles().add(role);
        return userRepository.save(user);
    }
}
