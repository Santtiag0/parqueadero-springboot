package com.parqueadero.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;

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
class MensualidadControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void registrarYListarMensualidadDeCarro() throws Exception {
        mockMvc.perform(post("/api/v1/mensualidades")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"placa":"ABC123","titular":"Ana Gomez",
                                 "fechaInicio":"2026-01-01","fechaFin":"2026-12-31"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.placa").value("ABC123"))
                .andExpect(jsonPath("$.vigente").value(true));

        mockMvc.perform(get("/api/v1/mensualidades"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].placa").value("ABC123"));
    }

    @Test
    void registrarMensualidadDeBicicletaConSerialValido() throws Exception {
        mockMvc.perform(post("/api/v1/mensualidades")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"placa":"A12","titular":"Juan Perez",
                                 "fechaInicio":"2026-01-01","fechaFin":"2026-12-31"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.placa").value("A12"));
    }

    @Test
    void mensualidadRegistradaDespuesDeLaEntradaCubreElTicketActivo() throws Exception {
        String placa = "MEN123";
        String hoy = LocalDate.now().toString();

        mockMvc.perform(post("/api/v1/tickets/entrada")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"placa\":\"%s\",\"tipoVehiculo\":\"CARRO\"}".formatted(placa)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.conMensualidad").value(false));

        mockMvc.perform(post("/api/v1/mensualidades")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"placa":"%s","titular":"Santiago Martinez",
                                 "fechaInicio":"%s","fechaFin":"%s"}
                                """.formatted(placa, hoy, hoy)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/tickets").param("estado", "ACTIVO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].conMensualidad").value(true));

        mockMvc.perform(post("/api/v1/tickets/salida")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"placa\":\"%s\"}".formatted(placa)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valorCobrado").value(0));
    }

    @Test
    void placaConFormatoInvalidoEsRechazada() throws Exception {
        mockMvc.perform(post("/api/v1/mensualidades")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"placa":"ABCD12","titular":"Juan Perez",
                                 "fechaInicio":"2026-01-01","fechaFin":"2026-12-31"}
                                """))
                .andExpect(status().isBadRequest());
    }
}
