package org.bormun.infraestructura.entidades;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "categorias")
public class CategoriaEntidad {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombreCategoria;
    private int precioInscripcion;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "categoria_id")
    private List<EquipoEntidad> equipos = new ArrayList<>();

    @Embedded
    private RestriccionesEntidad restricciones;

    public Long getId() {
        return id;
    }

    public List<EquipoEntidad> getInscritos() {
        return equipos;
    }

    public String getNombreCategoria() {
        return nombreCategoria;
    }

    public int getPrecioInscripcion() {
        return precioInscripcion;
    }

    public RestriccionesEntidad getRestricciones() {
        return restricciones;
    }

    public void setInscritos(List<EquipoEntidad> inscritos) {
        this.equipos = inscritos;
    }

    public void setNombreCategoria(String nombreCategoria) {
        this.nombreCategoria = nombreCategoria;
    }

    public void setPrecioInscripcion(int precioInscripcion) {
        this.precioInscripcion = precioInscripcion;
    }

    public void setRestricciones(RestriccionesEntidad restricciones) {
        this.restricciones = restricciones;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
