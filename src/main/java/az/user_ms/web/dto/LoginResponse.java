package az.user_ms.web.dto;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginResponse {
    private UUID userId;
    private String email;
    private String fullName;
    private String accessToken;
    private String refreshToken;
    private Long expiresIn; // seconds
}
