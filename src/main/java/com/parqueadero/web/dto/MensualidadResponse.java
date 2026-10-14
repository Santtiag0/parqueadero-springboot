package com.parqueadero.web.dto;

import java.time.LocalDate;

import com.parqueadero.model.Mensualidad;

public record MensualidadResponse(
        Long id,
        String placa,
        String titular,
        LocalDate fechaInicio,
        LocalDate fechaFin,
        boolean vigente
) {
    public static MensualidadResponse desde(Mensualidad mensualidad) {
        LocalDate hoy = LocalDate.now();
        boolean vigente = !mensualidad.getFechaInicio().isAfter(hoy) && !mensualidad.getFechaFin().isBefore(hoy);
        return new MensualidadResponse(mensualidad.getId(), mensualidad.getPlaca(), mensualidad.getTitular(),
                mensualidad.getFechaInicio(), mensualidad.getFechaFin(), vigente);
    }
}
