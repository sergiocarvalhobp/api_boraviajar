package com.boraviajar.api.web;

import com.boraviajar.api.service.PushService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/push")
@RequiredArgsConstructor
public class PushController {

    private final PushService pushService;

    @GetMapping("/vapid-public-key")
    public Map<String, String> vapid() {
        return pushService.vapidPublicKey();
    }

    @PostMapping("/subscribe")
    public Map<String, Boolean> subscribe(@RequestBody SubscribeBody body) {
        return pushService.subscribe(CurrentUser.require(), body.endpoint(), body.p256dh(), body.auth());
    }

    public record SubscribeBody(String endpoint, String p256dh, String auth) {}
}
