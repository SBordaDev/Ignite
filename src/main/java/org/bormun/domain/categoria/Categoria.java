package org.bormun.domain.categoria;

import org.bormun.domain.solicitud.SolicitudInvalidaException;
import org.bormun.domain.evento.GeneroNacimiento;
import org.bormun.domain.participante.Deportista;
import org.bormun.domain.participante.Equipo;

import java.util.ArrayList;
import java.util.List;

public class Categoria {
    private final String nombreCategoria;
    private final int precioInscripcion;
    private List<Equipo> inscritos;
    private final Restricciones restricciones;

    public Categoria(String nombreCategoria, int precioInscripcion, Restricciones restricciones){
        this.nombreCategoria = nombreCategoria;
        this.precioInscripcion = precioInscripcion;
        this.inscritos = new ArrayList<>();
        this.restricciones = restricciones;
    }

    //TODO: MEJORAR EL ERROR PARA SABER CUALES DEPORTISTAS LO GENERAN
    public void verificarDeportista(Deportista deportista){
        int edad = deportista.getEdad();
        GeneroNacimiento genero = deportista.getGenero();

            int edadMaxima = restricciones.edadMaxima();
            int edadMinima = restricciones.edadMinima();
            GeneroNacimiento generoCategoria = restricciones.generoNacimiento();

        if (edad > edadMaxima || edad < edadMinima){
            throw new SolicitudInvalidaException("Deportista fuera del rango de edad");
        }
        if (genero != generoCategoria){
            throw new SolicitudInvalidaException("Deportista no cumple con el genero de la categoria");
        }
    }

    public void verificarEquipo(Equipo equipo){
        List<Deportista> deportistas = equipo.getIntegrantes();
        int integrantesMaximos = restricciones.numeroIntegrantesPorEquipo();
        int equiposMaximos = restricciones.numeroEquipo();

        if(deportistas.size() > integrantesMaximos){
            throw new SolicitudInvalidaException("El Equipo es demasiado grande para el torneo");
        }
        if(equiposMaximos == inscritos.size()){
            throw new SolicitudInvalidaException("Ya no se pueden inscribir mas equipos a esta categoria");
        }

    }

    public void agregarEquipo(Equipo equipo){
        inscritos.add(equipo);
    }

    public List<Equipo> getInscritos() {
        return inscritos;
    }

    public String getNombreCategoria() {
        return nombreCategoria;
    }

    public int getPrecioInscripcion() {
        return precioInscripcion;
    }
}
