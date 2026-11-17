package com.parqueadero.web;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.parqueadero.service.ReporteService;
import com.parqueadero.web.dto.CierreResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1/reportes")
@Tag(name = "Reportes", description = "Cierre de caja e ingresos del parqueadero")
public class ReporteController {

    private final ReporteService reporteService;

    public ReporteController(ReporteService reporteService) {
        this.reporteService = reporteService;
    }

    @GetMapping("/cierre")
    @Operation(summary = "Cierre de caja de un dia",
            description = "Total recaudado y numero de salidas del dia indicado, desglosado por tipo de vehiculo")
    @ApiResponse(responseCode = "200", description = "Resumen del cierre")
    @ApiResponse(responseCode = "400", description = "Falta la fecha o no tiene el formato AAAA-MM-DD")
    @ApiResponse(responseCode = "401", description = "Falta el token o esta vencido")
    public CierreResponse cierre(
            @Parameter(description = "Dia a consultar, formato AAAA-MM-DD", example = "2026-11-16")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        return reporteService.cierreDelDia(fecha);
    }
}
