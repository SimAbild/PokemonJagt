package com.pokemonjagt.controller;

import com.pokemonjagt.service.PokedexService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * Provides access to the Pokedex — the master catalogue of all Pokemon.
 *
 * This endpoint requires a valid JWT (configured in WebConfig),
 * so only authenticated trainers can browse the Pokedex.
 * The Pokedex itself is read-only — trainers catch Pokemon from it,
 * but cannot add or remove entries.
 */
@RestController
@RequestMapping("/api/pokedex")
public class PokedexController {

    private final PokedexService pokedexService;

    public PokedexController(PokedexService pokedexService) {
        this.pokedexService = pokedexService;
    }

    /**
     * GET /api/pokedex
     *
     * Returns all Pokemon in the Pokedex, sorted by national Pokedex number.
     * Each entry includes the sprite URL so the frontend can display the image.
     */
    @GetMapping
    public List<Map<String, Object>> getAllAvailablePokemon() {
        return pokedexService.getAllAvailablePokemon();
    }
}
