package org.bormun.dominio.modelos;

import java.util.ArrayList;
import java.util.List;

public class Equipo {
    private final String nombreEquipo;
    private List<Deportista> integrantes = new ArrayList<>();

    public Equipo(String nombreEquipo){
        this.nombreEquipo = nombreEquipo;
    }

    public List<Deportista> getIntegrantes() {
        return integrantes;
    }


    public String getNombreEquipo() {
        return this.nombreEquipo;
    }

    public void agregarIntegrante(DatosDeportista datos){
        Deportista x = new Deportista(datos);
        integrantes.add(x);

    }

    public void agregarIntegrante(List<DatosDeportista> datos){
        for(DatosDeportista dato : datos){
            agregarIntegrante(dato);
        }
    }

}
