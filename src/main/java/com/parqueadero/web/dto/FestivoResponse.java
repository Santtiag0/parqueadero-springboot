package com.parqueadero.web.dto;

import java.time.LocalDate;

public record FestivoResponse(
        LocalDate fecha,
        String nombre
) {
}
