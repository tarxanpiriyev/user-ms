package com.freelance_platform.user_ms.controller;

import com.freelance_platform.user_ms.dto.UserRegisterDto;
import com.freelance_platform.user_ms.dto.security.AuthRequestDTO;
import com.freelance_platform.user_ms.dto.security.JwtResponseDTO;
import com.freelance_platform.user_ms.model.User;
import com.freelance_platform.user_ms.service.AuthServiceImpl;
import com.freelance_platform.user_ms.service.JWTService;
import com.freelance_platform.user_ms.service.UserServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthServiceImpl authService;
    private final UserServiceImpl userService;

    @PostMapping("/register")
    public void register(@RequestBody UserRegisterDto userRegisterDto) {
        userService.registerUser(userRegisterDto);
    }

    @PostMapping("/login")
    public JwtResponseDTO login(@RequestBody AuthRequestDTO authRequestDTO) {
        return authService.login(authRequestDTO);
    }
}
