package org.bormun.infraestructura.entidades;

import jakarta.persistence.*;
import org.bormun.dominio.modelos.Roles;

@Entity
@Table(name = "usuarios")
public class UsuarioEntidad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Roles rol;

    // Constructores
    public UsuarioEntidad() {}

    public UsuarioEntidad(String email, String password, Roles rol) {
        this.email = email;
        this.password = password;
        this.rol = rol;
    }

    // Getters y Setters (Añádelos aquí)
    public Long getId() { return id; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public Roles getRol() { return rol; }

    public void setPassword(String password) { this.password = password; }
}