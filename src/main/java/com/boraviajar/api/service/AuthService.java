package com.boraviajar.api.service;

import com.boraviajar.api.config.BoraviajarProperties;
import com.boraviajar.api.entity.User;
import com.boraviajar.api.repo.UserRepository;
import com.boraviajar.api.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final long SESSION_TTL_MS = 365L * 24 * 60 * 60 * 1000;

    private final BoraviajarProperties properties;
    private final Auth0UserInfoClient auth0UserInfoClient;
    private final UserRepository userRepository;
    private final JwtService jwtService;

    @Transactional
    public Map<String, String> exchangeAuth0AccessToken(String accessToken) {
        if (!properties.isAuth0Configured()) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Auth0 não configurado no servidor.");
        }
        if (accessToken == null || accessToken.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "access_token é obrigatório.");
        }

        var profile = auth0UserInfoClient.fetchUserInfo(properties.auth0().domain(), accessToken.trim());
        if (profile == null || profile.sub() == null || profile.sub().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "sub ausente no userinfo do Auth0.");
        }

        String displayName = resolveDisplayName(profile);
        User user = upsertUser(profile, displayName);

        String appId = properties.auth0().clientId();
        if (appId == null || appId.isBlank()) {
            appId = "mobile";
        }

        String sessionToken = jwtService.createSessionToken(
                user.getOpenId(),
                displayName,
                user.getEmail(),
                appId,
                SESSION_TTL_MS);

        return Map.of("sessionToken", sessionToken);
    }

    private static String resolveDisplayName(Auth0UserInfoClient.Auth0Profile profile) {
        if (profile.name() != null && !profile.name().isBlank()) {
            return profile.name().trim();
        }
        if (profile.nickname() != null && !profile.nickname().isBlank()) {
            return profile.nickname().trim();
        }
        if (profile.email() != null && profile.email().contains("@")) {
            return profile.email().substring(0, profile.email().indexOf('@'));
        }
        return "Viajante";
    }

    private User upsertUser(Auth0UserInfoClient.Auth0Profile profile, String name) {
        Instant now = Instant.now();
        String openId = profile.sub();
        String email = profile.email();
        Optional<User> existing = userRepository.findByOpenId(openId);
        if (existing.isPresent()) {
            User user = existing.get();
            user.setName(name);
            if (email != null) {
                user.setEmail(email);
            }
            applyGoogleAvatarIfMissing(user, profile.picture());
            user.setLoginMethod("google");
            user.setLastSignedIn(now);
            user.setUpdatedAt(now);
            return userRepository.save(user);
        }
        User user = new User();
        user.setOpenId(openId);
        user.setName(name);
        user.setEmail(email);
        applyGoogleAvatarIfMissing(user, profile.picture());
        user.setLoginMethod("google");
        user.setRole("user");
        user.setCreatedAt(now);
        user.setUpdatedAt(now);
        user.setLastSignedIn(now);
        return userRepository.save(user);
    }

    /**
     * Preenche avatarUrl com a foto HTTPS do Google/Auth0 apenas se o usuário
     * ainda não tiver avatar (ex.: upload manual em base64 não é sobrescrito).
     */
    private static void applyGoogleAvatarIfMissing(User user, String picture) {
        if (picture == null || picture.isBlank()) {
            return;
        }
        String current = user.getAvatarUrl();
        if (current != null && !current.isBlank()) {
            return;
        }
        user.setAvatarUrl(picture.trim());
    }
}
