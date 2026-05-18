package com.pokemonjagt.repository;

import com.pokemonjagt.entity.GymRoster;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface GymRosterRepository extends JpaRepository<GymRoster, UUID> {

    Optional<GymRoster> findByMemberId(UUID memberId);

    boolean existsByMemberId(UUID memberId);
}
