package org.bormun.dominio.modelos;

import java.util.ArrayList;
import java.util.List;

//TODO: agregar estados al evento
public class Evento {
    private Long id;
    private String nombre;
    private List<Categoria> categorias;
    private List<Solicitud> solicitudes;
    private boolean inscripcionAbierta;


    public Evento(String nombre){
        this.nombre = nombre;
        this.categorias = new ArrayList<>();
        this.solicitudes = new ArrayList<>();
        this.inscripcionAbierta = true;
    }

    public void agregarCategoria(String nombreCategoria, int precioInscripcion, Restricciones restricciones){
        categorias.add(new Categoria(nombreCategoria, precioInscripcion, restricciones));
    }

    public String getNombre() {
        return nombre;
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

    public void setCategorias(List<Categoria> categorias) {
        this.categorias = categorias;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
