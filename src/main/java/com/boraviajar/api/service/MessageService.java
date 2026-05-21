package com.boraviajar.api.service;

import com.boraviajar.api.entity.Mensagem;
import com.boraviajar.api.entity.Participante;
import com.boraviajar.api.entity.User;
import com.boraviajar.api.entity.Viagem;
import com.boraviajar.api.repo.MensagemRepository;
import com.boraviajar.api.repo.ParticipanteRepository;
import com.boraviajar.api.repo.UserRepository;
import com.boraviajar.api.repo.ViagemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class MessageService {

    private final MensagemRepository mensagemRepository;
    private final UserRepository userRepository;
    private final ViagemRepository viagemRepository;
    private final ParticipanteRepository participanteRepository;

    private void assertCanAccessChat(Viagem v, User user) {
        if (v.getLiderId().equals(user.getId())) {
            return;
        }
        Participante p = participanteRepository
                .findByViagemIdAndUserId(v.getId(), user.getId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.FORBIDDEN,
                        "O chat será liberado após o organizador confirmar sua participação"));
        if (!"confirmado".equalsIgnoreCase(p.getStatus())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "O chat será liberado após o organizador confirmar sua participação");
        }
    }

    /** Viagem encerrada: último dia da viagem ou depois — chat só leitura. */
    private static boolean isTripFinished(Viagem v) {
        return v.getDataFim() != null && !v.getDataFim().isAfter(LocalDate.now());
    }

    public List<Map<String, Object>> listByViagem(long viagemId, User viewer) {
        Viagem v = viagemRepository.findById(viagemId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Viagem não encontrada"));
        if (viewer != null) {
            assertCanAccessChat(v, viewer);
        }
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
        Viagem v = viagemRepository.findById(viagemId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Viagem não encontrada"));
        if (isTripFinished(v)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "O chat está encerrado. Esta viagem já terminou.");
        }
        assertCanAccessChat(v, sender);

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
