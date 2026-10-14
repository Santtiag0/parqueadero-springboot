package com.parqueadero.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SalidaRequest(

        @NotBlank(message = "la placa es obligatoria")
        @Size(max = 6, message = "la placa no puede superar 6 caracteres")
        String placa
) {
}
