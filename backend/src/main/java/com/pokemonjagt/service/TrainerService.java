package com.pokemonjagt.service;

import com.pokemonjagt.dto.GymMemberResponse;
import com.pokemonjagt.dto.TeamPokemonResponse;
import com.pokemonjagt.dto.TrainerTeamResponse;
import com.pokemonjagt.entity.GymMember;
import com.pokemonjagt.entity.MemberRole;
import com.pokemonjagt.entity.TrainerPokemon;
import com.pokemonjagt.repository.GymMemberRepository;
import com.pokemonjagt.repository.GymRosterRepository;
import com.pokemonjagt.repository.PokedexRepository;
import com.pokemonjagt.repository.TrainerPokemonRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class TrainerService {

    private final GymMemberRepository      gymMemberRepository;
    private final GymRosterRepository      gymRosterRepository;
    private final TrainerPokemonRepository trainerPokemonRepository;
    private final PokedexRepository        pokedexRepository;

    // -------------------------------------------------------------------------
    // Eget hold
    // -------------------------------------------------------------------------

    @Transactional
    public List<TeamPokemonResponse> getOwnTeam(String userId) {
        requireRole(userId, MemberRole.TRAINER, MemberRole.GYM_LEADER);
        return trainerPokemonRepository.findByMemberUserIdOrderByCaughtAtDesc(UUID.fromString(userId))
                .stream()
                .map(this::toTeamPokemonResponse)
                .toList();
    }

    @Transactional
    public void addPokemon(String userId, int pokedexId) {
        requireRole(userId, MemberRole.TRAINER, MemberRole.GYM_LEADER);

        GymMember member = gymMemberRepository.findByUserId(UUID.fromString(userId))
                .orElseThrow(() -> new IllegalStateException("Profil ikke fundet"));

        var pokedexEntry = pokedexRepository.findById(pokedexId)
                .orElseThrow(() -> new IllegalArgumentException("Pokemon ikke fundet: " + pokedexId));

        TrainerPokemon caught = new TrainerPokemon();
        caught.setMember(member);
        caught.setPokedex(pokedexEntry);
        caught.setLevel(5);
        trainerPokemonRepository.save(caught);
    }

    @Transactional
    public void removePokemon(String userId, String caughtPokemonId) {
        requireRole(userId, MemberRole.TRAINER, MemberRole.GYM_LEADER);

        TrainerPokemon entry = trainerPokemonRepository.findById(UUID.fromString(caughtPokemonId))
                .orElseThrow(() -> new IllegalArgumentException("Pokemon ikke fundet"));

        if (!entry.getMember().getUserId().equals(UUID.fromString(userId))) {
            throw new AccessDeniedException("Denne pokemon tilhører ikke dig");
        }

        trainerPokemonRepository.delete(entry);
    }

    // -------------------------------------------------------------------------
    // Alle trainers' hold — tilgængeligt for TRAINER og GYM_LEADER
    // -------------------------------------------------------------------------

    @Transactional
    public List<TrainerTeamResponse> getAllTrainerTeams(String callerUserId) {
        requireRole(callerUserId, MemberRole.TRAINER, MemberRole.GYM_LEADER);

        return gymMemberRepository.findByRole(MemberRole.TRAINER).stream()
                .map(trainer -> new TrainerTeamResponse(
                        toMemberResponse(trainer),
                        trainerPokemonRepository.findByMemberIdOrderByCaughtAtDesc(trainer.getId())
                                .stream()
                                .map(this::toTeamPokemonResponse)
                                .toList()
                ))
                .toList();
    }

    // -------------------------------------------------------------------------
    // Hjælpemetoder
    // -------------------------------------------------------------------------

    private void requireRole(String userId, MemberRole... allowed) {
        GymMember member = gymMemberRepository.findByUserId(UUID.fromString(userId))
                .orElseThrow(() -> new IllegalStateException("Profil ikke fundet"));
        for (MemberRole role : allowed) {
            if (member.getRole() == role) return;
        }
        throw new AccessDeniedException("Din rolle (" + member.getRole() + ") har ikke adgang til denne ressource");
    }

    private GymMemberResponse toMemberResponse(GymMember m) {
        return new GymMemberResponse(
                m.getId().toString(),
                m.getName(),
                m.getEmail(),
                m.getPhoneNumber(),
                m.getAddress(),
                m.getRole().name(),
                gymRosterRepository.existsByMemberId(m.getId()),
                m.getCreatedAt() != null ? m.getCreatedAt().toString() : null
        );
    }

    private TeamPokemonResponse toTeamPokemonResponse(TrainerPokemon tp) {
        return new TeamPokemonResponse(
                tp.getId().toString(),
                tp.getNickname(),
                tp.getLevel(),
                tp.getCaughtAt() != null ? tp.getCaughtAt().toString() : null,
                tp.getPokedex().getName(),
                tp.getPokedex().getType(),
                tp.getPokedex().getSpriteUrl()
        );
    }
}
