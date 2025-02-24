package com.freelance_platform.user_ms.service;

import com.freelance_platform.user_ms.dto.UserResponseDto;

public interface UserService {

    UserResponseDto getUserById(Long userId);

    UserResponseDto getUserByEmail(String email);
}
