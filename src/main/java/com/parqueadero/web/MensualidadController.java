package com.parqueadero.web;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.parqueadero.service.MensualidadService;
import com.parqueadero.web.dto.MensualidadRequest;
import com.parqueadero.web.dto.MensualidadResponse;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/mensualidades")
public class MensualidadController {

    private final MensualidadService mensualidadService;

    public MensualidadController(MensualidadService mensualidadService) {
        this.mensualidadService = mensualidadService;
    }

    @GetMapping
    public List<MensualidadResponse> listar() {
        return mensualidadService.listar().stream()
                .map(MensualidadResponse::desde)
                .toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MensualidadResponse registrar(@Valid @RequestBody MensualidadRequest request) {
        return MensualidadResponse.desde(mensualidadService.registrar(
                request.placa(), request.titular(), request.fechaInicio(), request.fechaFin()));
    }
}
