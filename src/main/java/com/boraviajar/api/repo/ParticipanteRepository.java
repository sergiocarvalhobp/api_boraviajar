package com.boraviajar.api.repo;

import com.boraviajar.api.entity.Participante;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ParticipanteRepository extends JpaRepository<Participante, Long> {
    List<Participante> findByViagemId(Long viagemId);

    List<Participante> findByUserId(Long userId);

    Optional<Participante> findByViagemIdAndUserId(Long viagemId, Long userId);

    long countByViagemId(Long viagemId);

    long countByViagemIdAndStatus(Long viagemId, String status);

    void deleteByViagemIdAndUserId(Long viagemId, Long userId);
}
