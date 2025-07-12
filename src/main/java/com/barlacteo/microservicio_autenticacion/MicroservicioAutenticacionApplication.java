package com.barlacteo.microservicio_autenticacion;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
public class MicroservicioAutenticacionApplication {

	public static void main(String[] args) {
		SpringApplication.run(MicroservicioAutenticacionApplication.class, args);
	PasswordEncoder encoder = new BCryptPasswordEncoder();
	String passwordHasheada = encoder.encode("barlacteo");
	System.out.println(passwordHasheada);
	}


}
