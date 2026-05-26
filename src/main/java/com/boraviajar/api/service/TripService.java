package com.boraviajar.api.service;

import com.boraviajar.api.entity.*;
import com.boraviajar.api.repo.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
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
    private final OrganizerRatingService organizerRatingService;
    @PersistenceContext
    private EntityManager entityManager;

    public List<Viagem> listAll() {
        return viagemRepository.findAllByOrderByCreatedAtDesc();
    }

    public List<Map<String, Object>> listAllEnriched(User viewer) {
        return listAll().stream().map(v -> toTripMap(v, viewer)).toList();
    }

    public Map<String, Object> listPageEnriched(User viewer, int offset, int limit, boolean apenasAtivas) {
        int safeLimit = Math.min(50, Math.max(1, limit));
        int safeOffset = Math.max(0, offset);
        int page = safeOffset / safeLimit;
        List<Viagem> rows;
        long total;
        if (apenasAtivas) {
            LocalDate hoje = LocalDate.now();
            rows = viagemRepository.findAllByDataFimGreaterThanEqualOrderByCreatedAtDesc(
                    hoje, PageRequest.of(page, safeLimit));
            total = viagemRepository.countByDataFimGreaterThanEqual(hoje);
        } else {
            rows = viagemRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(page, safeLimit));
            total = viagemRepository.count();
        }
        return toPagedResponse(rows, viewer, safeOffset, safeLimit, total);
    }

    public Map<String, Object> listByFilterEnriched(
            User viewer,
            String destino, String estado, String cidade, String atrativo,
            LocalDate dataInicio, LocalDate dataFim) {
        return listByFilterPageEnriched(
                viewer, destino, estado, cidade, atrativo, dataInicio, dataFim, 0, Integer.MAX_VALUE, false);
    }

    public Map<String, Object> listByFilterPageEnriched(
            User viewer,
            String destino, String estado, String cidade, String atrativo,
            LocalDate dataInicio, LocalDate dataFim,
            int offset, int limit,
            boolean apenasAtivas) {
        int safeLimit = Math.min(50, Math.max(1, limit));
        int safeOffset = Math.max(0, offset);
        FilterQuery fq = buildFilterQuery(
                destino, estado, cidade, atrativo, dataInicio, dataFim, apenasAtivas);
        List<Viagem> rows = fq.list(safeOffset, safeLimit);
        long total = fq.count();
        return toPagedResponse(rows, viewer, safeOffset, safeLimit, total);
    }

    private Map<String, Object> toPagedResponse(
            List<Viagem> rows, User viewer, int offset, int limit, long total) {
        List<Map<String, Object>> items = rows.stream().map(v -> toTripMap(v, viewer)).toList();
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("items", items);
        out.put("offset", offset);
        out.put("limit", limit);
        out.put("total", total);
        out.put("hasMore", offset + rows.size() < total);
        return out;
    }

    public Optional<Map<String, Object>> findEnrichedById(long id, User viewer) {
        return findById(id).map(v -> toTripMap(v, viewer));
    }

    /** Mapa enriquecido para listagens (ex.: histórico do usuário). */
    public Map<String, Object> toTripMapForViewer(Viagem v, User viewer) {
        return toTripMap(v, viewer);
    }

    /** Equivalente a getViagensByFilter no Node. */
    public List<Viagem> listByFilter(String destino, String estado, String cidade, String atrativo,
                                     LocalDate dataInicio, LocalDate dataFim) {
        return buildFilterQuery(destino, estado, cidade, atrativo, dataInicio, dataFim, false)
                .list(0, Integer.MAX_VALUE);
    }

    private FilterQuery buildFilterQuery(
            String destino, String estado, String cidade, String atrativo,
            LocalDate dataInicio, LocalDate dataFim,
            boolean apenasAtivas) {
        StringBuilder where = new StringBuilder(" where 1=1");
        Map<String, Object> params = new HashMap<>();
        if (apenasAtivas) {
            where.append(" and v.dataFim >= :hoje");
            params.put("hoje", LocalDate.now());
        }
        if (estado != null && !estado.isBlank()) {
            where.append(" and v.estado = :estado");
            params.put("estado", estado);
        }
        if (cidade != null && !cidade.isBlank()) {
            where.append(" and lower(v.cidade) like lower(concat('%', :cidade, '%'))");
            params.put("cidade", cidade);
        }
        if (atrativo != null && !atrativo.isBlank()) {
            where.append(" and lower(v.atrativo) like lower(concat('%', :atrativo, '%'))");
            params.put("atrativo", atrativo);
        }
        if (destino != null && !destino.isBlank()
                && (estado == null || estado.isBlank())
                && (cidade == null || cidade.isBlank())) {
            where.append(" and lower(v.destino) like lower(concat('%', :destino, '%'))");
            params.put("destino", destino);
        }
        if (dataInicio != null) {
            where.append(" and v.dataInicio >= :dataInicio");
            params.put("dataInicio", dataInicio);
        }
        if (dataFim != null) {
            where.append(" and v.dataFim <= :dataFim");
            params.put("dataFim", dataFim);
        }
        return new FilterQuery(where.toString(), params);
    }

    private final class FilterQuery {
        private final String whereClause;
        private final Map<String, Object> params;

        private FilterQuery(String whereClause, Map<String, Object> params) {
            this.whereClause = whereClause;
            this.params = params;
        }

        List<Viagem> list(int offset, int limit) {
            TypedQuery<Viagem> q = entityManager.createQuery(
                    "select v from Viagem v" + whereClause + " order by v.dataInicio", Viagem.class);
            params.forEach(q::setParameter);
            q.setFirstResult(offset);
            q.setMaxResults(limit);
            return q.getResultList();
        }

        long count() {
            TypedQuery<Long> q = entityManager.createQuery(
                    "select count(v) from Viagem v" + whereClause, Long.class);
            params.forEach(q::setParameter);
            return q.getSingleResult();
        }
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
            userRepository.findById(p.getUserId())
                    .ifPresent(u -> row.put("user", toUserMap(u)));
            withUsers.add(row);
        }
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("viagem", v);
        out.put("leader", leader);
        out.put("participants", withUsers);
        out.put("confirmadosCount",
                participanteRepository.countByViagemIdAndStatus(viagemId, "confirmado"));
        out.put("participantCount",
                participanteRepository.countByViagemIdAndStatus(viagemId, "confirmado"));
        out.put("totalParticipantes", participanteRepository.countByViagemId(viagemId));
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
        m.put("confirmadosCount",
                participanteRepository.countByViagemIdAndStatus(v.getId(), "confirmado"));
        m.put("participantesCount",
                participanteRepository.countByViagemIdAndStatus(v.getId(), "confirmado"));
        m.put("totalParticipantes", participanteRepository.countByViagemId(v.getId()));
        userRepository.findById(v.getLiderId()).ifPresent(leader -> {
            Map<String, Object> leaderMap = toUserMap(leader);
            organizerRatingService.enrichLeaderMap(leaderMap, leader.getId());
            m.put("lider", leaderMap);
        });
        if (viewer != null) {
            participanteRepository.findByViagemIdAndUserId(v.getId(), viewer.getId())
                    .ifPresent(p -> m.put("myStatus", p.getStatus()));
        }
        organizerRatingService.enrichTripMap(m, v, viewer);
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
