package com.parqueadero.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.parqueadero.model.EstadoTicket;
import com.parqueadero.model.Ticket;

public interface TicketRepository extends JpaRepository<Ticket, Long> {

    Optional<Ticket> findByPlacaAndEstado(String placa, EstadoTicket estado);

    List<Ticket> findByEstado(EstadoTicket estado);

    @Query(value = """
            SELECT * FROM tickets
            WHERE placa = :placa
            ORDER BY hora_entrada DESC
            """, nativeQuery = true)
    List<Ticket> historialPorPlaca(@Param("placa") String placa);
}
