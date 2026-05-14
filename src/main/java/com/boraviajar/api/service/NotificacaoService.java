package com.boraviajar.api.service;

import com.boraviajar.api.entity.Notificacao;
import com.boraviajar.api.entity.User;
import com.boraviajar.api.repo.NotificacaoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class NotificacaoService {

    private final NotificacaoRepository notificacaoRepository;

    @Transactional(readOnly = true)
    public List<Notificacao> listar(User user) {
        return notificacaoRepository.findByUserIdOrderByCreatedAtDesc(user.getId(), PageRequest.of(0, 30));
    }

    @Transactional(readOnly = true)
    public Map<String, Object> contarNaoLidas(User user) {
        long count = notificacaoRepository.countByUserIdAndLida(user.getId(), 0);
        return Map.of("count", count);
    }

    @Transactional
    public Map<String, Boolean> marcarLida(long id, User user) {
        notificacaoRepository.marcarLida(id, user.getId());
        return Map.of("success", true);
    }

    @Transactional
    public Map<String, Boolean> marcarTodas(User user) {
        notificacaoRepository.marcarTodasLidas(user.getId());
        return Map.of("success", true);
    }
}
