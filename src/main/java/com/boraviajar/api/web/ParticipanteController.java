package com.boraviajar.api.web;

import com.boraviajar.api.service.OrganizerRatingService;
import com.boraviajar.api.service.ParticipanteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/participantes")
@RequiredArgsConstructor
public class ParticipanteController {

    private final ParticipanteService participanteService;
    private final OrganizerRatingService organizerRatingService;

    @PostMapping("/join")
    public Map<String, Object> join(@RequestBody ViagemIdBody body) {
        return participanteService.join(body.viagemId(), CurrentUser.require());
    }

    @PostMapping("/leave")
    public Map<String, Boolean> leave(@RequestBody ViagemIdBody body) {
        return participanteService.leave(body.viagemId(), CurrentUser.require());
    }

    @GetMapping("/viagem/{viagemId}/count")
    public Map<String, Long> count(@PathVariable long viagemId) {
        return Map.of("count", participanteService.count(viagemId));
    }

    @GetMapping("/viagem/{viagemId}/user/{userId}")
    public Map<String, Boolean> isParticipante(@PathVariable long viagemId, @PathVariable long userId) {
        return Map.of("participante", participanteService.isParticipante(viagemId, userId));
    }

    @PatchMapping("/{participanteId}/status")
    public Map<String, Object> updateStatusPatch(
            @PathVariable long participanteId,
            @RequestBody StatusBody body) {
        return participanteService.updateStatus(participanteId, body.status(), CurrentUser.require());
    }

    /** POST — alguns proxies bloqueiam PATCH. */
    @PostMapping("/{participanteId}/status")
    public Map<String, Object> updateStatusPost(
            @PathVariable long participanteId,
            @RequestBody StatusBody body) {
        return participanteService.updateStatus(participanteId, body.status(), CurrentUser.require());
    }

    /** POST — path sem {@code rating} (evita bloqueio nginx/WAF). */
    @PostMapping("/avaliar-organizador")
    public Map<String, Object> submitOrganizerRating(@RequestBody OrganizerRatingBody body) {
        if (body.viagemId() == null || body.viagemId() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Campo viagemId é obrigatório");
        }
        if (body.stars() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Campo stars é obrigatório");
        }
        return organizerRatingService.submit(
                body.viagemId(), CurrentUser.require(), body.stars(), body.testemunho());
    }

    public record ViagemIdBody(long viagemId) {}

    public record StatusBody(String status) {}

    public record OrganizerRatingBody(Long viagemId, Integer stars, String testemunho) {}
}
