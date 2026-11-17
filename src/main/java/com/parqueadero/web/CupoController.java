package com.parqueadero.web;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.parqueadero.service.CupoService;
import com.parqueadero.web.dto.DisponibilidadItem;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1/cupos")
@Tag(name = "Cupos", description = "Ocupacion de los cupos del parqueadero por tipo de vehiculo")
public class CupoController {

    private final CupoService cupoService;

    public CupoController(CupoService cupoService) {
        this.cupoService = cupoService;
    }

    @GetMapping("/disponibilidad")
    @Operation(summary = "Consultar disponibilidad",
            description = "Devuelve, para cada tipo de vehiculo, el total de cupos y cuantos estan libres")
    @ApiResponse(responseCode = "200", description = "Disponibilidad actual")
    @ApiResponse(responseCode = "401", description = "Falta el token o esta vencido")
    public List<DisponibilidadItem> disponibilidad() {
        return cupoService.disponibilidad();
    }
}
