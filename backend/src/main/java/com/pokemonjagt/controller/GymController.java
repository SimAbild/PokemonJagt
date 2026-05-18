package com.pokemonjagt.controller;

import com.pokemonjagt.dto.CreateMemberRequest;
import com.pokemonjagt.dto.GymMemberResponse;
import com.pokemonjagt.dto.GymProfileRequest;
import com.pokemonjagt.dto.TeamPokemonResponse;
import com.pokemonjagt.service.GymService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/gym")
public class GymController {

    private final GymService gymService;

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<String> handleAccessDenied(AccessDeniedException e) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
    }

    @ExceptionHandler(HttpClientErrorException.class)
    public ResponseEntity<String> handleSupabaseError(HttpClientErrorException e) {
        return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsString());
    }

    @ExceptionHandler({IllegalStateException.class, IllegalArgumentException.class})
    public ResponseEntity<String> handleBadRequest(RuntimeException e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }

    // Gym leader opretter sin EGEN profil (efter normal Supabase-registrering)
    @PostMapping("/profile")
    public ResponseEntity<GymMemberResponse> createOwnProfile(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody GymProfileRequest body) {
        return ResponseEntity.ok(gymService.createOwnProfile(jwt.getSubject(), body));
    }

    // Returnerer den indloggede brugers profil og rolle
    @GetMapping("/my-profile")
    public ResponseEntity<GymMemberResponse> getMyProfile(@AuthenticationPrincipal Jwt jwt) {
        return gymService.getMyProfile(jwt.getSubject())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Gym leader opretter ny bruger (kalder Supabase admin API + gemmer i DB)
    @PostMapping("/members")
    public ResponseEntity<GymMemberResponse> createMember(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody CreateMemberRequest body) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(gymService.createMemberAsGymLeader(jwt.getSubject(), body));
    }

    // Gym leader ser alle brugere
    @GetMapping("/members")
    public List<GymMemberResponse> getAllMembers(@AuthenticationPrincipal Jwt jwt) {
        return gymService.getAllMembers(jwt.getSubject());
    }

    // Gym leader tilføjer trainer til aktivt gym (max 6)
    @PostMapping("/roster/{memberId}")
    public ResponseEntity<Void> addToRoster(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID memberId) {
        gymService.addToRoster(jwt.getSubject(), memberId);
        return ResponseEntity.ok().build();
    }

    // Gym leader fjerner trainer fra aktivt gym
    @DeleteMapping("/roster/{memberId}")
    public ResponseEntity<Void> removeFromRoster(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID memberId) {
        gymService.removeFromRoster(jwt.getSubject(), memberId);
        return ResponseEntity.ok().build();
    }

    // Farmer (og gym leader) ser gym leaderens pokemon hold
    @GetMapping("/leader-team")
    public List<TeamPokemonResponse> getGymLeaderTeam(@AuthenticationPrincipal Jwt jwt) {
        return gymService.getGymLeaderTeam(jwt.getSubject());
    }

    // Breeder (og gym leader) ser gymnasets totale penge
    @GetMapping("/money")
    public ResponseEntity<Map<String, Integer>> getGymMoney(@AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(gymService.getGymMoney(jwt.getSubject()));
    }
}
