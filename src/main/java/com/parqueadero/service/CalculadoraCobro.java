package com.parqueadero.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;

import com.parqueadero.model.Tarifa;

/**
 * Minutos y fracciones: toda fraccion de minuto cuenta como minuto completo.
 * Por debajo del tiempo de gracia el cobro es cero; por encima, se cobra la
 * estadia completa (la gracia no se resta del tiempo facturable).
 * El tope diario se aplica por cada bloque de 24 horas completas.
 * Si el dia de la salida es festivo, se aplica al final el recargo de la tarifa.
 */
public final class CalculadoraCobro {

    private static final int MINUTOS_POR_DIA = 24 * 60;
    private static final BigDecimal CIEN = BigDecimal.valueOf(100);

    private CalculadoraCobro() {
    }

    public static BigDecimal calcular(LocalDateTime horaEntrada, LocalDateTime horaSalida, Tarifa tarifa,
                                       boolean esFestivo) {
        if (horaSalida.isBefore(horaEntrada)) {
            throw new IllegalArgumentException("La hora de salida no puede ser anterior a la hora de entrada");
        }

        long segundos = Duration.between(horaEntrada, horaSalida).getSeconds();
        long minutos = (segundos + 59) / 60;

        if (minutos <= tarifa.getMinutosGracia()) {
            return BigDecimal.ZERO;
        }

        long diasCompletos = minutos / MINUTOS_POR_DIA;
        long minutosRestantes = minutos % MINUTOS_POR_DIA;

        BigDecimal costoDiasCompletos = tarifa.getTopeDia().multiply(BigDecimal.valueOf(diasCompletos));
        BigDecimal costoRestante = tarifa.getValorMinuto().multiply(BigDecimal.valueOf(minutosRestantes));
        if (costoRestante.compareTo(tarifa.getTopeDia()) > 0) {
            costoRestante = tarifa.getTopeDia();
        }

        BigDecimal total = costoDiasCompletos.add(costoRestante);

        if (esFestivo && tarifa.getRecargoFestivoPorcentaje() > 0) {
            BigDecimal factor = BigDecimal.ONE.add(
                    BigDecimal.valueOf(tarifa.getRecargoFestivoPorcentaje()).divide(CIEN));
            total = total.multiply(factor).setScale(0, RoundingMode.HALF_UP);
        }

        return total;
    }
}
