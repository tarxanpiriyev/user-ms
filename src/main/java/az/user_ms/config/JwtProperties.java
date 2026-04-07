package az.user_ms.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "jwt")
@Getter
@Setter
public class JwtProperties {
    private String secret;
    private Integer accessTokenExpiryMinutes;
    private Integer refreshTokenExpiryMinutes;
    private String issuer;
    private String audience;
}
