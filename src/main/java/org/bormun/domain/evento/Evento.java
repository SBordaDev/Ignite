package org.bormun.domain.evento;

import org.bormun.domain.solicitud.Solicitud;
import org.bormun.domain.categoria.Categoria;
import org.bormun.domain.categoria.Restricciones;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

//TODO: agregar estados al evento
public class Evento {
    private List<Categoria> categorias;
    private List<Solicitud> solicitudes;
    private Enum estado;


    public Evento(){
        this.categorias = new ArrayList<>();
        this.solicitudes = new ArrayList<>();
    }

    public void agregarCategoria(String nombreCategoria, int precioInscripcion, Restricciones restricciones){
        categorias.add(new Categoria(nombreCategoria, precioInscripcion, restricciones));
    }

    public void agregarSolicitud(Solicitud solicitud){
        solicitudes.add(solicitud);
    }

    public List<Categoria> getCategorias() {
        return categorias;
    }

    public List<Solicitud> getSolicitudes() {
        return solicitudes;
    }
}
