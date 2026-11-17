package com.parqueadero.web;

import java.util.List;
import java.util.stream.StreamSupport;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.parqueadero.model.TipoVehiculo;
import com.parqueadero.service.TarifaService;
import com.parqueadero.web.dto.TarifaRequest;
import com.parqueadero.web.dto.TarifaResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/tarifas")
@Tag(name = "Tarifas", description = "Valor por minuto, tope diario y recargo de dia festivo por tipo de vehiculo")
public class TarifaController {

    private final TarifaService tarifaService;

    public TarifaController(TarifaService tarifaService) {
        this.tarifaService = tarifaService;
    }

    @GetMapping
    @Operation(summary = "Listar tarifas", description = "Devuelve la tarifa configurada para cada tipo de vehiculo")
    @ApiResponse(responseCode = "200", description = "Lista de tarifas")
    @ApiResponse(responseCode = "401", description = "Falta el token o esta vencido")
    public List<TarifaResponse> listar() {
        return StreamSupport.stream(tarifaService.listar().spliterator(), false)
                .map(TarifaResponse::desde)
                .toList();
    }

    @PutMapping("/{tipo}")
    @Operation(summary = "Actualizar una tarifa", description = "Incluye el recargo que se aplica cuando la salida cae en un dia festivo de Colombia")
    @ApiResponse(responseCode = "200", description = "Tarifa actualizada")
    @ApiResponse(responseCode = "400", description = "Valores faltantes o fuera de rango (recargo entre 0 y 200%) o tipo de vehiculo no reconocido")
    @ApiResponse(responseCode = "401", description = "Falta el token o esta vencido")
    @ApiResponse(responseCode = "404", description = "No existe tarifa configurada para ese tipo de vehiculo")
    public TarifaResponse actualizar(@PathVariable TipoVehiculo tipo, @Valid @RequestBody TarifaRequest request) {
        return TarifaResponse.desde(
                tarifaService.actualizar(tipo, request.valorMinuto(), request.topeDia(), request.minutosGracia(),
                        request.recargoFestivoPorcentaje()));
    }
}
