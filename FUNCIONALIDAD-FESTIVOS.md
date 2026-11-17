# Recargo por día festivo — consumo de API externa

## 1. Motivación

El profesor indicó que el proyecto necesitaba demostrar dos cosas concretas:

1. **Consumir una API de un tercero** desde el backend (no solo exponer la propia).
2. **Documentar la propia API** con Swagger, de forma que se pueda abrir, inspeccionar y
   probar sin leer el código fuente.

Para que esto fuera más que un ejercicio aislado, la función elegida (recargo de tarifa
en días festivos) **toca lógica de negocio real**: cambia cuánto paga un cliente al salir,
usando datos de verdad. No es un widget decorativo.

---

## 2. La API externa

**Proveedor:** [api-colombia.com](https://api-colombia.com) — pública, gratuita, sin API key.

**Endpoint usado:**
```
GET https://api-colombia.com/api/v1/Holiday/year/{año}
```

**Respuesta real** (verificada en vivo antes de programar nada, no asumida):
```json
[
  {"date":"2026-01-01T00:00:00","name":"Año Nuevo"},
  {"date":"2026-01-12T00:00:00","name":"Día de los Reyes Magos"},
  {"date":"2026-04-02T00:00:00","name":"Jueves Santo"}
]
```

No requiere autenticación ni headers especiales. El campo `date` viene en formato
`LocalDateTime` (con hora `00:00:00`, que se descarta — solo interesa el día).

---

## 3. Cómo se consume: `FestivoService`

Archivo: `src/main/java/com/parqueadero/service/FestivoService.java`

### 3.1 El cliente HTTP

Se usa `RestClient` (la forma moderna en Spring 6.1+/Boot 3.2+, reemplaza a
`RestTemplate`). Spring Boot ya trae un bean `RestClient.Builder` autoconfigurado
— no hace falta escribir configuración extra, solo inyectarlo:

```java
public FestivoService(RestClient.Builder builder) {
    this.restClient = builder.baseUrl("https://api-colombia.com/api/v1").build();
}
```

### 3.2 Caché por año

Llamar a la API externa en **cada** salida de vehículo sería lento e innecesario
(los festivos de un año no cambian durante el año). Por eso se cachea en memoria:

```java
private final Map<Integer, Map<LocalDate, String>> cachePorAnio = new ConcurrentHashMap<>();

public Map<LocalDate, String> festivosDe(int anio) {
    return cachePorAnio.computeIfAbsent(anio, this::consultarFestivosExternos);
}
```

`computeIfAbsent` garantiza que la API externa solo se llama **una vez por año**,
la primera vez que se necesita esa fecha. Las siguientes consultas de ese mismo año
se resuelven desde memoria, instantáneo.

### 3.3 Resiliencia: qué pasa si la API externa falla

Esto es importante y fue una decisión de diseño explícita: **el parqueadero no puede
dejar de funcionar porque un servicio de terceros esté caído.**

```java
private Map<LocalDate, String> consultarFestivosExternos(int anio) {
    try {
        List<FestivoExterno> festivos = restClient.get()
                .uri("/Holiday/year/{anio}", anio)
                .retrieve()
                .body(new ParameterizedTypeReference<List<FestivoExterno>>() {});
        ...
    } catch (Exception ex) {
        log.warn("No se pudo consultar los festivos de {} en api-colombia.com: {}", anio, ex.getMessage());
        return Map.of();
    }
}
```

Si la llamada falla (timeout, 500, sin internet, lo que sea): se registra un
`warning` en el log y se devuelve un mapa vacío. Resultado práctico: **ese día
simplemente no se trata como festivo** — el ticket se cobra con la tarifa normal,
sin recargo, pero el sistema sigue registrando entradas y salidas sin interrupción.

### 3.4 Los dos métodos públicos

```java
public boolean esFestivo(LocalDate fecha)              // true/false
public Optional<String> nombreFestivo(LocalDate fecha)  // "Día de la Raza", vacío si no aplica
```

---

## 4. Dónde se usa: el cálculo del cobro

### 4.1 El campo nuevo en `Tarifa`

Cada tipo de vehículo tiene ahora un `recargoFestivoPorcentaje` (entero, 0–200):

```java
@Column(name = "recargo_festivo_porcentaje", nullable = false)
private int recargoFestivoPorcentaje;
```

Valores sembrados en `data.sql`:

| Tipo | Recargo |
|---|---|
| CARRO | 20% |
| MOTO | 20% |
| BICICLETA | 0% |

### 4.2 `CalculadoraCobro`: el recargo se aplica al final

```java
public static BigDecimal calcular(LocalDateTime horaEntrada, LocalDateTime horaSalida,
                                   Tarifa tarifa, boolean esFestivo) {
    // ... toda la logica existente: minutos, gracia, tope diario ...
    BigDecimal total = costoDiasCompletos.add(costoRestante);

    if (esFestivo && tarifa.getRecargoFestivoPorcentaje() > 0) {
        BigDecimal factor = BigDecimal.ONE.add(
                BigDecimal.valueOf(tarifa.getRecargoFestivoPorcentaje()).divide(CIEN));
        total = total.multiply(factor).setScale(0, RoundingMode.HALF_UP);
    }

    return total;
}
```

**Por qué al final y no antes:** el recargo se aplica sobre el resultado *ya*
calculado (incluyendo el tope diario), no sobre el valor por minuto. Así, si una
estadía larga ya tocó el tope, el recargo sigue aplicando sobre ese tope —
no se "escapa" del límite diario.

**Ejemplo real** (de las pruebas automatizadas):
- Carro, 2h30min, día normal → `150 min × $120 = $18.000`
- Mismo caso, día festivo (recargo 20%) → `$18.000 × 1.20 = $21.600`
- Carro, 5 horas (toca el tope de $25.000), día festivo → `$25.000 × 1.20 = $30.000`

### 4.3 Quién decide si es festivo: `ParqueaderoService`

```java
boolean esFestivo = festivos.esFestivo(horaSalida.toLocalDate());
valorCobrado = CalculadoraCobro.calcular(ticket.getHoraEntrada(), horaSalida, tarifa, esFestivo);
```

Se usa la fecha de **salida** (el momento del cobro), no la de entrada. Si alguien
entra un día normal y sale en festivo, paga el recargo — tiene sentido: el cobro
ocurre al salir.

---

## 5. El endpoint propio: `FestivoController`

No basta con consumir la API externa puertas adentro — también se expone como
parte de **nuestra propia API**, para que el frontend (o cualquier cliente) pueda
consultarlo:

```
GET /api/v1/festivos/hoy        → { "fecha": "2026-10-12", "esFestivo": true, "nombre": "Día de la Raza" }
GET /api/v1/festivos/{año}      → lista completa de festivos de ese año (desde cache)
```

Ambos requieren el mismo token JWT que el resto de la API (son rutas normales,
no públicas).

---

## 6. Swagger / OpenAPI

### 6.1 La dependencia

```xml
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.6.0</version>
</dependency>
```

Con **solo esto**, Spring genera automáticamente la documentación de **todos** los
endpoints existentes (no solo los nuevos) — lee las anotaciones `@RestController`,
`@GetMapping`, `@RequestBody`, etc. que ya estaban en el código.

### 6.2 Qué se agregó a mano

- `OpenApiConfig.java`: título, descripción, y un **esquema de seguridad** (`bearerAuth`)
  que hace aparecer un botón **"Authorize"** en la interfaz de Swagger. Ahí se pega el
  token que entrega `/api/v1/auth/login`, y desde ese momento se puede probar
  *cualquier* endpoint protegido directamente desde el navegador, sin Postman ni curl.
- `@Tag` y `@Operation` en `TarifaController` y `FestivoController`: le dan nombre y
  descripción a cada grupo de endpoints y a cada operación en la interfaz.
- `SecurityConfig.java`: se dejaron sin autenticación las rutas de Swagger mismas
  (`/swagger-ui/**`, `/v3/api-docs/**`) — si no, no se podría ni abrir la página para
  loguearse.

### 6.3 Dónde verlo

```
http://localhost:8080/swagger-ui/index.html   → interfaz interactiva
http://localhost:8080/v3/api-docs             → el JSON crudo (OpenAPI 3.0)
```

---

## 7. Base de datos

No se escribió ninguna migración a mano. Como `spring.jpa.hibernate.ddl-auto=update`,
Hibernate detecta el campo nuevo en `Tarifa.java` y agrega la columna
`recargo_festivo_porcentaje` a la tabla `tarifas` solo, al arrancar la aplicación.

---

## 8. Frontend

| Elemento | Qué hace |
|---|---|
| Banner naranja (tarjeta de identidad) | Si `GET /api/v1/festivos/hoy` dice que hoy es festivo, lo avisa con el nombre del festivo |
| Tabla de Tarifas | Columna nueva "Festivo" mostrando el porcentaje; el botón "Editar" ahora también pide ese valor |
| Monto Adeudado (en vivo) | La función `calcularCobroEnVivo` en `app.js` replica la misma fórmula del backend, incluyendo el recargo — para que lo que se ve en pantalla *antes* de pagar coincida con lo que realmente se cobra al salir |

---

## 9. Pruebas automatizadas

Se agregaron 3 pruebas nuevas en `CalculadoraCobroTest`, además de actualizar las 6
existentes al nuevo parámetro:

- `enDiaFestivoAplicaElRecargoDeLaTarifa` — verifica el 20% sobre un cobro normal
- `elRecargoDeFestivoTambienAplicaSobreElTopeDelDia` — verifica que el recargo
  también aplica cuando ya se tocó el tope diario
- `sinRecargoConfiguradoElFestivoNoCambiaElCobro` — si el tipo de vehículo tiene
  0% de recargo (como bicicleta), un día festivo no cambia nada

Total del proyecto: **27 pruebas**, todas en verde.

---

## 10. Cómo verificarlo manualmente

```bash
# 1. Login
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'

# 2. ¿Hoy es festivo?
curl http://localhost:8080/api/v1/festivos/hoy -H "Authorization: Bearer <token>"

# 3. Festivos de un año completo
curl http://localhost:8080/api/v1/festivos/2026 -H "Authorization: Bearer <token>"

# 4. Actualizar el recargo de una tarifa
curl -X PUT http://localhost:8080/api/v1/tarifas/CARRO \
  -H "Content-Type: application/json" -H "Authorization: Bearer <token>" \
  -d '{"valorMinuto":120,"topeDia":25000,"minutosGracia":5,"recargoFestivoPorcentaje":25}'
```

O, más cómodo: abrir `http://localhost:8080/swagger-ui/index.html`, dar clic en
**Authorize**, pegar el token, y probar cualquier endpoint desde ahí mismo.
