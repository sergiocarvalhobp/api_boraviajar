package com.boraviajar.api.service;

import com.boraviajar.api.entity.*;
import com.boraviajar.api.repo.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
public class TripService {

    private final ViagemRepository viagemRepository;
    private final UserRepository userRepository;
    private final ParticipanteRepository participanteRepository;
    @PersistenceContext
    private EntityManager entityManager;

    public List<Viagem> listAll() {
        return viagemRepository.findAllByOrderByCreatedAtDesc();
    }

    public List<Map<String, Object>> listAllEnriched(User viewer) {
        return listAll().stream().map(v -> toTripMap(v, viewer)).toList();
    }

    public List<Map<String, Object>> listByFilterEnriched(
            User viewer,
            String destino, String estado, String cidade, String atrativo,
            LocalDate dataInicio, LocalDate dataFim) {
        return listByFilter(destino, estado, cidade, atrativo, dataInicio, dataFim).stream()
                .map(v -> toTripMap(v, viewer))
                .toList();
    }

    public Optional<Map<String, Object>> findEnrichedById(long id, User viewer) {
        return findById(id).map(v -> toTripMap(v, viewer));
    }

    /** Equivalente a getViagensByFilter no Node. */
    public List<Viagem> listByFilter(String destino, String estado, String cidade, String atrativo,
                                     LocalDate dataInicio, LocalDate dataFim) {
        StringBuilder jpql = new StringBuilder("select v from Viagem v where 1=1");
        Map<String, Object> params = new HashMap<>();
        if (estado != null && !estado.isBlank()) {
            jpql.append(" and v.estado = :estado");
            params.put("estado", estado);
        }
        if (cidade != null && !cidade.isBlank()) {
            jpql.append(" and lower(v.cidade) like lower(concat('%', :cidade, '%'))");
            params.put("cidade", cidade);
        }
        if (atrativo != null && !atrativo.isBlank()) {
            jpql.append(" and lower(v.atrativo) like lower(concat('%', :atrativo, '%'))");
            params.put("atrativo", atrativo);
        }
        if (destino != null && !destino.isBlank()
                && (estado == null || estado.isBlank())
                && (cidade == null || cidade.isBlank())) {
            jpql.append(" and lower(v.destino) like lower(concat('%', :destino, '%'))");
            params.put("destino", destino);
        }
        if (dataInicio != null) {
            jpql.append(" and v.dataInicio >= :dataInicio");
            params.put("dataInicio", dataInicio);
        }
        if (dataFim != null) {
            jpql.append(" and v.dataFim <= :dataFim");
            params.put("dataFim", dataFim);
        }
        jpql.append(" order by v.dataInicio");
        TypedQuery<Viagem> q = entityManager.createQuery(jpql.toString(), Viagem.class);
        params.forEach(q::setParameter);
        return q.getResultList();
    }

    public Optional<Viagem> findById(long id) {
        return viagemRepository.findById(id);
    }

    public Map<String, Object> detailsWithParticipants(long viagemId) {
        Viagem v = viagemRepository.findById(viagemId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Viagem não encontrada"));
        User leader = userRepository.findById(v.getLiderId()).orElse(null);
        List<Participante> parts = participanteRepository.findByViagemId(viagemId);
        List<Map<String, Object>> withUsers = new ArrayList<>();
        for (Participante p : parts) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("id", p.getId());
            row.put("viagemId", p.getViagemId());
            row.put("userId", p.getUserId());
            row.put("status", p.getStatus());
            row.put("createdAt", p.getCreatedAt());
            row.put("updatedAt", p.getUpdatedAt());
            row.put("user", userRepository.findById(p.getUserId()).orElse(null));
            withUsers.add(row);
        }
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("viagem", v);
        out.put("leader", leader);
        out.put("participants", withUsers);
        out.put("participantCount", parts.size());
        return out;
    }

    @Transactional
    public Viagem create(User lider, CreateTripRequest in) {
        Instant now = Instant.now();
        Viagem v = new Viagem();
        v.setLiderId(lider.getId());
        v.setDestino(in.destino());
        v.setEstado(in.estado());
        v.setCidade(in.cidade());
        v.setAtrativo(in.atrativo());
        v.setDataInicio(in.dataInicio());
        v.setDataFim(in.dataFim());
        v.setDescricao(in.descricao());
        v.setTipo(in.tipo());
        v.setMaxVagas(in.maxVagas());
        v.setCreatedAt(now);
        v.setUpdatedAt(now);
        return viagemRepository.save(v);
    }

    public record CreateTripRequest(
            String destino,
            String estado,
            String cidade,
            String atrativo,
            LocalDate dataInicio,
            LocalDate dataFim,
            String descricao,
            String tipo,
            Integer maxVagas
    ) {}

    private Map<String, Object> toTripMap(Viagem v, User viewer) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", v.getId());
        m.put("liderId", v.getLiderId());
        m.put("destino", v.getDestino());
        m.put("estado", v.getEstado());
        m.put("cidade", v.getCidade());
        m.put("atrativo", v.getAtrativo());
        m.put("dataInicio", v.getDataInicio());
        m.put("dataFim", v.getDataFim());
        m.put("descricao", v.getDescricao());
        m.put("tipo", v.getTipo());
        m.put("maxVagas", v.getMaxVagas());
        m.put("createdAt", v.getCreatedAt());
        m.put("participantesCount", participanteRepository.countByViagemId(v.getId()));
        userRepository.findById(v.getLiderId()).ifPresent(leader -> m.put("lider", toUserMap(leader)));
        if (viewer != null) {
            participanteRepository.findByViagemIdAndUserId(v.getId(), viewer.getId())
                    .ifPresent(p -> m.put("myStatus", p.getStatus()));
        }
        return m;
    }

    private static Map<String, Object> toUserMap(User u) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", u.getId());
        m.put("openId", u.getOpenId());
        m.put("name", u.getName());
        m.put("foto", u.getAvatarUrl());
        m.put("avatarUrl", u.getAvatarUrl());
        return m;
    }
}
