package org.bormun.dominio.modelos;

import java.time.LocalDate;
import java.time.Period;

public class Deportista{
    private final DatosDeportista datosDeportista;

    public Deportista(DatosDeportista datos){
        this.datosDeportista = datos;
    }

    public int getEdad(){
        LocalDate nacimiento = datosDeportista.fechaNacimiento();
        LocalDate hoy = LocalDate.now();

        return Period.between(nacimiento, hoy).getYears();
    }

    public String getNombre(){
        return datosDeportista.nombre();
    }

    public String getId(){
        return datosDeportista.identificacion();
    }

    public GeneroNacimiento getGenero(){
        return datosDeportista.generoNacimiento();
    }
}
