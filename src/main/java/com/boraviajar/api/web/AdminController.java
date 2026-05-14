package com.boraviajar.api.web;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin")
public class AdminController {

    @PostMapping("/notify-owner")
    @PreAuthorize("hasRole('ADMIN')")
    public Map<String, Object> notifyOwner(@RequestBody Map<String, String> body) {
        throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED, "Integração notifyOwner (Forge) não portada para Java.");
    }
}
