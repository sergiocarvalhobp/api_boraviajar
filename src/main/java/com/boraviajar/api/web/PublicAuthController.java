package com.boraviajar.api.web;

import com.boraviajar.api.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Login mobile — rota alternativa a {@code POST /api/v1/auth/token} para contornar
 * regras de nginx que bloqueiam {@code /api/v1/auth/*} na borda HTTPS.
 */
@RestController
@RequestMapping("/api/v1/public")
public class PublicAuthController {

    private final AuthService authService;

    public PublicAuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/session")
    public ResponseEntity<Map<String, String>> createSession(
            @RequestBody AuthController.TokenRequest body) {
        return ResponseEntity.ok(authService.exchangeAuth0AccessToken(body.accessToken()));
    }
}
