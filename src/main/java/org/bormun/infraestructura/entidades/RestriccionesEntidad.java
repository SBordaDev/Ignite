package org.bormun.infraestructura.entidades;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import org.bormun.dominio.modelos.GeneroNacimiento;
import org.springframework.lang.NonNull;

@Embeddable
public class RestriccionesEntidad {
    private int edadMinima;

    private int edadMaxima;

    @Enumerated(EnumType.STRING)
    private GeneroNacimiento generoNacimiento;

    private int numeroEquipo;

    private int numeroIntegrantesPorEquipo;

    public RestriccionesEntidad(){}

    public int getEdadMinima() {
        return edadMinima;
    }

    public int getEdadMaxima() {
        return edadMaxima;
    }

    public int getNumeroEquipo() {
        return numeroEquipo;
    }

    public int getNumeroIntegrantesPorEquipo() {
        return numeroIntegrantesPorEquipo;
    }

    public GeneroNacimiento getGeneroNacimiento() {
        return generoNacimiento;
    }

    public void setEdadMaxima(int edadMaxima) {
        this.edadMaxima = edadMaxima;
    }

    public void setEdadMinima(int edadMinima) {
        this.edadMinima = edadMinima;
    }

    public void setGeneroNacimiento(GeneroNacimiento generoNacimiento) {
        this.generoNacimiento = generoNacimiento;
    }

    public void setNumeroEquipo(int numeroEquipo) {
        this.numeroEquipo = numeroEquipo;
    }

    public void setNumeroIntegrantesPorEquipo(int numeroIntegrantesPorEquipo) {
        this.numeroIntegrantesPorEquipo = numeroIntegrantesPorEquipo;
    }
}
