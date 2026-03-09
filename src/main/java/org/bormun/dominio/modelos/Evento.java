package org.bormun.dominio.modelos;

import java.util.ArrayList;
import java.util.List;

//TODO: agregar estados al evento
public class Evento {
    private List<Categoria> categorias;
    private List<Solicitud> solicitudes;
    private boolean inscripcionAbierta;


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

    public boolean isInscripcionAbierta() {
        return inscripcionAbierta;
    }

    public void setInscripcionAbierta(boolean inscripcionAbierta) {
        this.inscripcionAbierta = inscripcionAbierta;
    }
}
