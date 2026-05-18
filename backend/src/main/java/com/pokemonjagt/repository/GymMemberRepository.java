package com.pokemonjagt.repository;

import com.pokemonjagt.entity.GymMember;
import com.pokemonjagt.entity.MemberRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GymMemberRepository extends JpaRepository<GymMember, UUID> {

    Optional<GymMember> findByUserId(UUID userId);

    List<GymMember> findByRole(MemberRole role);
}
