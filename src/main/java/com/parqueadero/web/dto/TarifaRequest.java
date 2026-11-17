package com.parqueadero.web.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record TarifaRequest(

        @NotNull(message = "el valor por minuto es obligatorio")
        @Positive(message = "el valor por minuto debe ser positivo")
        BigDecimal valorMinuto,

        @NotNull(message = "el tope del dia es obligatorio")
        @Positive(message = "el tope del dia debe ser positivo")
        BigDecimal topeDia,

        @Min(value = 0, message = "los minutos de gracia no pueden ser negativos")
        int minutosGracia,

        @Min(value = 0, message = "el recargo de festivo no puede ser negativo")
        @Max(value = 200, message = "el recargo de festivo no puede superar 200%")
        int recargoFestivoPorcentaje
) {
}
