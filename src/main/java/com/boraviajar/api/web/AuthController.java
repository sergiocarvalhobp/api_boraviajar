package com.boraviajar.api.web;

import com.boraviajar.api.entity.User;
import com.boraviajar.api.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Mobile (Flutter): troca {@code access_token} do Auth0 pelo JWT de sessão Bora Viajar.
     */
    @PostMapping("/token")
    public ResponseEntity<Map<String, String>> token(@RequestBody TokenRequest body) {
        return ResponseEntity.ok(authService.exchangeAuth0AccessToken(body.accessToken()));
    }

    @GetMapping("/me")
    public ResponseEntity<User> me() {
        return ResponseEntity.ok(CurrentUser.optionalOrNull());
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, Boolean>> logout() {
        return ResponseEntity.ok(Map.of("success", true));
    }

    public record TokenRequest(String access_token) {
        public String accessToken() {
            return access_token;
        }
    }
}
