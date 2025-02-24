package com.freelance_platform.user_ms.service;

import com.freelance_platform.user_ms.dto.UserRegisterDto;
import com.freelance_platform.user_ms.dto.UserResponseDto;
import com.freelance_platform.user_ms.model.Role;
import com.freelance_platform.user_ms.model.User;
import com.freelance_platform.user_ms.repository.RoleRepository;
import com.freelance_platform.user_ms.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder bCryptPasswordEncoder;

    @Override
    public UserResponseDto getUserById(Long userId) {
        var user = userRepository.findById(userId).
                orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));
        return new UserResponseDto(user.getFullName(), user.getEmail());
    }

    public UserResponseDto getUserByEmail(String email) {
        var user = userRepository.findByEmailIgnoreCase(email).
                orElseThrow(() -> new EntityNotFoundException("User not found with email: " + email));
        return new UserResponseDto(user.getFullName(), user.getEmail());
    }

    public void registerUser(UserRegisterDto userRegisterDto) {
        var user = User.builder()
                .fullName(userRegisterDto.getFullName())
                .email(userRegisterDto.getEmail())
                .password(bCryptPasswordEncoder.encode(userRegisterDto.getPassword()))
                .roles(new HashSet<>())
                .build();

        userRegisterDto.getRoleNames().forEach(roleName -> {
            Role role = roleRepository.findByName(roleName)
                    .orElseThrow(() -> new EntityNotFoundException("Role not found: " + roleName));
            user.getRoles().add(role);
        });

        userRepository.save(user);
    }

//    public void updateUser(UserRegisterDto userRegisterDto) {
//
//    }

    public void deleteUserByEmail(String email) {
        var user = userRepository.findByEmailIgnoreCase(email).
                orElseThrow(() -> new EntityNotFoundException("User not found with email: " + email));
        userRepository.delete(user);
    }
}
