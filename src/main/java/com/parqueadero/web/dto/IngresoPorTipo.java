package com.parqueadero.web.dto;

import java.math.BigDecimal;

public record IngresoPorTipo(
        String tipoVehiculo,
        long vehiculos,
        BigDecimal total
) {
}
