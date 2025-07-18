package com.barlacteo.microservicio_autenticacion.Autenticacion;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.barlacteo.microservicio_autenticacion.Usuario.Rol;
import com.barlacteo.microservicio_autenticacion.Usuario.Usuario;
import com.barlacteo.microservicio_autenticacion.Usuario.UsuarioRepositorio;

@Service
public class UserDetailServiceImpl implements UserDetailsService{

    @Autowired
    private UsuarioRepositorio usuarioRepositorio;
    
    public UsuarioRepositorio getUsuarioRepositorio() {
    return usuarioRepositorio;
}

    
    @Override
    public UserDetails loadUserByUsername(String nombreUsuario) throws UsernameNotFoundException {
        Optional<Usuario> usuarioOpt = usuarioRepositorio.findByNombreUsuario(nombreUsuario);
        if (usuarioOpt.isEmpty()) {
            throw new UsernameNotFoundException("Usuario no encontrado: " + nombreUsuario);
        }

        Usuario usuario = usuarioOpt.get();
        return org.springframework.security.core.userdetails.User
                .builder()
                .username(usuario.getNombreUsuario())
                .password(usuario.getContrasenia())
                .roles(usuario.getRol().name())
                .build();
    }

}
