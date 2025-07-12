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
        System.out.println("Buscando usuario: " + nombreUsuario);

        usuarioRepositorio.findAll().forEach(u -> System.out.println("Usuario en BD: " + u.getNombreUsuario()));

        Optional<Usuario> usuarioOpt = usuarioRepositorio.findByNombreUsuario(nombreUsuario);
        if (usuarioOpt.isEmpty()) {
            System.out.println("Usuario no encontrado en BD: " + nombreUsuario);
            throw new UsernameNotFoundException("Usuario no encontrado: " + nombreUsuario);
        }

        Usuario usuario = usuarioOpt.get();
        System.out.println("Usuario encontrado: " + usuario.getNombreUsuario());
        System.out.println("Password en BD: " + usuario.getContrasenia());
        System.out.println("Password ingresado: barlacteo");

        return org.springframework.security.core.userdetails.User
                .builder()
                .username(usuario.getNombreUsuario())
                .password(usuario.getContrasenia())
                .roles(usuario.getRol().name())
                .build();
    }

}
