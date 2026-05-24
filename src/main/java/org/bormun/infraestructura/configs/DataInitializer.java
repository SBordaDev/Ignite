package org.bormun.infraestructura.configs;

import org.bormun.dominio.modelos.Roles;
import org.bormun.infraestructura.entidades.UsuarioEntidad;
import org.bormun.aplicacion.repositorios.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;

import java.time.LocalDateTime;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.email}")
    private String adminEmail;

    @Value("${app.admin.password}")
    private String adminPassword;

    public DataInitializer(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        if (!usuarioRepository.existsByEmail(adminEmail)) {
            UsuarioEntidad superAdmin = new UsuarioEntidad(
                    adminEmail,
                    passwordEncoder.encode(adminPassword),
                    Roles.ADMIN
            );
            usuarioRepository.save(superAdmin);
            System.out.println(LocalDateTime.now()+"Super Admin creado automáticamente: " + adminEmail);
        }
    }
}