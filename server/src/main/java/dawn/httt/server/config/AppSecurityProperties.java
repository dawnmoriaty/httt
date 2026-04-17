package dawn.httt.server.config;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Duration;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "app.security")
public class AppSecurityProperties {

    @NotBlank
    @Size(min = 32)
    private String jwtSecret;

    @NotNull
    private Duration accessTokenDuration;

    @NotNull
    private Duration refreshTokenDuration;

    @AssertTrue(message = "accessTokenDuration must be positive")
    public boolean isAccessTokenDurationValid() {
        return accessTokenDuration != null && !accessTokenDuration.isNegative() && !accessTokenDuration.isZero();
    }

    @AssertTrue(message = "refreshTokenDuration must be positive")
    public boolean isRefreshTokenDurationValid() {
        return refreshTokenDuration != null && !refreshTokenDuration.isNegative() && !refreshTokenDuration.isZero();
    }
}
