package com.boraviajar.api.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "openId", nullable = false, unique = true, length = 191)
    private String openId;

    @Column(columnDefinition = "TEXT")
    private String name;

    @Column(length = 320)
    private String email;

    @Column(name = "loginMethod", length = 64)
    private String loginMethod;

    @Column(length = 10)
    private String role = "user";

    @Column(columnDefinition = "TEXT")
    private String bio;

    private Integer idade;

    @Column(name = "avatarUrl", columnDefinition = "TEXT")
    private String avatarUrl;

    @Column(name = "cidadeResidencia", length = 150)
    private String cidadeResidencia;

    @Column(name = "estadoResidencia", length = 2)
    private String estadoResidencia;

    @Column(name = "destinosFavoritos", columnDefinition = "TEXT")
    private String destinosFavoritos;

    @Column(length = 100)
    private String instagram;

    @Column(name = "createdAt", nullable = false)
    private Instant createdAt;

    @Column(name = "updatedAt", nullable = false)
    private Instant updatedAt;

    @Column(name = "lastSignedIn", nullable = false)
    private Instant lastSignedIn;
}
