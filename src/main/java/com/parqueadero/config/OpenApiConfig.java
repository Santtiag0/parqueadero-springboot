package com.parqueadero.config;

import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;

/**
 * Documentacion automatica de toda la API en /swagger-ui/index.html.
 * El boton "Authorize" permite pegar el token que entrega /api/v1/auth/login
 * para poder probar los demas endpoints protegidos directamente desde ahi.
 */
@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Parqueadero API",
                version = "1.0",
                description = "API REST del sistema de gestion de parqueadero: entradas, salidas, tarifas, "
                        + "mensualidades, cierre de caja y recargo por dia festivo."
        ),
        security = @SecurityRequirement(name = "bearerAuth")
)
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        in = SecuritySchemeIn.HEADER
)
public class OpenApiConfig {
}
