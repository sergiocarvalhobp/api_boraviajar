package com.boraviajar.api.repo;

import com.boraviajar.api.entity.Notificacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NotificacaoRepository extends JpaRepository<Notificacao, Long> {

    List<Notificacao> findByUserIdOrderByCreatedAtDesc(Long userId, org.springframework.data.domain.Pageable pageable);

    long countByUserIdAndLida(Long userId, int lida);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Notificacao n SET n.lida = 1 WHERE n.id = :id AND n.userId = :userId")
    int marcarLida(@Param("id") Long id, @Param("userId") Long userId);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Notificacao n SET n.lida = 1 WHERE n.userId = :userId")
    int marcarTodasLidas(@Param("userId") Long userId);
}
