package com.boraviajar.api.web;

import com.boraviajar.api.service.HistoricoService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/historico")
@RequiredArgsConstructor
public class HistoricoController {

    private final HistoricoService historicoService;

    @GetMapping("/me")
    public Map<String, Object> meuHistorico() {
        return historicoService.meuHistorico(CurrentUser.require());
    }
}
