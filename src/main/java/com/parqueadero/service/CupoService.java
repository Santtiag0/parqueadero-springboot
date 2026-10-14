package com.parqueadero.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.parqueadero.model.TipoVehiculo;
import com.parqueadero.repository.CupoRepository;
import com.parqueadero.web.dto.DisponibilidadItem;

@Service
public class CupoService {

    private final CupoRepository cupos;

    public CupoService(CupoRepository cupos) {
        this.cupos = cupos;
    }

    @Transactional(readOnly = true)
    public List<DisponibilidadItem> disponibilidad() {
        return List.of(TipoVehiculo.values()).stream()
                .map(tipo -> new DisponibilidadItem(
                        tipo,
                        cupos.countByTipoVehiculo(tipo),
                        cupos.countByTipoVehiculoAndOcupadoFalse(tipo)))
                .toList();
    }
}
