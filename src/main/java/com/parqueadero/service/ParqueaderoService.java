package com.parqueadero.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.parqueadero.exception.ConflictoDeNegocioException;
import com.parqueadero.exception.RecursoNoEncontradoException;
import com.parqueadero.model.Cupo;
import com.parqueadero.model.EstadoTicket;
import com.parqueadero.model.Tarifa;
import com.parqueadero.model.Ticket;
import com.parqueadero.model.TipoVehiculo;
import com.parqueadero.repository.CupoRepository;
import com.parqueadero.repository.MensualidadRepository;
import com.parqueadero.repository.TarifaRepository;
import com.parqueadero.repository.TicketRepository;

@Service
public class ParqueaderoService {

    private final CupoRepository cupos;
    private final TicketRepository tickets;
    private final TarifaRepository tarifas;
    private final MensualidadRepository mensualidades;

    public ParqueaderoService(CupoRepository cupos, TicketRepository tickets, TarifaRepository tarifas,
                               MensualidadRepository mensualidades) {
        this.cupos = cupos;
        this.tickets = tickets;
        this.tarifas = tarifas;
        this.mensualidades = mensualidades;
    }

    @Transactional
    public Ticket registrarEntrada(String placa, TipoVehiculo tipoVehiculo, String cedulaPropietario) {
        String placaNormalizada = placa.trim().toUpperCase();

        ValidadorPlaca.validarParaTipo(placaNormalizada, tipoVehiculo);

        if (tipoVehiculo == TipoVehiculo.BICICLETA && (cedulaPropietario == null || cedulaPropietario.isBlank())) {
            throw new IllegalArgumentException("La cedula del propietario es obligatoria para bicicletas");
        }

        tickets.findByPlacaAndEstado(placaNormalizada, EstadoTicket.ACTIVO).ifPresent(t -> {
            throw new ConflictoDeNegocioException("La placa " + placaNormalizada + " ya tiene un ticket activo");
        });

        Cupo cupo = cupos.findFirstByTipoVehiculoAndOcupadoFalse(tipoVehiculo)
                .orElseThrow(() -> new ConflictoDeNegocioException(
                        "No hay cupos disponibles para " + tipoVehiculo));

        cupo.ocupar();
        cupos.save(cupo);

        boolean tieneMensualidad = !mensualidades.vigentes(placaNormalizada, LocalDate.now()).isEmpty();

        String cedulaNormalizada = tipoVehiculo == TipoVehiculo.BICICLETA ? cedulaPropietario.trim() : null;

        Ticket ticket = new Ticket(placaNormalizada, tipoVehiculo, cupo, LocalDateTime.now(), tieneMensualidad,
                cedulaNormalizada);
        return tickets.save(ticket);
    }

    @Transactional
    public Ticket registrarSalida(String placa) {
        String placaNormalizada = placa.trim().toUpperCase();

        Ticket ticket = tickets.findByPlacaAndEstado(placaNormalizada, EstadoTicket.ACTIVO)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No hay un ticket activo para la placa " + placaNormalizada));

        LocalDateTime horaSalida = LocalDateTime.now();
        BigDecimal valorCobrado;

        if (ticket.isConMensualidad()) {
            valorCobrado = BigDecimal.ZERO;
        } else {
            Tarifa tarifa = tarifas.findByTipoVehiculo(ticket.getTipoVehiculo())
                    .orElseThrow(() -> new RecursoNoEncontradoException(
                            "No hay tarifa configurada para " + ticket.getTipoVehiculo()));
            valorCobrado = CalculadoraCobro.calcular(ticket.getHoraEntrada(), horaSalida, tarifa);
        }

        ticket.registrarSalida(horaSalida, valorCobrado);

        Cupo cupo = ticket.getCupo();
        cupo.liberar();
        cupos.save(cupo);

        return tickets.save(ticket);
    }

    @Transactional(readOnly = true)
    public List<Ticket> listarPorEstado(EstadoTicket estado) {
        return tickets.findByEstado(estado);
    }

    @Transactional(readOnly = true)
    public List<Ticket> historial(String placa) {
        return tickets.historialPorPlaca(placa.trim().toUpperCase());
    }
}
