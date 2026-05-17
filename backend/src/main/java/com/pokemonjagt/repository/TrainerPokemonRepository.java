package com.pokemonjagt.repository;

import com.pokemonjagt.entity.TrainerPokemon;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TrainerPokemonRepository extends JpaRepository<TrainerPokemon, UUID> {

    List<TrainerPokemon> findByTrainerUserIdOrderByCaughtAtDesc(UUID userId);
}
