package com.pokemonjagt.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrainerResponse {
    private String id;
    private String name;
    private String gender;
    private String createdAt;
}
