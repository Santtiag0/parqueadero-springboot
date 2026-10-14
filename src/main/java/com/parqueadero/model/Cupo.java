package com.parqueadero.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

@Entity
@Table(name = "cupos")
public class Cupo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String codigo;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_vehiculo", nullable = false, length = 20)
    private TipoVehiculo tipoVehiculo;

    @Column(nullable = false)
    private boolean ocupado = false;

    @Version
    private Long version;

    protected Cupo() {
    }

    public Cupo(String codigo, TipoVehiculo tipoVehiculo) {
        this.codigo = codigo;
        this.tipoVehiculo = tipoVehiculo;
        this.ocupado = false;
    }

    public void ocupar() {
        this.ocupado = true;
    }

    public void liberar() {
        this.ocupado = false;
    }

    public Long getId() {
        return id;
    }

    public String getCodigo() {
        return codigo;
    }

    public TipoVehiculo getTipoVehiculo() {
        return tipoVehiculo;
    }

    public boolean isOcupado() {
        return ocupado;
    }

    public Long getVersion() {
        return version;
    }
}
