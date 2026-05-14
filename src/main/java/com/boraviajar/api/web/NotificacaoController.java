package com.boraviajar.api.web;

import com.boraviajar.api.entity.Notificacao;
import com.boraviajar.api.service.NotificacaoService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificacaoController {

    private final NotificacaoService notificacaoService;

    @GetMapping
    public List<Notificacao> listar() {
        return notificacaoService.listar(CurrentUser.require());
    }

    @GetMapping("/unread-count")
    public Map<String, Object> contar() {
        return notificacaoService.contarNaoLidas(CurrentUser.require());
    }

    @PostMapping("/{id}/read")
    public Map<String, Boolean> marcarLida(@PathVariable long id) {
        return notificacaoService.marcarLida(id, CurrentUser.require());
    }

    @PostMapping("/read-all")
    public Map<String, Boolean> marcarTodas() {
        return notificacaoService.marcarTodas(CurrentUser.require());
    }
}
