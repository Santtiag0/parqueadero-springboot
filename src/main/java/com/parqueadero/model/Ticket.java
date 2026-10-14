package com.parqueadero.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "tickets")
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 6)
    private String placa;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_vehiculo", nullable = false, length = 20)
    private TipoVehiculo tipoVehiculo;

    @ManyToOne(optional = false)
    @JoinColumn(name = "cupo_id", nullable = false)
    private Cupo cupo;

    @Column(name = "hora_entrada", nullable = false)
    private LocalDateTime horaEntrada;

    @Column(name = "hora_salida")
    private LocalDateTime horaSalida;

    @Column(name = "valor_cobrado")
    private BigDecimal valorCobrado;

    @Column(name = "con_mensualidad", nullable = false)
    private boolean conMensualidad;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private EstadoTicket estado;

    @Column(name = "cedula_propietario", length = 15)
    private String cedulaPropietario;

    protected Ticket() {
    }

    public Ticket(String placa, TipoVehiculo tipoVehiculo, Cupo cupo, LocalDateTime horaEntrada,
                  boolean conMensualidad, String cedulaPropietario) {
        this.placa = placa;
        this.tipoVehiculo = tipoVehiculo;
        this.cupo = cupo;
        this.horaEntrada = horaEntrada;
        this.conMensualidad = conMensualidad;
        this.estado = EstadoTicket.ACTIVO;
        this.cedulaPropietario = cedulaPropietario;
    }

    public void registrarSalida(LocalDateTime horaSalida, BigDecimal valorCobrado) {
        this.horaSalida = horaSalida;
        this.valorCobrado = valorCobrado;
        this.estado = EstadoTicket.CERRADO;
    }

    public void marcarConMensualidad() {
        this.conMensualidad = true;
    }

    public Long getId() {
        return id;
    }

    public String getPlaca() {
        return placa;
    }

    public TipoVehiculo getTipoVehiculo() {
        return tipoVehiculo;
    }

    public Cupo getCupo() {
        return cupo;
    }

    public LocalDateTime getHoraEntrada() {
        return horaEntrada;
    }

    public LocalDateTime getHoraSalida() {
        return horaSalida;
    }

    public BigDecimal getValorCobrado() {
        return valorCobrado;
    }

    public boolean isConMensualidad() {
        return conMensualidad;
    }

    public EstadoTicket getEstado() {
        return estado;
    }

    public String getCedulaPropietario() {
        return cedulaPropietario;
    }
}
