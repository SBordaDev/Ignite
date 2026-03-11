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
    private EquipoEntidad equipos;

    @ManyToOne
    @JoinColumn(name = "categoria_id")
    private CategoriaEntidad categorias;

    public CategoriaEntidad getCategorias() {
        return categorias;
    }

    public void setCategorias(CategoriaEntidad categorias) {
        this.categorias = categorias;
    }

    public String getComentarios() {
        return comentarios;
    }

    public void setComentarios(String comentarios) {
        this.comentarios = comentarios;
    }

    public EquipoEntidad getEquipos() {
        return equipos;
    }

    public void setEquipos(EquipoEntidad equipos) {
        this.equipos = equipos;
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
}
