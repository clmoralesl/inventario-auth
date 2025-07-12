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
import com.barlacteo.microservicio_autenticacion.Usuario.UsuarioRepositorio;

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
        // Obtiene el nombre de usuario autenticado desde el contexto de seguridad
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
}