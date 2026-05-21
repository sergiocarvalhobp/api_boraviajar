package com.boraviajar.api.repo;

import com.boraviajar.api.entity.Viagem;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface ViagemRepository extends JpaRepository<Viagem, Long> {

    List<Viagem> findAllByOrderByCreatedAtDesc();

    List<Viagem> findAllByOrderByCreatedAtDesc(Pageable pageable);

    List<Viagem> findAllByDataFimGreaterThanEqualOrderByCreatedAtDesc(LocalDate dataFim, Pageable pageable);

    long countByDataFimGreaterThanEqual(LocalDate dataFim);

    List<Viagem> findAllByLiderIdOrderByCreatedAtDesc(Long liderId);
}
