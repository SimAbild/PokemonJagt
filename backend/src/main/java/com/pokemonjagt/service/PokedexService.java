package com.pokemonjagt.service;

import com.pokemonjagt.dto.PokedexEntryResponse;
import com.pokemonjagt.repository.PokedexRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class PokedexService {

    private final PokedexRepository pokedexRepository;

    public List<PokedexEntryResponse> getAllAvailablePokemon() {
        return pokedexRepository.findAllByOrderByIdAsc().stream()
                .map(p -> new PokedexEntryResponse(p.getId(), p.getName(), p.getType(), p.getSpriteUrl()))
                .toList();
    }
}
