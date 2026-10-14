package com.parqueadero.web.dto;

import com.parqueadero.model.TipoVehiculo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record EntradaRequest(

        @NotBlank(message = "la placa es obligatoria")
        @Size(max = 6, message = "la placa no puede superar 6 caracteres")
        String placa,

        @NotNull(message = "el tipo de vehiculo es obligatorio")
        TipoVehiculo tipoVehiculo,

        @Size(max = 15, message = "la cedula no puede superar 15 caracteres")
        String cedulaPropietario
) {
}
