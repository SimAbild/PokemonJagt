package com.pokemonjagt.controller;

import com.pokemonjagt.dto.TeamPokemonResponse;
import com.pokemonjagt.dto.TrainerTeamResponse;
import com.pokemonjagt.service.TrainerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/trainer")
public class TrainerController {

    private final TrainerService trainerService;

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<String> handleAccessDenied(AccessDeniedException e) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
    }

    @ExceptionHandler({IllegalStateException.class, IllegalArgumentException.class})
    public ResponseEntity<String> handleBadRequest(RuntimeException e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }

    // Trainer ser og administrerer sit eget hold
    @GetMapping("/team")
    public List<TeamPokemonResponse> getOwnTeam(@AuthenticationPrincipal Jwt jwt) {
        return trainerService.getOwnTeam(jwt.getSubject());
    }

    @PostMapping("/team/{pokedexId}")
    public ResponseEntity<Void> catchPokemon(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable int pokedexId) {
        trainerService.addPokemon(jwt.getSubject(), pokedexId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/team/{caughtPokemonId}")
    public ResponseEntity<Void> releasePokemon(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable String caughtPokemonId) {
        trainerService.removePokemon(jwt.getSubject(), caughtPokemonId);
        return ResponseEntity.ok().build();
    }

    // Trainer (og gym leader) ser alle trainers' hold
    @GetMapping("/all-teams")
    public List<TrainerTeamResponse> getAllTrainerTeams(@AuthenticationPrincipal Jwt jwt) {
        return trainerService.getAllTrainerTeams(jwt.getSubject());
    }
}
