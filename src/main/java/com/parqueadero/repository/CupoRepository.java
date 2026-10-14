package com.parqueadero.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.parqueadero.model.Cupo;
import com.parqueadero.model.TipoVehiculo;

public interface CupoRepository extends JpaRepository<Cupo, Long> {

    Optional<Cupo> findFirstByTipoVehiculoAndOcupadoFalse(TipoVehiculo tipoVehiculo);

    List<Cupo> findByTipoVehiculo(TipoVehiculo tipoVehiculo);

    long countByTipoVehiculo(TipoVehiculo tipoVehiculo);

    long countByTipoVehiculoAndOcupadoFalse(TipoVehiculo tipoVehiculo);
}
