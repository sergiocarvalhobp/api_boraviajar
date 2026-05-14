package com.boraviajar.api.service;

import com.boraviajar.api.entity.Mensagem;
import com.boraviajar.api.entity.User;
import com.boraviajar.api.repo.MensagemRepository;
import com.boraviajar.api.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class MessageService {

    private final MensagemRepository mensagemRepository;
    private final UserRepository userRepository;

    public List<Map<String, Object>> listByViagem(long viagemId) {
        return mensagemRepository.findByViagemIdOrderByTimestampAsc(viagemId).stream().map(m -> {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("id", m.getId());
            row.put("viagemId", m.getViagemId());
            row.put("senderId", m.getSenderId());
            row.put("conteudo", m.getConteudo());
            row.put("timestamp", m.getTimestamp());
            User sender = userRepository.findById(m.getSenderId()).orElse(null);
            String senderName = "Usuário";
            String senderAvatar = null;
            if (sender != null) {
                if (sender.getName() != null && !sender.getName().isBlank()) {
                    senderName = sender.getName();
                } else if (sender.getEmail() != null) {
                    senderName = sender.getEmail();
                }
                senderAvatar = sender.getAvatarUrl();
            }
            row.put("senderName", senderName);
            row.put("senderAvatar", senderAvatar);
            return row;
        }).toList();
    }

    @Transactional
    public Map<String, Object> send(long viagemId, String conteudo, User sender) {
        Mensagem m = new Mensagem();
        m.setViagemId(viagemId);
        m.setSenderId(sender.getId());
        m.setConteudo(conteudo);
        m.setTimestamp(Instant.now());
        m = mensagemRepository.save(m);
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("id", m.getId());
        out.put("viagemId", m.getViagemId());
        out.put("senderId", m.getSenderId());
        out.put("conteudo", m.getConteudo());
        out.put("timestamp", m.getTimestamp());
        return out;
    }
}
