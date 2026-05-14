package com.boraviajar.api.service;

import com.boraviajar.api.entity.PushSubscription;
import com.boraviajar.api.entity.User;
import com.boraviajar.api.repo.PushSubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PushService {

    private final PushSubscriptionRepository pushSubscriptionRepository;

    @Value("${boraviajar.vapid.public-key:}")
    private String vapidPublicKey;

    public Map<String, String> vapidPublicKey() {
        return Map.of("key", vapidPublicKey != null ? vapidPublicKey : "");
    }

    @Transactional
    public Map<String, Boolean> subscribe(User user, String endpoint, String p256dh, String auth) {
        List<PushSubscription> existing = pushSubscriptionRepository.findByUserIdOrderByCreatedAtAsc(user.getId());
        boolean dup = existing.stream().anyMatch(s -> endpoint.equals(s.getEndpoint()));
        if (dup) {
            return Map.of("success", true);
        }
        if (existing.size() >= 5) {
            pushSubscriptionRepository.deleteById(existing.get(0).getId());
        }
        PushSubscription s = new PushSubscription();
        s.setUserId(user.getId());
        s.setEndpoint(endpoint);
        s.setP256dh(p256dh);
        s.setAuth(auth);
        s.setCreatedAt(Instant.now());
        pushSubscriptionRepository.save(s);
        return Map.of("success", true);
    }
}
