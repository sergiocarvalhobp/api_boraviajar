package com.boraviajar.api.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "curtidas_dicas")
public class CurtidaDica {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "dica_id", nullable = false)
    private Long dicaId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "createdAt", nullable = false)
    private Instant createdAt;
}
