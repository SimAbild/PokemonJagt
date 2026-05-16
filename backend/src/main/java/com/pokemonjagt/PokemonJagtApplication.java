package com.pokemonjagt;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// Entry point — Spring Boot scans this package and wires everything together automatically
@SpringBootApplication
public class PokemonJagtApplication {

    public static void main(String[] args) {
        SpringApplication.run(PokemonJagtApplication.class, args);
    }
}
