package com.pokemonjagt.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "pokedex")
public class Pokedex {

    @Id
    private Integer id;

    private String name;

    private String type;

    @Column(name = "sprite_url")
    private String spriteUrl;
}
