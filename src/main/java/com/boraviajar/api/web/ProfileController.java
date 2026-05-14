package com.boraviajar.api.web;

import com.boraviajar.api.entity.User;
import com.boraviajar.api.service.ProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    @GetMapping("/public/{userId}")
    public Map<String, Object> getPublic(@PathVariable long userId) {
        return profileService.getPublic(userId);
    }

    @GetMapping("/me")
    public User meu() {
        return CurrentUser.require();
    }

    @PatchMapping("/me")
    public User atualizar(@RequestBody ProfileService.ProfilePatch patch) {
        return profileService.atualizar(CurrentUser.require(), patch);
    }

    @PostMapping("/avatar")
    public ResponseEntity<Map<String, String>> uploadAvatar() {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED)
                .body(Map.of("error", "Upload de avatar use o backend Node + storage S3 por enquanto."));
    }
}
