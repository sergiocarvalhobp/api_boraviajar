package com.boraviajar.api.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(
        name = "organizer_ratings",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_organizer_rating_viagem_rater",
                columnNames = {"viagem_id", "rater_user_id"}
        )
)
public class OrganizerRating {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "viagem_id", nullable = false)
    private Long viagemId;

    @Column(name = "rater_user_id", nullable = false)
    private Long raterUserId;

    @Column(name = "organizer_user_id", nullable = false)
    private Long organizerUserId;

    @Column(nullable = false)
    private Integer estrelas;

    @Column(name = "createdAt", nullable = false)
    private Instant createdAt;

    @Column(name = "updatedAt", nullable = false)
    private Instant updatedAt;
}
