package com.pokemonjagt.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Handles all database reads and writes for trainer profiles and Pokemon teams.
 *
 * Every method takes a userId (from the validated JWT) and scopes its query
 * to that user only. This ensures a trainer can never see or modify another
 * trainer's data — even if they somehow know the other trainer's ID.
 */
@Service
public class TrainerService {

    // JdbcTemplate is Spring's wrapper around JDBC — it handles connection
    // management and maps query results to Java maps automatically
    private final JdbcTemplate database;

    public TrainerService(JdbcTemplate database) {
        this.database = database;
    }

    /**
     * Finds the trainer profile that belongs to the given Supabase user ID.
     *
     * Returns Optional.empty() when the user has logged in for the first time
     * and has not yet created their trainer profile.
     */
    public Optional<Map<String, Object>> findTrainerByUserId(String userId) {
        List<Map<String, Object>> results = database.queryForList(
                "SELECT id, name, gender, created_at FROM trainers WHERE user_id = ?::uuid",
                userId
        );

        // queryForList returns an empty list (not null) if nothing is found
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    /**
     * Creates a new trainer profile and links it to the given Supabase user ID.
     *
     * The database enforces that each user can only have one trainer
     * (via the UNIQUE constraint on user_id in the trainers table).
     */
    public Map<String, Object> createTrainerProfile(String userId, String trainerName, String gender) {
        database.update(
                "INSERT INTO trainers (user_id, name, gender) VALUES (?::uuid, ?, ?)",
                userId, trainerName, gender
        );

        // Re-query to return the full created record including the generated ID and timestamp
        return findTrainerByUserId(userId).orElseThrow();
    }

    /**
     * Returns all Pokemon currently on the trainer's team.
     *
     * Joins with the pokedex table so each result includes the Pokemon's
     * name, type, and sprite URL — everything the frontend needs to display a card.
     */
    public List<Map<String, Object>> getTrainerTeam(String userId) {
        String teamQuery = """
                SELECT
                    tp.id          AS caught_pokemon_id,
                    tp.nickname,
                    tp.level,
                    tp.caught_at,
                    p.name         AS pokemon_name,
                    p.type         AS pokemon_type,
                    p.sprite_url
                FROM trainer_pokemon tp
                JOIN trainers t ON t.id = tp.trainer_id
                JOIN pokedex  p ON p.id = tp.pokedex_id
                WHERE t.user_id = ?::uuid
                ORDER BY tp.caught_at DESC
                """;

        return database.queryForList(teamQuery, userId);
    }

    /**
     * Adds a Pokemon from the Pokedex to the trainer's team.
     *
     * Uses a subquery to find the trainer's ID from the user ID,
     * so the frontend only needs to send the Pokedex ID — not any internal IDs.
     * The newly caught Pokemon starts at level 5.
     */
    public void addPokemonToTeam(String userId, int pokedexId) {
        database.update(
                """
                INSERT INTO trainer_pokemon (trainer_id, pokedex_id, level)
                SELECT id, ?, 5
                FROM trainers
                WHERE user_id = ?::uuid
                """,
                pokedexId, userId
        );
    }

    /**
     * Removes a specific caught Pokemon from the trainer's team (releasing it).
     *
     * The nested SELECT ensures a trainer can only delete Pokemon from their
     * own team — even if a bad actor sends someone else's caught_pokemon_id,
     * the WHERE clause will find no match and nothing will be deleted.
     */
    public void removePokemonFromTeam(String userId, String caughtPokemonId) {
        database.update(
                """
                DELETE FROM trainer_pokemon
                WHERE id = ?::uuid
                  AND trainer_id IN (
                      SELECT id FROM trainers WHERE user_id = ?::uuid
                  )
                """,
                caughtPokemonId, userId
        );
    }
}
