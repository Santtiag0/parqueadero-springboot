package com.parqueadero.web.dto;

import com.parqueadero.model.TipoVehiculo;

public record DisponibilidadItem(
        TipoVehiculo tipoVehiculo,
        long total,
        long libres
) {
}
