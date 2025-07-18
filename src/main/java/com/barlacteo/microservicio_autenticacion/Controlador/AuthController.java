package com.barlacteo.microservicio_autenticacion.Controlador;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import com.barlacteo.microservicio_autenticacion.Autenticacion.JwtUtil;
import com.barlacteo.microservicio_autenticacion.Autenticacion.UserDetailServiceImpl;
import com.barlacteo.microservicio_autenticacion.Usuario.Rol;
import com.barlacteo.microservicio_autenticacion.Usuario.Usuario;
import com.barlacteo.microservicio_autenticacion.Usuario.UsuarioRepositorio;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserDetailServiceImpl userDetailServiceImpl;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UsuarioRepositorio usuarioRepositorio;

    @Autowired
    private PasswordEncoder passwordEncoder;


    @PostMapping("/login")
    public ResponseEntity<?> createAuthenticationToken(@RequestBody AuthRequest authRequest) throws Exception {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(authRequest.getUsername(), authRequest.getPassword())
            );
        } catch (Exception e) {
            throw new Exception("Incorrect username or password", e);
        }

        final UserDetails userDetails = userDetailServiceImpl.loadUserByUsername(authRequest.getUsername());
        final String jwt = jwtUtil.generateToken(userDetails.getUsername(), userDetails.getAuthorities().iterator().next().getAuthority());

        return ResponseEntity.ok(new AuthResponse(jwt));
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser() {
        String username = org.springframework.security.core.context.SecurityContextHolder
                .getContext().getAuthentication().getName();

        // Busca el usuario en la base de datos
        var usuarioOpt = userDetailServiceImpl.getUsuarioRepositorio().findByNombreUsuario(username);
        if (usuarioOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        var usuario = usuarioOpt.get();
        Map<String, Object> response = new HashMap<>();
        response.put("id", usuario.getIdUsuario());
        response.put("nombre_usuario", usuario.getNombreUsuario());
        response.put("email", usuario.getEmail());
        response.put("rol", usuario.getRol().name());        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/registrar")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterRequest request, org.springframework.validation.BindingResult result) {
        // Captura errores de validación
        if (result.hasErrors()) {
            Map<String, String> errores = new HashMap<>();
            result.getFieldErrors().forEach(error ->
                errores.put(error.getField(), error.getDefaultMessage()));
            return ResponseEntity.badRequest().body(errores);
        }

        // Validaciones de negocio
        if (usuarioRepositorio.findByNombreUsuario(request.getUsername()).isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("error", "El nombre de usuario ya existe"));
        }
        if (usuarioRepositorio.findByEmail(request.getEmail()).isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("error", "El email ya está registrado"));
        }

        Usuario usuario = new Usuario();
        usuario.setNombreUsuario(request.getUsername());
        usuario.setContrasenia(passwordEncoder.encode(request.getPassword()));
        usuario.setEmail(request.getEmail());
        usuario.setRol(request.getRol());
        usuarioRepositorio.save(usuario);

        Map<String, Object> response = new HashMap<>();
        response.put("id", usuario.getIdUsuario());
        response.put("nombre_usuario", usuario.getNombreUsuario());
        response.put("email", usuario.getEmail());
        return ResponseEntity.ok(response);
    }

    // Clase para la solicitud de autenticación
    public static class AuthRequest {
        private String username;
        private String password;

        // Getters y setters
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }

    // Clase para la respuesta de autenticación
    public static class AuthResponse {
        private final String jwt;

        public AuthResponse(String jwt) {
            this.jwt = jwt;
        }

        public String getJwt() {
            return jwt;
        }
    }

    // Clase para la solicitud de registro
    public static class RegisterRequest {
        @NotBlank(message = "El nombre de usuario es obligatorio")
        private String username;

        @NotBlank(message = "El email es obligatorio")
        @Email(message = "El formato del correo electrónico no es válido")
        private String email;

        @NotBlank(message = "La contraseña es obligatoria")
        @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres")
        private String password;

        private Rol rol;

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public Rol getRol() { return rol; }
        public void setRol(Rol rol) { this.rol = rol; }
    }
}