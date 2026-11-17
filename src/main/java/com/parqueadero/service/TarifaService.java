package com.parqueadero.service;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.parqueadero.exception.RecursoNoEncontradoException;
import com.parqueadero.model.Tarifa;
import com.parqueadero.model.TipoVehiculo;
import com.parqueadero.repository.TarifaRepository;

@Service
public class TarifaService {

    private final TarifaRepository tarifas;

    public TarifaService(TarifaRepository tarifas) {
        this.tarifas = tarifas;
    }

    @Transactional
    public Tarifa actualizar(TipoVehiculo tipoVehiculo, BigDecimal valorMinuto, BigDecimal topeDia,
                              int minutosGracia, int recargoFestivoPorcentaje) {
        Tarifa tarifa = tarifas.findByTipoVehiculo(tipoVehiculo)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No existe una tarifa configurada para " + tipoVehiculo));
        tarifa.actualizar(valorMinuto, topeDia, minutosGracia, recargoFestivoPorcentaje);
        return tarifas.save(tarifa);
    }

    @Transactional(readOnly = true)
    public Iterable<Tarifa> listar() {
        return tarifas.findAll();
    }
}
