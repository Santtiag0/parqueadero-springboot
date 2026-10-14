package com.parqueadero.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.parqueadero.model.Tarifa;
import com.parqueadero.model.TipoVehiculo;

public interface TarifaRepository extends JpaRepository<Tarifa, Long> {

    Optional<Tarifa> findByTipoVehiculo(TipoVehiculo tipoVehiculo);
}
