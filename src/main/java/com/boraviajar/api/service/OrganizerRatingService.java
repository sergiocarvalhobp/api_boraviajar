package com.boraviajar.api.service;

import com.boraviajar.api.entity.OrganizerRating;
import com.boraviajar.api.entity.User;
import com.boraviajar.api.entity.Viagem;
import com.boraviajar.api.repo.OrganizerRatingRepository;
import com.boraviajar.api.repo.ParticipanteRepository;
import com.boraviajar.api.repo.ViagemRepository;
import lombok.RequiredArgsConstructor;
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

    private final OrganizerRatingRepository organizerRatingRepository;
    private final ViagemRepository viagemRepository;
    private final ParticipanteRepository participanteRepository;

    public boolean isTripFinished(Viagem v) {
        return v.getDataFim() != null && v.getDataFim().isBefore(LocalDate.now());
    }

    public Optional<Double> averageForOrganizer(long organizerUserId) {
        Double avg = organizerRatingRepository.averageStarsByOrganizerUserId(organizerUserId);
        if (avg == null) return Optional.empty();
        return Optional.of(Math.round(avg * 10.0) / 10.0);
    }

    public long countForOrganizer(long organizerUserId) {
        return organizerRatingRepository.countByOrganizerUserId(organizerUserId);
    }

    public void enrichLeaderMap(Map<String, Object> leaderMap, long organizerUserId) {
        averageForOrganizer(organizerUserId).ifPresent(avg ->
                leaderMap.put("organizerRating", avg));
        long count = countForOrganizer(organizerUserId);
        if (count > 0) {
            leaderMap.put("organizerRatingCount", count);
        }
    }

    public void enrichTripMap(Map<String, Object> tripMap, Viagem v, User viewer) {
        boolean finished = isTripFinished(v);
        tripMap.put("tripFinished", finished);

        if (viewer == null) {
            tripMap.put("canRateOrganizer", false);
            return;
        }

        boolean isLeader = v.getLiderId().equals(viewer.getId());
        boolean isParticipant = participanteRepository
                .findByViagemIdAndUserId(v.getId(), viewer.getId())
                .isPresent();

        boolean canRate = finished && isParticipant && !isLeader;
        tripMap.put("canRateOrganizer", canRate);

        if (canRate || isLeader) {
            organizerRatingRepository
                    .findByViagemIdAndRaterUserId(v.getId(), viewer.getId())
                    .ifPresent(r -> tripMap.put("myOrganizerRating", r.getEstrelas()));
        }
    }

    public Map<String, Object> getState(long tripId, User viewer) {
        Viagem v = viagemRepository.findById(tripId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Viagem não encontrada"));

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("tripId", v.getId());
        out.put("organizerUserId", v.getLiderId());
        out.put("tripFinished", isTripFinished(v));

        averageForOrganizer(v.getLiderId()).ifPresent(avg -> out.put("organizerRating", avg));
        long count = countForOrganizer(v.getLiderId());
        if (count > 0) out.put("organizerRatingCount", count);

        boolean isLeader = viewer.getId().equals(v.getLiderId());
        boolean isParticipant = participanteRepository
                .findByViagemIdAndUserId(v.getId(), viewer.getId())
                .isPresent();
        boolean canRate = isTripFinished(v) && isParticipant && !isLeader;

        out.put("canRateOrganizer", canRate);
        organizerRatingRepository
                .findByViagemIdAndRaterUserId(v.getId(), viewer.getId())
                .ifPresent(r -> out.put("myOrganizerRating", r.getEstrelas()));

        return out;
    }

    @Transactional
    public Map<String, Object> submit(long tripId, User rater, int stars) {
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
                    "O organizador não pode avaliar a própria viagem");
        }

        if (participanteRepository.findByViagemIdAndUserId(v.getId(), rater.getId()).isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Apenas participantes podem avaliar o organizador");
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
        rating.setUpdatedAt(now);
        organizerRatingRepository.save(rating);

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("myOrganizerRating", stars);
        out.put("canRateOrganizer", true);
        averageForOrganizer(v.getLiderId()).ifPresent(avg -> out.put("organizerRating", avg));
        out.put("organizerRatingCount", countForOrganizer(v.getLiderId()));
        return out;
    }
}
