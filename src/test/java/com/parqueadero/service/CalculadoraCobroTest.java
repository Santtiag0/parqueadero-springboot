package com.parqueadero.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import com.parqueadero.model.Tarifa;
import com.parqueadero.model.TipoVehiculo;

class CalculadoraCobroTest {

    // Tarifa de ejemplo tomada del material del curso: $120/min, tope $25.000, 5 min de gracia.
    private final Tarifa tarifaCarro = new Tarifa(TipoVehiculo.CARRO,
            BigDecimal.valueOf(120), BigDecimal.valueOf(25000), 5);

    private final LocalDateTime entrada = LocalDateTime.of(2026, 1, 1, 8, 0);

    @Test
    void dentroDelTiempoDeGraciaNoCobra() {
        LocalDateTime salida = entrada.plusMinutes(4);

        BigDecimal valor = CalculadoraCobro.calcular(entrada, salida, tarifaCarro);

        assertThat(valor).isEqualByComparingTo("0");
    }

    @Test
    void dosHorasYMediaCobraPorMinuto() {
        LocalDateTime salida = entrada.plusMinutes(150);

        BigDecimal valor = CalculadoraCobro.calcular(entrada, salida, tarifaCarro);

        assertThat(valor).isEqualByComparingTo("18000");
    }

    @Test
    void cincoHorasAplicaElTopeDelDia() {
        LocalDateTime salida = entrada.plusHours(5);

        BigDecimal valor = CalculadoraCobro.calcular(entrada, salida, tarifaCarro);

        assertThat(valor).isEqualByComparingTo("25000");
    }

    @Test
    void veintiseisHorasSumaUnTopeMasElRestante() {
        LocalDateTime salida = entrada.plusHours(26);

        BigDecimal valor = CalculadoraCobro.calcular(entrada, salida, tarifaCarro);

        assertThat(valor).isEqualByComparingTo("39400");
    }

    @Test
    void unaFraccionDeMinutoCuentaComoMinutoCompleto() {
        LocalDateTime salida = entrada.plusMinutes(10).plusSeconds(1);

        BigDecimal valor = CalculadoraCobro.calcular(entrada, salida, tarifaCarro);

        // 11 minutos facturables (la fraccion de 1 segundo redondea hacia arriba)
        assertThat(valor).isEqualByComparingTo("1320");
    }

    @Test
    void salidaAnteriorALaEntradaLanzaExcepcion() {
        LocalDateTime salida = entrada.minusMinutes(1);

        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class,
                () -> CalculadoraCobro.calcular(entrada, salida, tarifaCarro));
    }
}
