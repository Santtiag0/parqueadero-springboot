package com.parqueadero.web.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.parqueadero.model.EstadoTicket;
import com.parqueadero.model.Ticket;
import com.parqueadero.model.TipoVehiculo;

public record TicketResponse(
        Long id,
        String placa,
        TipoVehiculo tipoVehiculo,
        String cupoCodigo,
        LocalDateTime horaEntrada,
        LocalDateTime horaSalida,
        BigDecimal valorCobrado,
        boolean conMensualidad,
        EstadoTicket estado,
        String cedulaPropietario
) {
    public static TicketResponse desde(Ticket ticket) {
        return new TicketResponse(
                ticket.getId(),
                ticket.getPlaca(),
                ticket.getTipoVehiculo(),
                ticket.getCupo().getCodigo(),
                ticket.getHoraEntrada(),
                ticket.getHoraSalida(),
                ticket.getValorCobrado(),
                ticket.isConMensualidad(),
                ticket.getEstado(),
                ticket.getCedulaPropietario()
        );
    }
}
