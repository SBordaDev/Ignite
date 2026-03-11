package org.bormun.infraestructura.entidades;

import jakarta.persistence.*;
import org.bormun.dominio.modelos.GeneroNacimiento;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Entity
@Table(name = "deportistas")
public class DeportistaEntidad {
    @Id
    private String identificacion;

    private String nombre;

    @Enumerated(EnumType.STRING)
    private GeneroNacimiento generoNacimiento;

    @DateTimeFormat
    private LocalDate fechaNacimiento;

    public DeportistaEntidad(){}

    public String getIdentificacion() {
        return identificacion;
    }

    public String getNombre() {
        return nombre;
    }

    public GeneroNacimiento getGeneroNacimiento() {
        return generoNacimiento;
    }

    public LocalDate getFechaNacimiento() {
        return fechaNacimiento;
    }

    public void setFechaNacimiento(LocalDate fechaNacimiento) {
        this.fechaNacimiento = fechaNacimiento;
    }

    public void setGeneroNacimiento(GeneroNacimiento generoNacimiento) {
        this.generoNacimiento = generoNacimiento;
    }

    public void setIdentificacion(String identificacion) {
        this.identificacion = identificacion;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
}
