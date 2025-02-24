package com.freelance_platform.user_ms.dto;

import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserRegisterDto {
    private String fullName;
    private String email;
    private String password;
    private List<String> roleNames;
}
