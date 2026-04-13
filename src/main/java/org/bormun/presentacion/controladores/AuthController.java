package org.bormun.presentacion.controladores;

import jakarta.validation.Valid;
import org.bormun.presentacion.dto.request.LoginRequestDTO;
import org.bormun.presentacion.dto.request.RegistroRequestDTO;
import org.bormun.presentacion.dto.response.TokenResponseDTO;
import org.bormun.dominio.modelos.Roles;
import org.bormun.infraestructura.entidades.UsuarioEntidad;
import org.bormun.aplicacion.repositorios.UsuarioRepository;
import org.bormun.infraestructura.seguridad.TokenService;
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

    public AuthController(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder, TokenService tokenService) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
    }

    // --- 1. REGISTRO DE USUARIOS ---
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

    // --- 2. INICIO DE SESIÓN (LOGIN) ---
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
}