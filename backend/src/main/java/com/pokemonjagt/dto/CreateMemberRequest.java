package com.pokemonjagt.dto;

import com.pokemonjagt.entity.MemberRole;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateMemberRequest {
    private String name;
    private String email;
    private String password;
    private String phoneNumber;
    private String address;
    private MemberRole role;
}
