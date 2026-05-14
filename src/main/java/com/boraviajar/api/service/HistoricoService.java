package com.boraviajar.api.service;

import com.boraviajar.api.entity.Participante;
import com.boraviajar.api.entity.User;
import com.boraviajar.api.entity.Viagem;
import com.boraviajar.api.repo.ParticipanteRepository;
import com.boraviajar.api.repo.UserRepository;
import com.boraviajar.api.repo.ViagemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HistoricoService {

    private final ViagemRepository viagemRepository;
    private final ParticipanteRepository participanteRepository;

    public Map<String, Object> meuHistorico(User user) {
        List<Viagem> criadas = viagemRepository.findAllByLiderIdOrderByCreatedAtDesc(user.getId());
        Set<Long> ids = participanteRepository.findByUserId(user.getId()).stream()
                .map(Participante::getViagemId)
                .collect(Collectors.toSet());
        List<Viagem> todas = viagemRepository.findAllByOrderByCreatedAtDesc();
        List<Viagem> participando = todas.stream()
                .filter(v -> ids.contains(v.getId()) && !v.getLiderId().equals(user.getId()))
                .toList();
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("criadas", criadas);
        out.put("participando", participando);
        return out;
    }
}
