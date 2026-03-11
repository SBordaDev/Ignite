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

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "evento_id")
    private List<SolicitudEntidad> solicitudes = new ArrayList<>();

    public EventoEntidad(){}

    public Long getId() { return id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public boolean isInscripcionAbierta() { return inscripcionAbierta; }
    public void setInscripcionAbierta(boolean inscripcionAbierta) { this.inscripcionAbierta = inscripcionAbierta; }
    public List<CategoriaEntidad> getCategorias() { return categorias; }
    public void setCategorias(List<CategoriaEntidad> categorias) { this.categorias = categorias; }
    public List<SolicitudEntidad> getSolicitudes() {
        return solicitudes;
    }
    public void setSolicitudes(List<SolicitudEntidad> solicitudes) {
        this.solicitudes = solicitudes;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
