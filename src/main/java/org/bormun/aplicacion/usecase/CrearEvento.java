package org.bormun.aplicacion.usecase;

import org.bormun.dominio.modelos.Evento;
import org.bormun.aplicacion.repositorios.EventoRepository; // Asumiendo que esta es tu interfaz
import org.bormun.infraestructura.entidades.EventoEntidad;
import org.bormun.infraestructura.entidades.UsuarioEntidad;
import org.bormun.aplicacion.mapper.EventoMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CrearEvento {

    private final EventoRepository eventoRepository;

    // Inyección de dependencias mediante constructor
    public CrearEvento(EventoRepository eventoRepository) {
        this.eventoRepository = eventoRepository;
    }

    /**
     * Recibe un Evento de dominio puro (ya validado por el controlador/DTO)
     * y coordina su persistencia en la base de datos.
     */
    @Transactional
    public Evento crearEvento(Evento eventoNuevo, UsuarioEntidad creador) {

        // 1. Reglas de Negocio Centrales (Opcional)
        // Como las validaciones de edades y cupos ya están en la clase Restricciones,
        // aquí podrías agregar reglas a nivel global del evento.
        // Ejemplo ficticio: if (eventoRepository.existsByNombre(eventoNuevo.getNombre())) throw new EventoDuplicadoException();
        if (eventoNuevo.getNombre() == null || eventoNuevo.getNombre().trim().isEmpty()) {
            throw new IllegalArgumentException("El evento no puede crearse sin un nombre válido.");
        }

        if (eventoNuevo.getCategorias() == null || eventoNuevo.getCategorias().isEmpty()) {
            throw new IllegalArgumentException("El evento debe tener al menos una categoría para ser creado.");
        }

        if(eventoRepository.existsByNombreIgnoreCase(eventoNuevo.getNombre().trim())){
            throw new IllegalArgumentException("El nombre del evento ya existe");
        }




        // 2. Traducción hacia el Adaptador de Salida (Base de Datos)
        // El Mapper se encarga de desarmar el Evento y sus Categorías en Entidades
        EventoEntidad entidadParaGuardar = EventoMapper.aEntidad(eventoNuevo);
        entidadParaGuardar.setCreador(creador);

        // 3. Persistencia
        // Gracias a la Cascada (CascadeType.ALL) en tu EventoEntidad,
        // este único save() guardará el evento y todas sus categorías en un solo paso.
        EventoEntidad entidadGuardada = eventoRepository.save(entidadParaGuardar);

        // 4. Retorno al Controlador
        // Traducimos de vuelta a Dominio para que el Controlador obtenga el Evento con su ID real generado por la BD
        return EventoMapper.aDominio(entidadGuardada);
    }
}