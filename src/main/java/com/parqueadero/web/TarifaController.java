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

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/tarifas")
public class TarifaController {

    private final TarifaService tarifaService;

    public TarifaController(TarifaService tarifaService) {
        this.tarifaService = tarifaService;
    }

    @GetMapping
    public List<TarifaResponse> listar() {
        return StreamSupport.stream(tarifaService.listar().spliterator(), false)
                .map(TarifaResponse::desde)
                .toList();
    }

    @PutMapping("/{tipo}")
    public TarifaResponse actualizar(@PathVariable TipoVehiculo tipo, @Valid @RequestBody TarifaRequest request) {
        return TarifaResponse.desde(
                tarifaService.actualizar(tipo, request.valorMinuto(), request.topeDia(), request.minutosGracia()));
    }
}
