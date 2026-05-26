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

    /** Mobile: confirma que POST autenticado chega ao Spring (diagnóstico de proxy/nginx). */
    @PostMapping("/session-check")
    public ResponseEntity<Map<String, Object>> sessionCheck() {
        User u = CurrentUser.require();
        return ResponseEntity.ok(Map.of("ok", true, "userId", u.getId()));
    }

    /**
     * Salvar avaliação do organizador — mesmo prefixo de {@link #sessionCheck()},
     * onde POST autenticado já funciona no mobile.
     */
    @PostMapping("/trip-rating")
    public ResponseEntity<Map<String, Object>> submitTripRating(@RequestBody TripRatingBody body) {
        if (body.viagemId() == null || body.viagemId() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Campo viagemId é obrigatório");
        }
        if (body.stars() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Campo stars é obrigatório");
        }
        return ResponseEntity.ok(organizerRatingService.submit(
                body.viagemId(), CurrentUser.require(), body.stars(), body.testemunho()));
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

    public record TripRatingBody(Long viagemId, Integer stars, String testemunho) {}
}
