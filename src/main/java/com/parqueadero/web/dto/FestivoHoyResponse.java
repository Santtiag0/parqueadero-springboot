package com.parqueadero.web.dto;

import java.time.LocalDate;

public record FestivoHoyResponse(
        LocalDate fecha,
        boolean esFestivo,
        String nombre
) {
}
