package com.pokemonjagt.repository;

import com.pokemonjagt.entity.Trainer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface TrainerRepository extends JpaRepository<Trainer, UUID> {

    Optional<Trainer> findByUserId(UUID userId);
}
