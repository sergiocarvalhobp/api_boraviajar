package com.boraviajar.api.service;

import com.boraviajar.api.entity.Participante;
import com.boraviajar.api.entity.User;
import com.boraviajar.api.entity.Viagem;
import com.boraviajar.api.repo.ParticipanteRepository;
import com.boraviajar.api.repo.ViagemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class HistoricoService {

    private final ViagemRepository viagemRepository;
    private final ParticipanteRepository participanteRepository;
    private final TripService tripService;

    public Map<String, Object> meuHistorico(User user) {
        List<Map<String, Object>> criadas = viagemRepository
                .findAllByLiderIdOrderByCreatedAtDesc(user.getId())
                .stream()
                .map(v -> tripService.toTripMapForViewer(v, user))
                .toList();

        List<Map<String, Object>> participando = new ArrayList<>();
        for (Participante p : participanteRepository.findByUserId(user.getId())) {
            viagemRepository.findById(p.getViagemId()).ifPresent(v -> {
                if (!v.getLiderId().equals(user.getId())) {
                    Map<String, Object> row = new LinkedHashMap<>(
                            tripService.toTripMapForViewer(v, user));
                    row.put("myStatus", p.getStatus());
                    participando.add(row);
                }
            });
        }

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("criadas", criadas);
        out.put("participando", participando);
        return out;
    }
}
