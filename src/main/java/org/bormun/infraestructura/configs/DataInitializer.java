package org.bormun.infraestructura.configs;

import org.bormun.dominio.modelos.Roles;
import org.bormun.infraestructura.entidades.UsuarioEntidad;
import org.bormun.aplicacion.repositorios.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        String correoAdmin = "admin@gmail.com";

        if (usuarioRepository.findByEmail(correoAdmin).isEmpty()) {
            UsuarioEntidad superAdmin = new UsuarioEntidad(
                    correoAdmin,
                    passwordEncoder.encode("@Ignite°10"),
                    Roles.ADMIN
            );
            usuarioRepository.save(superAdmin);
            System.out.println(LocalDateTime.now()+" 🛡️ Super Admin creado automáticamente: " + correoAdmin);
        }
    }
}