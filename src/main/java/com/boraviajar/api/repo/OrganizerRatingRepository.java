package com.boraviajar.api.repo;

import com.boraviajar.api.entity.OrganizerRating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface OrganizerRatingRepository extends JpaRepository<OrganizerRating, Long> {

    Optional<OrganizerRating> findByViagemIdAndRaterUserId(Long viagemId, Long raterUserId);

    @Query("SELECT AVG(r.estrelas) FROM OrganizerRating r WHERE r.organizerUserId = :organizerId")
    Double averageStarsByOrganizerUserId(@Param("organizerId") Long organizerId);

    @Query("SELECT COUNT(r) FROM OrganizerRating r WHERE r.organizerUserId = :organizerId")
    long countByOrganizerUserId(@Param("organizerId") Long organizerId);
}
