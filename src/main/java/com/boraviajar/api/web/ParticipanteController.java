package com.boraviajar.api.web;

import com.boraviajar.api.service.ParticipanteService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/participantes")
@RequiredArgsConstructor
public class ParticipanteController {

    private final ParticipanteService participanteService;

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
    public Map<String, Object> updateStatus(
            @PathVariable long participanteId,
            @RequestBody StatusBody body) {
        return participanteService.updateStatus(participanteId, body.status(), CurrentUser.require());
    }

    public record ViagemIdBody(long viagemId) {}

    public record StatusBody(String status) {}
}
