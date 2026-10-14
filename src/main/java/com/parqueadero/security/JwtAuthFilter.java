package com.parqueadero.security;

import java.io.IOException;
import java.util.List;

import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Se ejecuta en cada peticion: si viene un header "Authorization: Bearer <token>"
 * valido, marca al usuario como autenticado para el resto de la peticion.
 * Si no viene token o es invalido, simplemente sigue sin autenticar (Spring
 * Security es quien decide despues si esa ruta necesita autenticacion o no).
 */
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    public JwtAuthFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                     @NonNull HttpServletResponse response,
                                     @NonNull FilterChain filterChain) throws IOException, ServletException {
        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring("Bearer ".length());
            try {
                String username = jwtService.extraerUsername(token);
                var autenticacion = new UsernamePasswordAuthenticationToken(username, null, List.of());
                SecurityContextHolder.getContext().setAuthentication(autenticacion);
            } catch (Exception ex) {
                // Token invalido o expirado: no autenticamos, la peticion sigue como anonima.
            }
        }

        filterChain.doFilter(request, response);
    }
}
