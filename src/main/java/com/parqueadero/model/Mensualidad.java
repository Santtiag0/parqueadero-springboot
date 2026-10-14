package com.parqueadero.model;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "mensualidades")
public class Mensualidad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 6)
    private String placa;

    @Column(nullable = false, length = 100)
    private String titular;

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDate fechaInicio;

    @Column(name = "fecha_fin", nullable = false)
    private LocalDate fechaFin;

    protected Mensualidad() {
    }

    public Mensualidad(String placa, String titular, LocalDate fechaInicio, LocalDate fechaFin) {
        this.placa = placa;
        this.titular = titular;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
    }

    public Long getId() {
        return id;
    }

    public String getPlaca() {
        return placa;
    }

    public String getTitular() {
        return titular;
    }

    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    public LocalDate getFechaFin() {
        return fechaFin;
    }
}
