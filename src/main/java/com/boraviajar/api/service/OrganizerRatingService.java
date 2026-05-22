package com.boraviajar.api.service;

import com.boraviajar.api.entity.OrganizerRating;
import com.boraviajar.api.entity.User;
import com.boraviajar.api.entity.Viagem;
import com.boraviajar.api.repo.OrganizerRatingRepository;
import com.boraviajar.api.repo.ParticipanteRepository;
import com.boraviajar.api.repo.ViagemRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OrganizerRatingService {

    private static final Logger log = LoggerFactory.getLogger(OrganizerRatingService.class);

    private final OrganizerRatingRepository organizerRatingRepository;
    private final ViagemRepository viagemRepository;
    private final ParticipanteRepository participanteRepository;

    /** Viagem encerrada para avaliação: último dia da viagem ou depois. */
    public boolean isTripFinished(Viagem v) {
        return v.getDataFim() != null && !v.getDataFim().isAfter(LocalDate.now());
    }

    private boolean isConfirmedParticipant(long viagemId, long userId) {
        return participanteRepository.findByViagemIdAndUserId(viagemId, userId)
                .map(p -> "confirmado".equalsIgnoreCase(p.getStatus()))
                .orElse(false);
    }

    public Optional<Double> averageForOrganizer(long organizerUserId) {
        try {
            Double avg = organizerRatingRepository.averageStarsByOrganizerUserId(organizerUserId);
            if (avg == null) return Optional.empty();
            return Optional.of(Math.round(avg * 10.0) / 10.0);
        } catch (Exception e) {
            log.warn("Média de avaliação indisponível (tabela organizer_ratings?): {}", e.getMessage());
            return Optional.empty();
        }
    }

    public long countForOrganizer(long organizerUserId) {
        try {
            return organizerRatingRepository.countByOrganizerUserId(organizerUserId);
        } catch (Exception e) {
            log.warn("Contagem de avaliações indisponível: {}", e.getMessage());
            return 0;
        }
    }

    public void enrichLeaderMap(Map<String, Object> leaderMap, long organizerUserId) {
        try {
            averageForOrganizer(organizerUserId).ifPresent(avg -> {
                leaderMap.put("organizerRating", avg);
                leaderMap.put("mediaOrganizador", avg);
            });
            long count = countForOrganizer(organizerUserId);
            if (count > 0) {
                leaderMap.put("organizerRatingCount", count);
                leaderMap.put("totalAvaliacoesOrganizador", count);
            }
        } catch (Exception e) {
            log.warn("enrichLeaderMap ignorado: {}", e.getMessage());
        }
    }

    public void enrichTripMap(Map<String, Object> tripMap, Viagem v, User viewer) {
        try {
            boolean finished = isTripFinished(v);
            tripMap.put("tripFinished", finished);

            if (viewer == null) {
                tripMap.put("canRateOrganizer", false);
                return;
            }

            boolean isLeader = v.getLiderId().equals(viewer.getId());
            boolean canRate = finished && isConfirmedParticipant(v.getId(), viewer.getId()) && !isLeader;
            tripMap.put("canRateOrganizer", canRate);

            if (canRate || isLeader) {
                organizerRatingRepository
                        .findByViagemIdAndRaterUserId(v.getId(), viewer.getId())
                        .ifPresent(r -> {
                            tripMap.put("myOrganizerRating", r.getEstrelas());
                            if (r.getTestemunho() != null && !r.getTestemunho().isBlank()) {
                                tripMap.put("myOrganizerTestimony", r.getTestemunho());
                            }
                        });
            }
        } catch (Exception e) {
            log.warn("enrichTripMap ignorado: {}", e.getMessage());
            tripMap.putIfAbsent("tripFinished", isTripFinished(v));
            tripMap.putIfAbsent("canRateOrganizer", false);
        }
    }

    public Map<String, Object> getState(long tripId, User viewer) {
        Viagem v = viagemRepository.findById(tripId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Viagem não encontrada"));

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("tripId", v.getId());
        out.put("organizerUserId", v.getLiderId());
        out.put("tripFinished", isTripFinished(v));

        averageForOrganizer(v.getLiderId()).ifPresent(avg -> {
            out.put("organizerRating", avg);
            out.put("mediaOrganizador", avg);
        });
        long count = countForOrganizer(v.getLiderId());
        if (count > 0) {
            out.put("organizerRatingCount", count);
            out.put("totalAvaliacoesOrganizador", count);
        }

        boolean isLeader = viewer.getId().equals(v.getLiderId());
        boolean canRate = isTripFinished(v) && isConfirmedParticipant(v.getId(), viewer.getId()) && !isLeader;

        out.put("canRateOrganizer", canRate);
        organizerRatingRepository
                .findByViagemIdAndRaterUserId(v.getId(), viewer.getId())
                .ifPresent(r -> {
                    out.put("myOrganizerRating", r.getEstrelas());
                    if (r.getTestemunho() != null && !r.getTestemunho().isBlank()) {
                        out.put("myOrganizerTestimony", r.getTestemunho());
                    }
                });

        return out;
    }

    @Transactional
    public Map<String, Object> submit(long tripId, User rater, int stars, String testemunho) {
        if (stars < 1 || stars > 5) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A nota deve ser entre 1 e 5");
        }

        Viagem v = viagemRepository.findById(tripId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Viagem não encontrada"));

        if (!isTripFinished(v)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "A avaliação só é permitida após o fim da viagem");
        }

        if (v.getLiderId().equals(rater.getId())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Quem criou a viagem não pode avaliar a própria viagem");
        }

        if (!isConfirmedParticipant(v.getId(), rater.getId())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "A avaliação só é permitida para participantes confirmados da viagem");
        }

        Instant now = Instant.now();
        OrganizerRating rating = organizerRatingRepository
                .findByViagemIdAndRaterUserId(v.getId(), rater.getId())
                .orElseGet(OrganizerRating::new);

        if (rating.getId() == null) {
            rating.setCreatedAt(now);
            rating.setViagemId(v.getId());
            rating.setRaterUserId(rater.getId());
            rating.setOrganizerUserId(v.getLiderId());
        }
        rating.setEstrelas(stars);
        if (testemunho != null) {
            String t = testemunho.trim();
            rating.setTestemunho(t.isEmpty() ? null : t);
        }
        rating.setUpdatedAt(now);
        organizerRatingRepository.save(rating);

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("myOrganizerRating", stars);
        if (rating.getTestemunho() != null) {
            out.put("myOrganizerTestimony", rating.getTestemunho());
        }
        out.put("canRateOrganizer", true);
        averageForOrganizer(v.getLiderId()).ifPresent(avg -> out.put("organizerRating", avg));
        out.put("organizerRatingCount", countForOrganizer(v.getLiderId()));
        return out;
    }
}
