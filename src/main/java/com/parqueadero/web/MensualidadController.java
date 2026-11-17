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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/mensualidades")
@Tag(name = "Mensualidades", description = "Planes mensuales: las placas con mensualidad vigente no pagan al salir")
public class MensualidadController {

    private final MensualidadService mensualidadService;

    public MensualidadController(MensualidadService mensualidadService) {
        this.mensualidadService = mensualidadService;
    }

    @GetMapping
    @Operation(summary = "Listar mensualidades", description = "Todas las mensualidades registradas, de la mas reciente a la mas antigua")
    @ApiResponse(responseCode = "200", description = "Lista de mensualidades")
    @ApiResponse(responseCode = "401", description = "Falta el token o esta vencido")
    public List<MensualidadResponse> listar() {
        return mensualidadService.listar().stream()
                .map(MensualidadResponse::desde)
                .toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Registrar una mensualidad",
            description = "Asocia una placa y su titular a un rango de fechas. La placa debe tener un formato valido "
                    + "de carro (AAA111), moto (AAA11A) o bicicleta (3 caracteres alfanumericos).")
    @ApiResponse(responseCode = "201", description = "Mensualidad creada")
    @ApiResponse(responseCode = "400", description = "Datos faltantes o placa con formato invalido")
    @ApiResponse(responseCode = "401", description = "Falta el token o esta vencido")
    @ApiResponse(responseCode = "409", description = "La fecha de fin es anterior a la fecha de inicio")
    public MensualidadResponse registrar(@Valid @RequestBody MensualidadRequest request) {
        return MensualidadResponse.desde(mensualidadService.registrar(
                request.placa(), request.titular(), request.fechaInicio(), request.fechaFin()));
    }
}
