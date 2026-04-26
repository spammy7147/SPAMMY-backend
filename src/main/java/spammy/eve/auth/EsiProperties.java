// src/main/java/spammy/eve/config/EsiProperties.java
package spammy.eve.auth;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "spammy.esi")
public class EsiProperties {
    private String clientId;
    private String clientSecret;
    private String callbackUrl;
    private String scopes;
}