package com.pokemonjagt.controller;

import com.pokemonjagt.dto.CreateTrainerRequest;
import com.pokemonjagt.dto.TeamPokemonResponse;
import com.pokemonjagt.dto.TrainerResponse;
import com.pokemonjagt.service.TrainerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/trainer")
public class TrainerController {

    private final TrainerService trainerService;

    // @AuthenticationPrincipal Jwt jwt:
    // Spring Security har allerede verificeret JWT'en inden denne metode kaldes
    // Her beder vi Spring om at give os det verificerede JWT-objekt direkte som parameter
    // jwt.getSubject() returnerer "sub"-feltet fra JWT — det er brugerens UUID fra Supabase
    @GetMapping
    public ResponseEntity<TrainerResponse> getOwnTrainerProfile(@AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject();
        return trainerService.findTrainerByUserId(userId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<TrainerResponse> createOwnTrainerProfile(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody CreateTrainerRequest body) {

        String userId = jwt.getSubject();
        TrainerResponse created = trainerService.createTrainerProfile(userId, body.getName(), body.getGender());
        return ResponseEntity.ok(created);
    }

    @GetMapping("/team")
    public List<TeamPokemonResponse> getOwnPokemonTeam(@AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject();
        return trainerService.getTrainerTeam(userId);
    }

    @PostMapping("/team/{pokedexId}")
    public ResponseEntity<Void> catchPokemonFromPokedex(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable int pokedexId) {

        String userId = jwt.getSubject();
        trainerService.addPokemonToTeam(userId, pokedexId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/team/{caughtPokemonId}")
    public ResponseEntity<Void> releasePokemonFromTeam(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable String caughtPokemonId) {

        String userId = jwt.getSubject();
        trainerService.removePokemonFromTeam(userId, caughtPokemonId);
        return ResponseEntity.ok().build();
    }
}
