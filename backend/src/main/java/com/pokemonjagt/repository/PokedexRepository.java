package com.pokemonjagt.repository;

import com.pokemonjagt.entity.Pokedex;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PokedexRepository extends JpaRepository<Pokedex, Integer> {

    List<Pokedex> findAllByOrderByIdAsc();
}
