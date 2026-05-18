package com.pokemonjagt.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GymMemberResponse {
    private String id;
    private String name;
    private String email;
    private String phoneNumber;
    private String address;
    private String role;
    private boolean inRoster;
    private String createdAt;
}
