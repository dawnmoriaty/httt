package dawn.httt.server.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.bootstrap")
public class AppBootstrapProperties {

    private String adminUsername;
    private String adminPassword;
    private String adminEmail;
    private String adminFullName;
}
