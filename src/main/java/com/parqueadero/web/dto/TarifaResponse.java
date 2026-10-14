package com.parqueadero.web.dto;

import java.math.BigDecimal;

import com.parqueadero.model.Tarifa;
import com.parqueadero.model.TipoVehiculo;

public record TarifaResponse(
        TipoVehiculo tipoVehiculo,
        BigDecimal valorMinuto,
        BigDecimal topeDia,
        int minutosGracia
) {
    public static TarifaResponse desde(Tarifa tarifa) {
        return new TarifaResponse(tarifa.getTipoVehiculo(), tarifa.getValorMinuto(), tarifa.getTopeDia(),
                tarifa.getMinutosGracia());
    }
}
