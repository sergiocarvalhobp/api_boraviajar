package com.boraviajar.api.repo;

import com.boraviajar.api.entity.ComentarioDica;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ComentarioDicaRepository extends JpaRepository<ComentarioDica, Long> {
    List<ComentarioDica> findByDicaIdOrderByCreatedAtAsc(Long dicaId);

    long countByDicaId(Long dicaId);
}
