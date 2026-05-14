package com.boraviajar.api.repo;

import com.boraviajar.api.entity.Dica;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DicaRepository extends JpaRepository<Dica, Long> {

    List<Dica> findByEstadoAndCategoriaOrderByCreatedAtDesc(String estado, String categoria, Pageable pageable);

    List<Dica> findByEstadoOrderByCreatedAtDesc(String estado, Pageable pageable);

    List<Dica> findByCategoriaOrderByCreatedAtDesc(String categoria, Pageable pageable);

    List<Dica> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    List<Dica> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
