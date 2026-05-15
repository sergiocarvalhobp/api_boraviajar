package com.boraviajar.api.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "boraviajar")
public record BoraviajarProperties(
        Jwt jwt,
        Auth0 auth0,
        Vapid vapid
) {
    public record Jwt(String secret) {}

    public record Auth0(String domain, String clientId) {}

    public record Vapid(String publicKey) {}

    public boolean isAuth0Configured() {
        return auth0 != null
                && auth0.domain() != null
                && !auth0.domain().isBlank()
                && jwt != null
                && jwt.secret() != null
                && !jwt.secret().isBlank();
    }
}
