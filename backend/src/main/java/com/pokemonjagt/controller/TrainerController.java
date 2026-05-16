package com.pokemonjagt.controller;

import com.pokemonjagt.service.TrainerService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Handles everything related to the trainer's profile and Pokemon team.
 *
 * Every endpoint reads the authenticated user's ID from the request object.
 * The JwtInterceptor placed it there after validating the JWT —
 * so by the time we reach this controller, we know exactly who is calling.
 *
 * The pattern used in every method:
 *   1. Get the user's ID from the validated JWT (already on the request)
 *   2. Pass it to the service, which scopes all queries to that user
 *   3. Return the result
 */
@RestController
@RequestMapping("/api/trainer")
public class TrainerController {

    private final TrainerService trainerService;

    public TrainerController(TrainerService trainerService) {
        this.trainerService = trainerService;
    }

    /**
     * GET /api/trainer
     *
     * Returns the trainer profile of the logged-in user.
     * Returns 404 if the user has never created a trainer profile —
     * the frontend uses this to decide whether to show the "Create Profile" form.
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getOwnTrainerProfile(HttpServletRequest request) {
        String authenticatedUserId = (String) request.getAttribute("authenticatedUserId");

        return trainerService.findTrainerByUserId(authenticatedUserId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * POST /api/trainer
     *
     * Creates a trainer profile for the logged-in user.
     * Can only be done once per user (the database enforces this with a UNIQUE constraint).
     *
     * Request body: { "name": "Ash", "gender": "male" }
     * Response:     the newly created trainer profile
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createOwnTrainerProfile(
            HttpServletRequest request,
            @RequestBody Map<String, String> profileDetails) {

        String authenticatedUserId = (String) request.getAttribute("authenticatedUserId");

        Map<String, Object> createdProfile = trainerService.createTrainerProfile(
                authenticatedUserId,
                profileDetails.get("name"),
                profileDetails.get("gender")
        );

        return ResponseEntity.ok(createdProfile);
    }

    /**
     * GET /api/trainer/team
     *
     * Returns all Pokemon on the logged-in trainer's team.
     * Each Pokemon includes its name, type, sprite URL, nickname, and level.
     */
    @GetMapping("/team")
    public List<Map<String, Object>> getOwnPokemonTeam(HttpServletRequest request) {
        String authenticatedUserId = (String) request.getAttribute("authenticatedUserId");
        return trainerService.getTrainerTeam(authenticatedUserId);
    }

    /**
     * POST /api/trainer/team/{pokedexId}
     *
     * Catches a Pokemon from the Pokedex and adds it to the trainer's team.
     * pokedexId is the national Pokedex number (e.g. 25 = Pikachu, 6 = Charizard).
     */
    @PostMapping("/team/{pokedexId}")
    public ResponseEntity<Void> catchPokemonFromPokedex(
            HttpServletRequest request,
            @PathVariable int pokedexId) {

        String authenticatedUserId = (String) request.getAttribute("authenticatedUserId");
        trainerService.addPokemonToTeam(authenticatedUserId, pokedexId);
        return ResponseEntity.ok().build();
    }

    /**
     * DELETE /api/trainer/team/{caughtPokemonId}
     *
     * Releases a Pokemon from the trainer's team.
     * caughtPokemonId is the UUID of the row in trainer_pokemon — not the Pokedex number.
     * The service verifies ownership before deleting, so trainers cannot release each other's Pokemon.
     */
    @DeleteMapping("/team/{caughtPokemonId}")
    public ResponseEntity<Void> releasePokemonFromTeam(
            HttpServletRequest request,
            @PathVariable String caughtPokemonId) {

        String authenticatedUserId = (String) request.getAttribute("authenticatedUserId");
        trainerService.removePokemonFromTeam(authenticatedUserId, caughtPokemonId);
        return ResponseEntity.ok().build();
    }
}
