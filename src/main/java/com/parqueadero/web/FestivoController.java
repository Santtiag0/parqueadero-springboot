package com.parqueadero.web;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.parqueadero.service.FestivoService;
import com.parqueadero.web.dto.FestivoHoyResponse;
import com.parqueadero.web.dto.FestivoResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1/festivos")
@Tag(name = "Festivos", description = "Dias festivos de Colombia, consultados en vivo en api-colombia.com "
        + "y usados para recargar la tarifa en la salida")
public class FestivoController {

    private final FestivoService festivos;

    public FestivoController(FestivoService festivos) {
        this.festivos = festivos;
    }

    @GetMapping("/hoy")
    @Operation(summary = "Saber si hoy es festivo", description = "Util para el frontend: avisa si hoy aplica el recargo de festivo")
    @ApiResponse(responseCode = "200", description = "Fecha de hoy, si es festivo y su nombre")
    @ApiResponse(responseCode = "401", description = "Falta el token o esta vencido")
    public FestivoHoyResponse hoy() {
        LocalDate hoy = LocalDate.now();
        return festivos.nombreFestivo(hoy)
                .map(nombre -> new FestivoHoyResponse(hoy, true, nombre))
                .orElseGet(() -> new FestivoHoyResponse(hoy, false, null));
    }

    @GetMapping("/{anio}")
    @Operation(summary = "Listar los festivos de un año", description = "Trae la lista completa consultando (o reutilizando el cache de) la API externa")
    @ApiResponse(responseCode = "200", description = "Festivos del año ordenados por fecha (lista vacia si la API externa no responde)")
    @ApiResponse(responseCode = "400", description = "El año no es un numero")
    @ApiResponse(responseCode = "401", description = "Falta el token o esta vencido")
    public List<FestivoResponse> delAnio(@PathVariable int anio) {
        return festivos.festivosDe(anio).entrySet().stream()
                .map(e -> new FestivoResponse(e.getKey(), e.getValue()))
                .sorted(Comparator.comparing(FestivoResponse::fecha))
                .toList();
    }
}
