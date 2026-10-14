package com.parqueadero;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.parqueadero.model.Usuario;
import com.parqueadero.repository.UsuarioRepository;

@SpringBootApplication
public class ParqueaderoApplication {

    public static void main(String[] args) {
        SpringApplication.run(ParqueaderoApplication.class, args);
    }

    /**
     * Crea un usuario de ejemplo al arrancar, si todavia no existe, para poder
     * iniciar sesion sin tener que registrar uno a mano.
     */
    @Bean
    public CommandLineRunner crearUsuarioDeEjemplo(UsuarioRepository usuarios, PasswordEncoder passwordEncoder) {
        return args -> {
            if (usuarios.findByUsername("admin").isEmpty()) {
                usuarios.save(new Usuario("admin", passwordEncoder.encode("admin123")));
            }
        };
    }
}
