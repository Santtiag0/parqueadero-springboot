package com.parqueadero.web;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.parqueadero.exception.CredencialesInvalidasException;
import com.parqueadero.model.Usuario;
import com.parqueadero.repository.UsuarioRepository;
import com.parqueadero.security.JwtService;
import com.parqueadero.web.dto.LoginRequest;
import com.parqueadero.web.dto.LoginResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Autenticacion", description = "Inicio de sesion. Entrega el token JWT que se pega en el boton Authorize")
public class AuthController {

    private final UsuarioRepository usuarios;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthController(UsuarioRepository usuarios, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.usuarios = usuarios;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @PostMapping("/login")
    @SecurityRequirements
    @Operation(summary = "Iniciar sesion",
            description = "Valida usuario y contrasena y devuelve un token JWT valido por 1 hora. "
                    + "Este endpoint es publico: no requiere token.")
    @ApiResponse(responseCode = "200", description = "Credenciales correctas, se entrega el token")
    @ApiResponse(responseCode = "400", description = "Faltan el usuario o la contrasena")
    @ApiResponse(responseCode = "401", description = "Usuario o contrasena incorrectos")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        Usuario usuario = usuarios.findByUsername(request.username())
                .orElseThrow(() -> new CredencialesInvalidasException("Usuario o contrasena incorrectos"));

        if (!passwordEncoder.matches(request.password(), usuario.getPasswordHash())) {
            throw new CredencialesInvalidasException("Usuario o contrasena incorrectos");
        }

        String token = jwtService.generarToken(usuario.getUsername());
        return new LoginResponse(token, usuario.getUsername());
    }
}
