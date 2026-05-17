package com.pokemonjagt.controller;

import com.pokemonjagt.dto.PokedexEntryResponse;
import com.pokemonjagt.service.PokedexService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/pokedex")
public class PokedexController {

    private final PokedexService pokedexService;

    @GetMapping
    public List<PokedexEntryResponse> getAllAvailablePokemon() {
        return pokedexService.getAllAvailablePokemon();
    }
}
