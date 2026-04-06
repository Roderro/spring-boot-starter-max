package ru.maxbot.examples.security;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

@SpringBootApplication
@EnableMethodSecurity
public class SecurityExampleApplication {

    public static void main(String[] args) {
        SpringApplication.run(SecurityExampleApplication.class, args);
    }
}

