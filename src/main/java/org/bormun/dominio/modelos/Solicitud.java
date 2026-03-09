package org.bormun.dominio.modelos;

import java.time.LocalDate;



public class Solicitud {
    private final String nombreOrganizacion;
    private final LocalDate fechaSolicitud;
    private boolean pagoConfirmado;
    private int precioTotal;
    private EstadoSolicitud estadoSolicitud;
    private String comentarios;
    private Equipo equipo;
    private Categoria categoria;

    public Solicitud(String nombreOrganizacion, Equipo equipo, Categoria categoria){
        this.nombreOrganizacion = nombreOrganizacion;
        this.fechaSolicitud = LocalDate.now();
        this.pagoConfirmado = false;
        this.precioTotal = 0;
        this.estadoSolicitud = EstadoSolicitud.EN_PROCESO;
        this.equipo = equipo;
        this.categoria = categoria;
    }

    public Equipo getEquipo() {
        return equipo;
    }

    public EstadoSolicitud getEstadoSolicitud() {
        return estadoSolicitud;
    }

    public int getPrecioTotal() {
        return precioTotal;
    }

    public Categoria getCategoria() {
        return categoria;
    }

    public String getComentarios() {
        return comentarios;
    }

    public void setPrecioTotal(int precioUnit) {
        this.precioTotal = precioUnit * this.equipo.getIntegrantes().size();
    }

    public void setEstadoSolicitud(EstadoSolicitud estadoSolicitud) {
        this.estadoSolicitud = estadoSolicitud;
    }

    public void setComentarios(String comentarios) {
        this.comentarios = comentarios;
    }
}
