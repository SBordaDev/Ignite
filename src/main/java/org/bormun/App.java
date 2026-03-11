package org.bormun;

import org.bormun.dominio.modelos.*;
import org.bormun.dominio.repositorios.EventoRepository;
import org.bormun.infraestructura.entidades.EventoEntidad;
import org.bormun.infraestructura.mapper.EventoMapper;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDate;
import java.util.Optional;

@SpringBootApplication
public class App {

    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }

    @Bean
    public CommandLineRunner probarRelaciones(EventoRepository eventoRepo, TransactionTemplate transactionTemplate) {
        return args -> {

            transactionTemplate.execute(status -> {
                System.out.println("--- INICIANDO PRUEBA DE BASE DE DATOS IGNITE ---");
                //Evento evento = new Evento("Evento test IGNITE");
                //evento.agregarCategoria("CAT_MASCULINA", 5000, new Restricciones(10, 50, GeneroNacimiento.HOMBRE, 4, 2));
                //evento.agregarCategoria("CAT_FEMENUNA", 2500, new Restricciones(10, 50, GeneroNacimiento.MUJER, 4, 2));

                //eventoRepo.save(EventoMapper.aEntidad(evento));

                Optional<EventoEntidad> resultado = eventoRepo.findById(1L);

                if (resultado.isPresent()) {
                    EventoEntidad entidadEncontrada = resultado.get();

                    Evento miEvento = EventoMapper.aDominio(entidadEncontrada);

                    Equipo equipo = new Equipo("Equipo 1 test");
                    equipo.agregarIntegrante(new DatosDeportista("Maria", "345", GeneroNacimiento.MUJER, LocalDate.of(2006, 1, 1)));
                    equipo.agregarIntegrante(new DatosDeportista("Juliana", "123", GeneroNacimiento.MUJER, LocalDate.of(2006,2,1)));

                    Equipo equipo2 = new Equipo("Equipo 2 test");
                    equipo2.agregarIntegrante(new DatosDeportista("Ana", "678", GeneroNacimiento.MUJER, LocalDate.of(2006, 1, 1)));
                    equipo2.agregarIntegrante(new DatosDeportista("Sara", "901", GeneroNacimiento.MUJER, LocalDate.of(2006,2,1)));

                    Solicitud solicitud = new Solicitud("ALL MIGHTY", equipo, miEvento.getCategorias().get(1));
                    Solicitud solicitud2 = new Solicitud("IGNITE PRO TEAM", equipo2, miEvento.getCategorias().get(1));

                    miEvento.agregarSolicitud(solicitud);
                    miEvento.agregarSolicitud(solicitud2);

                    eventoRepo.save(EventoMapper.aEntidad(miEvento));
                } else {
                    System.out.println("El evento no existe en la base de datos.");
                }

                System.out.println("--- PRUEBA TERMINADA: Ve a tu consola H2 ---");

                return null;
            });

        };
    }
}
