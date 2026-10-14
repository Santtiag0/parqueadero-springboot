package com.parqueadero.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.parqueadero.model.Mensualidad;

public interface MensualidadRepository extends JpaRepository<Mensualidad, Long> {

    @Query("""
            select m from Mensualidad m
            where m.placa = :placa
              and m.fechaInicio <= :fecha and m.fechaFin >= :fecha
            """)
    List<Mensualidad> vigentes(@Param("placa") String placa, @Param("fecha") LocalDate fecha);
}
