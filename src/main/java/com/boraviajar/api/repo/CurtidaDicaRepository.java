package com.boraviajar.api.repo;

import com.boraviajar.api.entity.CurtidaDica;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CurtidaDicaRepository extends JpaRepository<CurtidaDica, Long> {
    long countByDicaId(Long dicaId);

    boolean existsByDicaIdAndUserId(Long dicaId, Long userId);

    void deleteByDicaIdAndUserId(Long dicaId, Long userId);
}
