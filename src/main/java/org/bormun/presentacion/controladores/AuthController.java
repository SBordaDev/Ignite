package org.bormun.presentacion.controladores;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.SchemaProperty;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.bormun.aplicacion.usecase.ConsultarUsuario;
import org.bormun.presentacion.dto.request.LoginRequestDTO;
import org.bormun.presentacion.dto.request.RegistroRequestDTO;
import org.bormun.presentacion.dto.response.TokenResponseDTO;
import org.bormun.dominio.modelos.Roles;
import org.bormun.infraestructura.entidades.UsuarioEntidad;
import org.bormun.aplicacion.repositorios.UsuarioRepository;
import org.bormun.infraestructura.seguridad.TokenService;
import org.bormun.presentacion.dto.response.UsuarioDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;
    private final ConsultarUsuario consultarUsuario;

    public AuthController(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder, TokenService tokenService, ConsultarUsuario consultarUsuario) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
        this.consultarUsuario = consultarUsuario;
    }

    @Operation(
            summary = "Registro de usuarios nuevos",
            description = "El endpoint permite a cualquier persona crear un usuario dentro del sistema con el rol de EQUIPO"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Autenticación exitosa",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = TokenResponseDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Credenciales incorrectas",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Error en los datos enviados",
                    content = @Content(
                            mediaType = "application/json",
                            schemaProperties = {
                                    @SchemaProperty(
                                            name = "detalles",
                                            schema = @Schema(
                                                    type = "object",
                                                    example = "{\"datoErroneo\": \"Descripcion del problema\"}"
                                            )
                                    ),
                                    @SchemaProperty(
                                            name = "error",
                                            schema = @Schema(
                                                    type = "string",
                                                    example = "Error en los datos enviados"
                                            )
                                    )
                            }
                    )
            )
    })
    @PostMapping("/registro")
    public ResponseEntity<?> registrarUsuario(@Valid @RequestBody RegistroRequestDTO dto) {

        // Verificamos si el correo ya existe
        if (usuarioRepository.findByEmail(dto.email()).isPresent()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "El correo ya está registrado"));
        }

        // 🔥 LA MAGIA DE BCRYPT: Encriptamos la contraseña antes de guardarla
        String contrasenaEncriptada = passwordEncoder.encode(dto.password());

        // Creamos y guardamos el usuario
        UsuarioEntidad nuevoUsuario = new UsuarioEntidad(dto.email(), contrasenaEncriptada, Roles.EQUIPO);
        usuarioRepository.save(nuevoUsuario);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("mensaje", "Usuario registrado exitosamente"));
    }

    @Operation(
            summary = "Login de usuarios",
            description = "El endpoint permite a usuarios autenticarse dentro del sistema, si el sistema reconoce al usuario delvolvera un token con el que podran ejecutar el resto de endpoints dentro de la aplicacion siempre y cuando su rol este autorizado"
    )
    @PostMapping("/login")
    public ResponseEntity<?> iniciarSesion(@Valid @RequestBody LoginRequestDTO dto) {

        // 1. Buscamos al usuario en la base de datos
        Optional<UsuarioEntidad> usuarioOpt = usuarioRepository.findByEmail(dto.email());

        // 2. Verificamos que exista y que la contraseña coincida
        // .matches() compara el texto plano (1234) con el hash indescifrable de la BD
        if (usuarioOpt.isEmpty() || !passwordEncoder.matches(dto.password(), usuarioOpt.get().getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Credenciales incorrectas"));
        }

        // 3. Si todo está bien, imprimimos su "Manilla VIP" (El Token JWT)
        UsuarioEntidad usuarioAutenticado = usuarioOpt.get();
        String token = tokenService.generarToken(usuarioAutenticado);

        // 4. Se la entregamos
        return ResponseEntity.ok(new TokenResponseDTO(token));
    }

    @Operation(
            summary = "Escalar privilegios de un Usuario",
            description = "Este endpoint solo puede ser ejectuado por un usuario ADMIN y permite cambiar el rol de un usuario dentro del sistema"
    )
    @PatchMapping("/usuarios/{idUsuario}/rol")
    public ResponseEntity<?> cambiarRolUsuario(
            @PathVariable Long idUsuario,
            @RequestParam Roles nuevoRol) {

        UsuarioEntidad usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        usuario.setRol(nuevoRol);
        usuarioRepository.save(usuario);

        return ResponseEntity.ok(Map.of("mensaje", "Rol actualizado exitosamente a " + nuevoRol));
    }

    @GetMapping("usuarios/{idUsuario}")
    public ResponseEntity<?> obtenerUsuario(
            @PathVariable Long idUsuario
    ){
        try {
            UsuarioEntidad entidad = consultarUsuario.buscarPorId(idUsuario);
            return ResponseEntity.ok(new UsuarioDTO(entidad.getId(), entidad.getEmail()));
        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Usuario no encontrado"));
        }

    }
}