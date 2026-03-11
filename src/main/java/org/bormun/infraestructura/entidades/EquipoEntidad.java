package org.bormun.infraestructura.entidades;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "equipos")
public class EquipoEntidad {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombreEquipo;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "equipo_id")
    List<DeportistaEntidad> deportistas = new ArrayList<>();

    public List<DeportistaEntidad> getDeportistas() {
        return deportistas;
    }

    public void setDeportistas(List<DeportistaEntidad> deportistas) {
        this.deportistas = deportistas;
    }

    public Long getId() {
        return id;
    }

    public String getNombreEquipo() {
        return nombreEquipo;
    }

    public void setNombreEquipo(String nombreEquipo) {
        this.nombreEquipo = nombreEquipo;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
