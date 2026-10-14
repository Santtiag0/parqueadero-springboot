package com.parqueadero.web.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record MensualidadRequest(

        @NotBlank(message = "la placa es obligatoria")
        @Size(max = 6, message = "la placa no puede superar 6 caracteres")
        String placa,

        @NotBlank(message = "el titular es obligatorio")
        @Size(max = 100, message = "el titular no puede superar 100 caracteres")
        String titular,

        @NotNull(message = "la fecha de inicio es obligatoria")
        LocalDate fechaInicio,

        @NotNull(message = "la fecha de fin es obligatoria")
        @FutureOrPresent(message = "la fecha de fin no puede estar en el pasado")
        LocalDate fechaFin
) {
}
