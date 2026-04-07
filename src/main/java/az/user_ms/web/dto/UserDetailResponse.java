package az.user_ms.web.dto;

import az.user_ms.domain.user.User;
import az.user_ms.domain.user.UserStatus;
import lombok.*;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDetailResponse {
    private UUID id;
    private String fullName;
    private String email;
    private UserStatus status;
    private Boolean emailVerified;
    private Set<String> roles;
    private Instant createdAt;
    private Instant updatedAt;

    public static UserDetailResponse fromUser(User user) {
        return UserDetailResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .status(user.getStatus())
                .emailVerified(user.getEmailVerified())
                .roles(user.getRoles().stream()
                        .map(role -> role.getName())
                        .collect(Collectors.toSet()))
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
