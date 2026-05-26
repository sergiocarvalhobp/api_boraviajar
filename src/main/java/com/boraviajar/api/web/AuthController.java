package com.boraviajar.api.web;

import com.boraviajar.api.entity.User;
import com.boraviajar.api.service.AuthService;
import com.boraviajar.api.service.OrganizerRatingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final OrganizerRatingService organizerRatingService;

    /**
     * Mobile (Flutter): troca {@code access_token} do Auth0 pelo JWT de sessão Bora Viajar.
     */
    @PostMapping("/token")
    public ResponseEntity<Map<String, String>> token(@RequestBody TokenRequest body) {
        return ResponseEntity.ok(authService.exchangeAuth0AccessToken(body.accessToken()));
    }

    @GetMapping("/me")
    public ResponseEntity<User> me() {
        return ResponseEntity.ok(CurrentUser.require());
    }

    /**
     * Mobile: diagnóstico de sessão POST e salvamento de avaliação.
     * <p>
     * Corpo vazio ou sem {@code stars} → {@code {ok, userId}}.
     * Com {@code viagemId} + {@code stars} → salva avaliação (mesma rota — nginx libera só este path).
     */
    @PostMapping("/session-check")
    public ResponseEntity<Map<String, Object>> sessionCheck(
            @RequestBody(required = false) SessionActionBody body) {
        User u = CurrentUser.require();
        if (body != null && body.viagemId() != null && body.viagemId() > 0 && body.stars() != null) {
            if (body.stars() < 1 || body.stars() > 5) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A nota deve ser entre 1 e 5");
            }
            return ResponseEntity.ok(
                    organizerRatingService.submit(body.viagemId(), u, body.stars(), body.testemunho()));
        }
        return ResponseEntity.ok(Map.of("ok", true, "userId", u.getId()));
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

    public record SessionActionBody(Long viagemId, Integer stars, String testemunho) {}
}
