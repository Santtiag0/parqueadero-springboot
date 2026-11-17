package com.parqueadero.web;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.parqueadero.model.EstadoTicket;
import com.parqueadero.service.ParqueaderoService;
import com.parqueadero.web.dto.EntradaRequest;
import com.parqueadero.web.dto.SalidaRequest;
import com.parqueadero.web.dto.TicketResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/tickets")
@Tag(name = "Tickets", description = "Entrada y salida de vehiculos del parqueadero")
public class TicketController {

    private final ParqueaderoService parqueaderoService;

    public TicketController(ParqueaderoService parqueaderoService) {
        this.parqueaderoService = parqueaderoService;
    }

    @PostMapping("/entrada")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Registrar la entrada de un vehiculo",
            description = "Asigna el primer cupo libre segun el tipo de vehiculo y crea un ticket ACTIVO. "
                    + "Para bicicletas la cedula del propietario es obligatoria.")
    @ApiResponse(responseCode = "201", description = "Ticket creado y cupo asignado")
    @ApiResponse(responseCode = "400", description = "Placa con formato invalido o falta la cedula de una bicicleta")
    @ApiResponse(responseCode = "401", description = "Falta el token o esta vencido")
    @ApiResponse(responseCode = "409", description = "La placa ya tiene un ticket activo o no hay cupos disponibles")
    public TicketResponse registrarEntrada(@Valid @RequestBody EntradaRequest request) {
        return TicketResponse.desde(
                parqueaderoService.registrarEntrada(request.placa(), request.tipoVehiculo(), request.cedulaPropietario()));
    }

    @PostMapping("/salida")
    @Operation(summary = "Registrar la salida y cobrar",
            description = "Cierra el ticket activo de la placa, calcula el valor (minutos, minutos de gracia, "
                    + "tope diario y recargo si es dia festivo) y libera el cupo. "
                    + "Si la placa tiene mensualidad vigente el valor es 0.")
    @ApiResponse(responseCode = "200", description = "Ticket cerrado con el valor cobrado")
    @ApiResponse(responseCode = "400", description = "Falta la placa")
    @ApiResponse(responseCode = "401", description = "Falta el token o esta vencido")
    @ApiResponse(responseCode = "404", description = "No hay un ticket activo para esa placa")
    public TicketResponse registrarSalida(@Valid @RequestBody SalidaRequest request) {
        return TicketResponse.desde(parqueaderoService.registrarSalida(request.placa()));
    }

    @GetMapping
    @Operation(summary = "Listar tickets por estado", description = "Por defecto devuelve los vehiculos que estan adentro (ACTIVO)")
    @ApiResponse(responseCode = "200", description = "Lista de tickets")
    @ApiResponse(responseCode = "400", description = "Estado no reconocido")
    @ApiResponse(responseCode = "401", description = "Falta el token o esta vencido")
    public List<TicketResponse> listar(
            @Parameter(description = "Estado de los tickets a listar")
            @RequestParam(defaultValue = "ACTIVO") EstadoTicket estado) {
        return parqueaderoService.listarPorEstado(estado).stream()
                .map(TicketResponse::desde)
                .toList();
    }

    @GetMapping("/historial/{placa}")
    @Operation(summary = "Historial de una placa", description = "Todos los tickets, activos y cerrados, de un vehiculo")
    @ApiResponse(responseCode = "200", description = "Tickets de la placa (lista vacia si nunca ha entrado)")
    @ApiResponse(responseCode = "401", description = "Falta el token o esta vencido")
    public List<TicketResponse> historial(
            @Parameter(description = "Placa o serial del vehiculo", example = "ABC123")
            @PathVariable String placa) {
        return parqueaderoService.historial(placa).stream()
                .map(TicketResponse::desde)
                .toList();
    }
}
