package com.boraviajar.api.repo;

import com.boraviajar.api.entity.PushSubscription;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PushSubscriptionRepository extends JpaRepository<PushSubscription, Long> {

    List<PushSubscription> findByUserIdOrderByCreatedAtAsc(Long userId);
}
