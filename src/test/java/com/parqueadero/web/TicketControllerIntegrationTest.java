package com.parqueadero.web;

import static org.hamcrest.Matchers.greaterThan;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@WithMockUser
class TicketControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void registrarEntradaYSalidaCalculaElCobro() throws Exception {
        String placa = "ABC123";

        mockMvc.perform(post("/api/v1/tickets/entrada")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"placa\":\"%s\",\"tipoVehiculo\":\"CARRO\"}".formatted(placa)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.placa").value(placa))
                .andExpect(jsonPath("$.estado").value("ACTIVO"))
                .andExpect(jsonPath("$.cupoCodigo").value("C-01"));

        mockMvc.perform(get("/api/v1/tickets").param("estado", "ACTIVO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].placa").value(placa));

        mockMvc.perform(post("/api/v1/tickets/salida")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"placa\":\"%s\"}".formatted(placa)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("CERRADO"))
                .andExpect(jsonPath("$.valorCobrado").exists());

        mockMvc.perform(get("/api/v1/tickets/historial/" + placa))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void noPermiteDosEntradasActivasParaLaMismaPlaca() throws Exception {
        String placa = "XYZ99Z";
        String cuerpo = "{\"placa\":\"%s\",\"tipoVehiculo\":\"MOTO\"}".formatted(placa);

        mockMvc.perform(post("/api/v1/tickets/entrada")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/tickets/entrada")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo))
                .andExpect(status().isConflict());
    }

    @Test
    void salidaSinTicketActivoDevuelveNotFound() throws Exception {
        mockMvc.perform(post("/api/v1/tickets/salida")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"placa\":\"ZZZ999\"}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void entradaConPlacaVaciaDevuelveBadRequest() throws Exception {
        mockMvc.perform(post("/api/v1/tickets/entrada")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"placa\":\"\",\"tipoVehiculo\":\"CARRO\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void disponibilidadMuestraCuposLibres() throws Exception {
        mockMvc.perform(get("/api/v1/cupos/disponibilidad"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(greaterThan(0)));
    }

    @Test
    void entradaDeBicicletaSinCedulaDevuelveBadRequest() throws Exception {
        mockMvc.perform(post("/api/v1/tickets/entrada")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"placa\":\"A12\",\"tipoVehiculo\":\"BICICLETA\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void entradaDeBicicletaConCedulaQuedaRegistrada() throws Exception {
        mockMvc.perform(post("/api/v1/tickets/entrada")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"placa\":\"A12\",\"tipoVehiculo\":\"BICICLETA\",\"cedulaPropietario\":\"1020304050\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.cedulaPropietario").value("1020304050"))
                .andExpect(jsonPath("$.cupoCodigo").value("B-01"));
    }

    @Test
    void placaDeCarroSoloConNumerosEsRechazada() throws Exception {
        mockMvc.perform(post("/api/v1/tickets/entrada")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"placa\":\"123456\",\"tipoVehiculo\":\"CARRO\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void placaDeCarroSoloConLetrasEsRechazada() throws Exception {
        mockMvc.perform(post("/api/v1/tickets/entrada")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"placa\":\"ABCDEF\",\"tipoVehiculo\":\"CARRO\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void placaDeMotoConFormatoDeCarroEsRechazada() throws Exception {
        mockMvc.perform(post("/api/v1/tickets/entrada")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"placa\":\"ABC123\",\"tipoVehiculo\":\"MOTO\"}"))
                .andExpect(status().isBadRequest());
    }
}
