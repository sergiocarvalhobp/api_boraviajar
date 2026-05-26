package com.boraviajar.api.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/system")
public class SystemController {

    @GetMapping("/health")
    public Map<String, Boolean> systemHealth(@RequestParam long timestamp) {
        return Map.of("ok", true);
    }

    /**
     * Diagnóstico de deploy: se este endpoint responder 200 com {@code mobileAuthTokenPublic=true}
     * mas {@code POST /api/v1/auth/token} ainda der 403, o bloqueio é no nginx (não no Spring).
     */
    @GetMapping("/deploy-check")
    public Map<String, Object> deployCheck() {
        return Map.of(
                "api", "api-boraviajar",
                "version", "0.1.0-SNAPSHOT",
                "securityRules", "2026-05-20-ratings-chat-v1",
                "mobileAuthTokenPublic", true,
                "mobileAuthQueryFallback", true,
                "mobileAuthHeaderFallback", true,
                "features", Map.of(
                        "authAvaliarViagemPost", true,
                        "authTripRatingPost", true,
                        "organizerRating", true,
                        "postOrganizerRating", true,
                        "participanteOrganizerRatingPost", true,
                        "participanteStatusPost", true,
                        "chatConfirmadoOnly", true,
                        "chatReadOnlyAfterTripEnd", true));
    }
}
