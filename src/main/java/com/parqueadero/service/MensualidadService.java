package com.parqueadero.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.parqueadero.exception.ConflictoDeNegocioException;
import com.parqueadero.model.EstadoTicket;
import com.parqueadero.model.Mensualidad;
import com.parqueadero.repository.MensualidadRepository;
import com.parqueadero.repository.TicketRepository;

@Service
public class MensualidadService {

    private final MensualidadRepository mensualidades;
    private final TicketRepository tickets;

    public MensualidadService(MensualidadRepository mensualidades, TicketRepository tickets) {
        this.mensualidades = mensualidades;
        this.tickets = tickets;
    }

    @Transactional
    public Mensualidad registrar(String placa, String titular, LocalDate fechaInicio, LocalDate fechaFin) {
        String placaNormalizada = placa.trim().toUpperCase();

        if (!ValidadorPlaca.esAlgunFormatoValido(placaNormalizada)) {
            throw new IllegalArgumentException(
                    "La placa/serial no corresponde a ningun formato valido: carro (AAA111), moto (AAA11A) "
                            + "o bicicleta (3 caracteres alfanumericos)");
        }
        if (fechaFin.isBefore(fechaInicio)) {
            throw new ConflictoDeNegocioException("La fecha de fin no puede ser anterior a la fecha de inicio");
        }

        Mensualidad mensualidad = new Mensualidad(placaNormalizada, titular, fechaInicio, fechaFin);
        Mensualidad guardada = mensualidades.save(mensualidad);

        LocalDate hoy = LocalDate.now();
        boolean vigenteHoy = !fechaInicio.isAfter(hoy) && !fechaFin.isBefore(hoy);
        if (vigenteHoy) {
            tickets.findByPlacaAndEstado(placaNormalizada, EstadoTicket.ACTIVO).ifPresent(ticket -> {
                ticket.marcarConMensualidad();
                tickets.save(ticket);
            });
        }

        return guardada;
    }

    @Transactional(readOnly = true)
    public List<Mensualidad> listar() {
        return mensualidades.findAll(Sort.by(Sort.Direction.DESC, "fechaInicio"));
    }
}
