package com.parqueadero.web;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.parqueadero.model.EstadoTicket;
import com.parqueadero.service.ParqueaderoService;
import com.parqueadero.web.dto.EntradaRequest;
import com.parqueadero.web.dto.SalidaRequest;
import com.parqueadero.web.dto.TicketResponse;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/tickets")
public class TicketController {

    private final ParqueaderoService parqueaderoService;

    public TicketController(ParqueaderoService parqueaderoService) {
        this.parqueaderoService = parqueaderoService;
    }

    @PostMapping("/entrada")
    @ResponseStatus(HttpStatus.CREATED)
    public TicketResponse registrarEntrada(@Valid @RequestBody EntradaRequest request) {
        return TicketResponse.desde(
                parqueaderoService.registrarEntrada(request.placa(), request.tipoVehiculo(), request.cedulaPropietario()));
    }

    @PostMapping("/salida")
    public TicketResponse registrarSalida(@Valid @RequestBody SalidaRequest request) {
        return TicketResponse.desde(parqueaderoService.registrarSalida(request.placa()));
    }

    @GetMapping
    public List<TicketResponse> listar(@RequestParam(defaultValue = "ACTIVO") EstadoTicket estado) {
        return parqueaderoService.listarPorEstado(estado).stream()
                .map(TicketResponse::desde)
                .toList();
    }

    @GetMapping("/historial/{placa}")
    public List<TicketResponse> historial(@PathVariable String placa) {
        return parqueaderoService.historial(placa).stream()
                .map(TicketResponse::desde)
                .toList();
    }
}
