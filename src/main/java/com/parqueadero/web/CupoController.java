package com.parqueadero.web;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.parqueadero.service.CupoService;
import com.parqueadero.web.dto.DisponibilidadItem;

@RestController
@RequestMapping("/api/v1/cupos")
public class CupoController {

    private final CupoService cupoService;

    public CupoController(CupoService cupoService) {
        this.cupoService = cupoService;
    }

    @GetMapping("/disponibilidad")
    public List<DisponibilidadItem> disponibilidad() {
        return cupoService.disponibilidad();
    }
}
