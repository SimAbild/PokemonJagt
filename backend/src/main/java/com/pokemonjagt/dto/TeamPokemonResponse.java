package com.pokemonjagt.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TeamPokemonResponse {
    private String caughtPokemonId;
    private String nickname;
    private Integer level;
    private String caughtAt;
    private String pokemonName;
    private String pokemonType;
    private String spriteUrl;
}
