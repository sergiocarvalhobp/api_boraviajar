package com.boraviajar.api.repo;

import com.boraviajar.api.entity.Mensagem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MensagemRepository extends JpaRepository<Mensagem, Long> {
    List<Mensagem> findByViagemIdOrderByTimestampAsc(Long viagemId);
}
