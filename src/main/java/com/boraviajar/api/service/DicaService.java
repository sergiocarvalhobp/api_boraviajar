package com.boraviajar.api.service;

import com.boraviajar.api.entity.*;
import com.boraviajar.api.repo.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.*;

@Service
@RequiredArgsConstructor
public class DicaService {

    private final DicaRepository dicaRepository;
    private final UserRepository userRepository;
    private final CurtidaDicaRepository curtidaDicaRepository;
    private final ComentarioDicaRepository comentarioDicaRepository;

    public List<Map<String, Object>> listar(String estado, String categoria, Long userId, int limit, int offset) {
        int page = Math.max(0, offset / Math.max(1, limit));
        PageRequest p = PageRequest.of(page, Math.min(100, Math.max(1, limit)));
        List<Dica> rows;
        if (userId != null) {
            rows = dicaRepository.findByUserIdOrderByCreatedAtDesc(userId, p);
        } else if (estado != null && !estado.isBlank() && categoria != null && !categoria.isBlank()) {
            rows = dicaRepository.findByEstadoAndCategoriaOrderByCreatedAtDesc(estado, categoria, p);
        } else if (estado != null && !estado.isBlank()) {
            rows = dicaRepository.findByEstadoOrderByCreatedAtDesc(estado, p);
        } else if (categoria != null && !categoria.isBlank()) {
            rows = dicaRepository.findByCategoriaOrderByCreatedAtDesc(categoria, p);
        } else {
            rows = dicaRepository.findAllByOrderByCreatedAtDesc(p);
        }
        return rows.stream().map(this::toListRow).toList();
    }

    private Map<String, Object> toListRow(Dica d) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("dica", d);
        userRepository.findById(d.getUserId()).ifPresent(u -> {
            Map<String, Object> autor = new LinkedHashMap<>();
            autor.put("id", u.getId());
            autor.put("name", u.getName());
            autor.put("avatarUrl", u.getAvatarUrl());
            autor.put("cidadeResidencia", u.getCidadeResidencia());
            autor.put("estadoResidencia", u.getEstadoResidencia());
            m.put("autor", autor);
        });
        m.put("totalCurtidas", curtidaDicaRepository.countByDicaId(d.getId()));
        m.put("totalComentarios", comentarioDicaRepository.countByDicaId(d.getId()));
        return m;
    }

    public Map<String, Object> detalhe(long id) {
        Dica d = dicaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Dica não encontrada"));
        return toListRow(d);
    }

    public boolean isCurtida(long dicaId, long userId) {
        return curtidaDicaRepository.existsByDicaIdAndUserId(dicaId, userId);
    }

    @Transactional
    public Map<String, String> toggleCurtida(long dicaId, User user) {
        if (curtidaDicaRepository.existsByDicaIdAndUserId(dicaId, user.getId())) {
            curtidaDicaRepository.deleteByDicaIdAndUserId(dicaId, user.getId());
            return Map.of("acao", "descurtido");
        }
        CurtidaDica c = new CurtidaDica();
        c.setDicaId(dicaId);
        c.setUserId(user.getId());
        c.setCreatedAt(Instant.now());
        curtidaDicaRepository.save(c);
        return Map.of("acao", "curtido");
    }

    @Transactional
    public void comentar(long dicaId, User user, String conteudo) {
        ComentarioDica c = new ComentarioDica();
        c.setDicaId(dicaId);
        c.setUserId(user.getId());
        c.setConteudo(conteudo);
        c.setCreatedAt(Instant.now());
        comentarioDicaRepository.save(c);
    }

    public List<ComentarioDica> listarComentarios(long dicaId) {
        return comentarioDicaRepository.findByDicaIdOrderByCreatedAtAsc(dicaId);
    }

    @Transactional
    public void criar(User user, String titulo, String conteudo, String estado, String cidade, String atrativo, String categoria) {
        Dica d = new Dica();
        d.setUserId(user.getId());
        d.setTitulo(titulo);
        d.setConteudo(conteudo);
        d.setEstado(estado);
        d.setCidade(cidade);
        d.setAtrativo(atrativo);
        d.setCategoria(categoria != null ? categoria : "Geral");
        Instant now = Instant.now();
        d.setCreatedAt(now);
        d.setUpdatedAt(now);
        dicaRepository.save(d);
    }
}
