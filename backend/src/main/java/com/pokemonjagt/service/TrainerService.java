package com.pokemonjagt.service;

import com.pokemonjagt.dto.TeamPokemonResponse;
import com.pokemonjagt.dto.TrainerResponse;
import com.pokemonjagt.entity.Trainer;
import com.pokemonjagt.entity.TrainerPokemon;
import com.pokemonjagt.repository.PokedexRepository;
import com.pokemonjagt.repository.TrainerPokemonRepository;
import com.pokemonjagt.repository.TrainerRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class TrainerService {

    private final TrainerRepository trainerRepository;
    private final TrainerPokemonRepository trainerPokemonRepository;
    private final PokedexRepository pokedexRepository;

    public Optional<TrainerResponse> findTrainerByUserId(String userId) {
        return trainerRepository.findByUserId(UUID.fromString(userId))
                .map(this::toTrainerResponse);
    }

    @Transactional
    public TrainerResponse createTrainerProfile(String userId, String trainerName, String gender) {
        Trainer trainer = new Trainer();
        trainer.setUserId(UUID.fromString(userId));
        trainer.setName(trainerName);
        trainer.setGender(gender);
        trainerRepository.save(trainer);
        return toTrainerResponse(trainer);
    }

    public List<TeamPokemonResponse> getTrainerTeam(String userId) {
        return trainerPokemonRepository
                .findByTrainerUserIdOrderByCaughtAtDesc(UUID.fromString(userId))
                .stream()
                .map(this::toTeamPokemonResponse)
                .toList();
    }

    @Transactional
    public void addPokemonToTeam(String userId, int pokedexId) {
        Trainer trainer = trainerRepository.findByUserId(UUID.fromString(userId))
                .orElseThrow(() -> new IllegalStateException("Trainer not found for user " + userId));

        var pokedexEntry = pokedexRepository.findById(pokedexId)
                .orElseThrow(() -> new IllegalArgumentException("No pokemon with pokedex id " + pokedexId));

        TrainerPokemon caught = new TrainerPokemon();
        caught.setTrainer(trainer);
        caught.setPokedex(pokedexEntry);
        caught.setLevel(5);
        trainerPokemonRepository.save(caught);
    }

    @Transactional
    public void removePokemonFromTeam(String userId, String caughtPokemonId) {
        TrainerPokemon entry = trainerPokemonRepository.findById(UUID.fromString(caughtPokemonId))
                .orElseThrow(() -> new IllegalArgumentException("Caught pokemon not found"));

        if (!entry.getTrainer().getUserId().equals(UUID.fromString(userId))) {
            throw new IllegalStateException("Pokemon does not belong to this trainer");
        }

        trainerPokemonRepository.delete(entry);
    }

    private TrainerResponse toTrainerResponse(Trainer t) {
        return new TrainerResponse(
                t.getId().toString(),
                t.getName(),
                t.getGender(),
                t.getCreatedAt() != null ? t.getCreatedAt().toString() : null
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
