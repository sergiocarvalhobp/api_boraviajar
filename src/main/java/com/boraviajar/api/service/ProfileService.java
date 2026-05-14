package com.boraviajar.api.service;

import com.boraviajar.api.entity.User;
import com.boraviajar.api.entity.Viagem;
import com.boraviajar.api.repo.ParticipanteRepository;
import com.boraviajar.api.repo.UserRepository;
import com.boraviajar.api.repo.ViagemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final UserRepository userRepository;
    private final ViagemRepository viagemRepository;
    private final ParticipanteRepository participanteRepository;

    public Map<String, Object> getPublic(long userId) {
        User u = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado"));
        Map<String, Object> pub = new LinkedHashMap<>();
        pub.put("id", u.getId());
        pub.put("name", u.getName());
        pub.put("bio", u.getBio());
        pub.put("idade", u.getIdade());
        pub.put("avatarUrl", u.getAvatarUrl());
        pub.put("cidadeResidencia", u.getCidadeResidencia());
        pub.put("estadoResidencia", u.getEstadoResidencia());
        pub.put("destinosFavoritos", u.getDestinosFavoritos());
        pub.put("instagram", u.getInstagram());
        pub.put("createdAt", u.getCreatedAt());

        List<Viagem> criadas = viagemRepository.findAllByLiderIdOrderByCreatedAtDesc(userId);
        Set<Long> pids = participanteRepository.findByUserId(userId).stream()
                .map(p -> p.getViagemId())
                .collect(Collectors.toSet());
        List<Viagem> todas = viagemRepository.findAllByOrderByCreatedAtDesc();
        List<Viagem> participando = todas.stream()
                .filter(v -> pids.contains(v.getId()) && !v.getLiderId().equals(userId))
                .toList();
        pub.put("totalViagens", criadas.size() + participando.size());
        pub.put("viagens", criadas);
        pub.put("participando", participando);
        return pub;
    }

    @Transactional
    public User atualizar(User me, ProfilePatch patch) {
        if (patch.bio() != null) me.setBio(patch.bio());
        if (patch.idade() != null) me.setIdade(patch.idade());
        if (patch.cidadeResidencia() != null) me.setCidadeResidencia(patch.cidadeResidencia());
        if (patch.estadoResidencia() != null) me.setEstadoResidencia(patch.estadoResidencia());
        if (patch.destinosFavoritosJson() != null) me.setDestinosFavoritos(patch.destinosFavoritosJson());
        if (patch.instagram() != null) me.setInstagram(patch.instagram());
        me.setUpdatedAt(Instant.now());
        return userRepository.save(me);
    }

    public record ProfilePatch(
            String bio,
            Integer idade,
            String cidadeResidencia,
            String estadoResidencia,
            String destinosFavoritosJson,
            String instagram
    ) {}
}
