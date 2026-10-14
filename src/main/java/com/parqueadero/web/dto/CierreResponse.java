package com.parqueadero.web.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record CierreResponse(
        LocalDate fecha,
        List<IngresoPorTipo> detalle,
        long totalVehiculos,
        BigDecimal totalIngresos
) {
}
