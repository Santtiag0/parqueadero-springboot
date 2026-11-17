package com.parqueadero.service;

import java.time.LocalDateTime;

/**
 * Forma exacta de cada elemento que devuelve https://api-colombia.com/api/v1/Holiday/year/{anio}:
 * [{"date":"2026-01-01T00:00:00","name":"Año Nuevo"}, ...]
 */
public record FestivoExterno(LocalDateTime date, String name) {
}
