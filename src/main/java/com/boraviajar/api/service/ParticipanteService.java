package com.boraviajar.api.service;

import com.boraviajar.api.entity.Participante;
import com.boraviajar.api.entity.User;
import com.boraviajar.api.entity.Viagem;
import com.boraviajar.api.repo.ParticipanteRepository;
import com.boraviajar.api.repo.ViagemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ParticipanteService {

    private final ParticipanteRepository participanteRepository;
    private final ViagemRepository viagemRepository;

    public long count(long viagemId) {
        return participanteRepository.countByViagemId(viagemId);
    }

    public boolean isParticipante(long viagemId, long userId) {
        return participanteRepository.findByViagemIdAndUserId(viagemId, userId).isPresent();
    }

    @Transactional
    public Map<String, Object> join(long viagemId, User user) {
        if (participanteRepository.findByViagemIdAndUserId(viagemId, user.getId()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Você já é participante desta viagem");
        }
        Viagem v = viagemRepository.findById(viagemId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Viagem não encontrada"));
        if (v.getLiderId().equals(user.getId())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "O organizador não pode participar da própria viagem");
        }
        if (v.getDataFim() != null && !v.getDataFim().isAfter(java.time.LocalDate.now())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Esta viagem já encerrou");
        }
        if (v.getMaxVagas() != null) {
            long c = participanteRepository.countByViagemId(viagemId);
            if (c >= v.getMaxVagas()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Esta viagem já está com todas as vagas preenchidas");
            }
        }
        Participante p = new Participante();
        p.setViagemId(viagemId);
        p.setUserId(user.getId());
        p.setStatus("interessado");
        Instant now = Instant.now();
        p.setCreatedAt(now);
        p.setUpdatedAt(now);
        p = participanteRepository.save(p);
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("id", p.getId());
        out.put("viagemId", p.getViagemId());
        out.put("userId", p.getUserId());
        out.put("status", p.getStatus());
        return out;
    }

    @Transactional
    public Map<String, Boolean> leave(long viagemId, User user) {
        Participante p = participanteRepository.findByViagemIdAndUserId(viagemId, user.getId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Você não está participando desta viagem"));
        participanteRepository.delete(p);
        return Map.of("success", true);
    }

    @Transactional
    public Map<String, Object> updateStatus(long participanteId, String status, User leader) {
        Participante p = participanteRepository.findById(participanteId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Participante não encontrado"));
        Viagem v = viagemRepository.findById(p.getViagemId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Viagem não encontrada"));
        if (!v.getLiderId().equals(leader.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Apenas o líder da viagem pode alterar o status");
        }
        if (status == null || status.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Status é obrigatório");
        }
        final String normalized = status.trim().toLowerCase();
        if (!normalized.equals("interessado")
                && !normalized.equals("confirmado")
                && !normalized.equals("recusado")) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Status inválido. Use: interessado, confirmado ou recusado");
        }
        p.setStatus(normalized);
        p.setUpdatedAt(Instant.now());
        p = participanteRepository.save(p);
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("id", p.getId());
        out.put("viagemId", p.getViagemId());
        out.put("userId", p.getUserId());
        out.put("status", p.getStatus());
        return out;
    }
}
