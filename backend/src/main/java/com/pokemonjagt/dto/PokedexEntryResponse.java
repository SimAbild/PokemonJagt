package com.pokemonjagt.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PokedexEntryResponse {
    private Integer id;
    private String name;
    private String type;
    private String spriteUrl;
}
