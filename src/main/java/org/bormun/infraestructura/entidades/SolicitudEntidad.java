package org.bormun.infraestructura.entidades;

import jakarta.persistence.*;
import org.bormun.dominio.modelos.EstadoSolicitud;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Entity
@Table(name = "solicitudes")
public class SolicitudEntidad {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombreOrganizacion;

    @DateTimeFormat
    private LocalDate fechaSolicitud;

    private Boolean pagoConfirmado;

    private int precioTotal;

    @Enumerated(EnumType.STRING)
    private EstadoSolicitud estadoSolicitud;

    private String comentarios;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "equipo_id")
    private EquipoEntidad equipo;

    @ManyToOne
    @JoinColumn(name = "categoria_id")
    private CategoriaEntidad categoria;

    public String getComentarios() {
        return comentarios;
    }

    public void setComentarios(String comentarios) {
        this.comentarios = comentarios;
    }

    public EquipoEntidad getEquipo() {
        return equipo;
    }

    public void setEquipo(EquipoEntidad equipos) {
        this.equipo = equipos;
    }

    public EstadoSolicitud getEstadoSolicitud() {
        return estadoSolicitud;
    }

    public void setEstadoSolicitud(EstadoSolicitud estadoSolicitud) {
        this.estadoSolicitud = estadoSolicitud;
    }

    public LocalDate getFechaSolicitud() {
        return fechaSolicitud;
    }

    public void setFechaSolicitud(LocalDate fechaSolicitud) {
        this.fechaSolicitud = fechaSolicitud;
    }

    public Long getId() {
        return id;
    }

    public String getNombreOrganizacion() {
        return nombreOrganizacion;
    }

    public void setNombreOrganizacion(String nombreOrganizacion) {
        this.nombreOrganizacion = nombreOrganizacion;
    }

    public Boolean getPagoConfirmado() {
        return pagoConfirmado;
    }

    public void setPagoConfirmado(Boolean pagoConfirmado) {
        this.pagoConfirmado = pagoConfirmado;
    }

    public int getPrecioTotal() {
        return precioTotal;
    }

    public void setPrecioTotal(int precioTotal) {
        this.precioTotal = precioTotal;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public CategoriaEntidad getCategoria() {
        return categoria;
    }

    public void setCategoria(CategoriaEntidad categoria) {
        this.categoria = categoria;
    }
}
