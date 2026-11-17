package com.parqueadero.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

/**
 * Consulta los dias festivos de Colombia en una API publica externa
 * (https://api-colombia.com) y los guarda en cache por año, para no tener
 * que llamarla en cada ticket. Si la API externa falla, el parqueadero
 * sigue funcionando: simplemente ese dia no se trata como festivo.
 */
@Service
public class FestivoService {

    private static final Logger log = LoggerFactory.getLogger(FestivoService.class);

    private final RestClient restClient;
    private final Map<Integer, Map<LocalDate, String>> cachePorAnio = new ConcurrentHashMap<>();

    public FestivoService(RestClient.Builder builder) {
        this.restClient = builder.baseUrl("https://api-colombia.com/api/v1").build();
    }

    public boolean esFestivo(LocalDate fecha) {
        return festivosDe(fecha.getYear()).containsKey(fecha);
    }

    public Optional<String> nombreFestivo(LocalDate fecha) {
        return Optional.ofNullable(festivosDe(fecha.getYear()).get(fecha));
    }

    public Map<LocalDate, String> festivosDe(int anio) {
        return cachePorAnio.computeIfAbsent(anio, this::consultarFestivosExternos);
    }

    private Map<LocalDate, String> consultarFestivosExternos(int anio) {
        try {
            List<FestivoExterno> festivos = restClient.get()
                    .uri("/Holiday/year/{anio}", anio)
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<FestivoExterno>>() {
                    });

            if (festivos == null) {
                return Map.of();
            }

            return festivos.stream()
                    .collect(Collectors.toMap(f -> f.date().toLocalDate(), FestivoExterno::name, (a, b) -> a));
        } catch (Exception ex) {
            log.warn("No se pudo consultar los festivos de {} en api-colombia.com: {}", anio, ex.getMessage());
            return Map.of();
        }
    }
}
