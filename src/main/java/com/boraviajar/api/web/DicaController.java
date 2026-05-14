package com.boraviajar.api.web;

import com.boraviajar.api.entity.ComentarioDica;
import com.boraviajar.api.service.DicaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/dicas")
@RequiredArgsConstructor
public class DicaController {

    private final DicaService dicaService;

    @GetMapping
    public List<Map<String, Object>> listar(
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) String categoria,
            @RequestParam(required = false) Long userId,
            @RequestParam(defaultValue = "50") int limit,
            @RequestParam(defaultValue = "0") int offset
    ) {
        return dicaService.listar(estado, categoria, userId, limit, offset);
    }

    @GetMapping("/{id}")
    public Map<String, Object> detalhe(@PathVariable long id) {
        return dicaService.detalhe(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Boolean> criar(@RequestBody CriarDicaBody body) {
        dicaService.criar(
                CurrentUser.require(),
                body.titulo(), body.conteudo(), body.estado(), body.cidade(), body.atrativo(), body.categoria());
        return Map.of("success", true);
    }

    @PostMapping("/{id}/curtir")
    public Map<String, String> curtir(@PathVariable long id) {
        return dicaService.toggleCurtida(id, CurrentUser.require());
    }

    @GetMapping("/{id}/curtida")
    public Map<String, Boolean> isCurtida(@PathVariable long id, @RequestParam long userId) {
        return Map.of("curtida", dicaService.isCurtida(id, userId));
    }

    @PostMapping("/{id}/comentarios")
    public Map<String, Boolean> comentar(@PathVariable long id, @RequestBody ComentarioBody body) {
        dicaService.comentar(id, CurrentUser.require(), body.conteudo());
        return Map.of("success", true);
    }

    @GetMapping("/{id}/comentarios")
    public List<ComentarioDica> listarComentarios(@PathVariable long id) {
        return dicaService.listarComentarios(id);
    }

    public record CriarDicaBody(String titulo, String conteudo, String estado, String cidade, String atrativo, String categoria) {}

    public record ComentarioBody(String conteudo) {}
}
