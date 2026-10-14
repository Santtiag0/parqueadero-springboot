package com.parqueadero.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;

import com.parqueadero.web.dto.CierreResponse;
import com.parqueadero.web.dto.IngresoPorTipo;

@Service
public class ReporteService {

    private final JdbcClient jdbcClient;

    public ReporteService(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public CierreResponse cierreDelDia(LocalDate fecha) {
        LocalDateTime inicioDelDia = fecha.atStartOfDay();
        LocalDateTime finDelDia = fecha.plusDays(1).atStartOfDay();

        List<IngresoPorTipo> detalle = jdbcClient.sql("""
                        SELECT tipo_vehiculo, COUNT(*) AS vehiculos,
                               COALESCE(SUM(valor_cobrado), 0) AS total
                        FROM tickets
                        WHERE estado = 'CERRADO' AND hora_salida >= :desde AND hora_salida < :hasta
                        GROUP BY tipo_vehiculo
                        ORDER BY tipo_vehiculo
                        """)
                .param("desde", inicioDelDia)
                .param("hasta", finDelDia)
                .query(IngresoPorTipo.class)
                .list();

        long totalVehiculos = detalle.stream().mapToLong(IngresoPorTipo::vehiculos).sum();
        BigDecimal totalIngresos = detalle.stream()
                .map(IngresoPorTipo::total)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new CierreResponse(fecha, detalle, totalVehiculos, totalIngresos);
    }
}
