package org.bormun.dominio.modelos;

import java.time.LocalDate;



public class Solicitud {
    private Long id;
    private final String nombreOrganizacion;
    private LocalDate fechaSolicitud;
    private boolean pagoConfirmado;
    private int precioTotal;
    private EstadoSolicitud estadoSolicitud;
    private String comentarios;
    private final Equipo equipo;
    private final Categoria categoria;

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

    public void actualizarPrecioTotal(int precioUnit) {
        this.precioTotal = precioUnit * this.equipo.getIntegrantes().size();
    }

    public void setEstadoSolicitud(EstadoSolicitud estadoSolicitud) {
        this.estadoSolicitud = estadoSolicitud;
    }

    public void setComentarios(String comentarios) {
        this.comentarios = comentarios;
    }

    public LocalDate getFechaSolicitud() {
        return fechaSolicitud;
    }

    public String getNombreOrganizacion() {
        return nombreOrganizacion;
    }

    public boolean isPagoConfirmado() {
        return pagoConfirmado;
    }

    public void setPagoConfirmado(boolean pagoConfirmado) {
        this.pagoConfirmado = pagoConfirmado;
    }

    public void setPrecioTotal(int precioTotal) {
        this.precioTotal = precioTotal;
    }

    public void setFechaSolicitud(LocalDate fechaSolicitud) {
        this.fechaSolicitud = fechaSolicitud;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
