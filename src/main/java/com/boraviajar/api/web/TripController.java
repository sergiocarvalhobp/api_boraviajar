package com.boraviajar.api.web;

import com.boraviajar.api.entity.User;
import com.boraviajar.api.entity.Viagem;
import com.boraviajar.api.service.TripService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/trips")
@RequiredArgsConstructor
public class TripController {

    private final TripService tripService;

    @GetMapping
    public List<Viagem> list() {
        return tripService.listAll();
    }

    @GetMapping("/filter")
    public List<Viagem> filter(
            @RequestParam(required = false) String destino,
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) String cidade,
            @RequestParam(required = false) String atrativo,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim
    ) {
        return tripService.listByFilter(destino, estado, cidade, atrativo, dataInicio, dataFim);
    }

    @GetMapping("/{id}")
    public Viagem getById(@PathVariable long id) {
        return tripService.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    @GetMapping("/{id}/details")
    public Map<String, Object> details(@PathVariable long id) {
        return tripService.detailsWithParticipants(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Viagem create(@RequestBody CreateTripBody body) {
        User u = CurrentUser.require();
        if (body.destino() == null || body.destino().isBlank()
                || body.descricao() == null || body.descricao().isBlank()
                || body.tipo() == null || body.tipo().isBlank()
                || body.dataInicio() == null || body.dataFim() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Campos obrigatórios ausentes");
        }
        return tripService.create(u, new TripService.CreateTripRequest(
                body.destino(), body.estado(), body.cidade(), body.atrativo(),
                body.dataInicio(), body.dataFim(), body.descricao(), body.tipo(), body.maxVagas()));
    }

    public record CreateTripBody(
            String destino,
            String estado,
            String cidade,
            String atrativo,
            LocalDate dataInicio,
            LocalDate dataFim,
            String descricao,
            String tipo,
            Integer maxVagas
    ) {}
}
