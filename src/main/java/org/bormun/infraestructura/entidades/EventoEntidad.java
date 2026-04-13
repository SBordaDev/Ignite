package org.bormun.infraestructura.entidades;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "eventos")
public class EventoEntidad {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;
    private boolean inscripcionAbierta;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "evento_id")
    private List<CategoriaEntidad> categorias = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creador_id", nullable = false)
    private UsuarioEntidad creador;

    // No olvides agregar el Getter y el Setter
    public UsuarioEntidad getCreador() { return creador; }
    public void setCreador(UsuarioEntidad creador) { this.creador = creador; }

    public EventoEntidad(){}

    public Long getId() { return id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public boolean isInscripcionAbierta() { return inscripcionAbierta; }
    public void setInscripcionAbierta(boolean inscripcionAbierta) { this.inscripcionAbierta = inscripcionAbierta; }
    public List<CategoriaEntidad> getCategorias() { return categorias; }
    public void setCategorias(List<CategoriaEntidad> categorias) { this.categorias = categorias; }

    public void setId(Long id) {
        this.id = id;
    }
}
