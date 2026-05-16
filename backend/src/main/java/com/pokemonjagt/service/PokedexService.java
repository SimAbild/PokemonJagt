package com.pokemonjagt.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Handles database reads for the Pokedex — the master list of all Pokemon.
 *
 * The Pokedex is read-only for trainers. Pokemon are added here by administrators
 * (directly in Supabase), not through the app. Every authenticated trainer
 * can browse the full Pokedex and catch any Pokemon they see.
 */
@Service
public class PokedexService {

    private final JdbcTemplate database;

    public PokedexService(JdbcTemplate database) {
        this.database = database;
    }

    /**
     * Returns every Pokemon in the Pokedex, sorted by their national Pokedex number.
     * Each entry includes the sprite URL so the frontend can display the image.
     */
    public List<Map<String, Object>> getAllAvailablePokemon() {
        return database.queryForList(
                "SELECT id, name, type, sprite_url FROM pokedex ORDER BY id"
        );
    }
}
