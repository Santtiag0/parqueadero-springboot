package com.parqueadero.model;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "tarifas")
public class Tarifa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_vehiculo", nullable = false, unique = true, length = 20)
    private TipoVehiculo tipoVehiculo;

    @Column(name = "valor_minuto", nullable = false)
    private BigDecimal valorMinuto;

    @Column(name = "tope_dia", nullable = false)
    private BigDecimal topeDia;

    @Column(name = "minutos_gracia", nullable = false)
    private int minutosGracia;

    protected Tarifa() {
    }

    public Tarifa(TipoVehiculo tipoVehiculo, BigDecimal valorMinuto, BigDecimal topeDia, int minutosGracia) {
        this.tipoVehiculo = tipoVehiculo;
        this.valorMinuto = valorMinuto;
        this.topeDia = topeDia;
        this.minutosGracia = minutosGracia;
    }

    public void actualizar(BigDecimal valorMinuto, BigDecimal topeDia, int minutosGracia) {
        this.valorMinuto = valorMinuto;
        this.topeDia = topeDia;
        this.minutosGracia = minutosGracia;
    }

    public Long getId() {
        return id;
    }

    public TipoVehiculo getTipoVehiculo() {
        return tipoVehiculo;
    }

    public BigDecimal getValorMinuto() {
        return valorMinuto;
    }

    public BigDecimal getTopeDia() {
        return topeDia;
    }

    public int getMinutosGracia() {
        return minutosGracia;
    }
}
