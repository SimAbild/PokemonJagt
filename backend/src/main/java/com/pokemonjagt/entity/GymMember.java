package com.pokemonjagt.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "gym_members")
public class GymMember {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", unique = true)
    private UUID userId;

    @Column(nullable = false)
    private String name;

    @Column(unique = true)
    private String email;

    @Column(name = "phone_number")
    private String phoneNumber;

    private String address;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MemberRole role;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;

    @OneToMany(mappedBy = "member", fetch = FetchType.LAZY)
    private List<TrainerPokemon> team;

    // Lazy: kun tilgås i @Transactional kontekst — bruges til at tjekke om trainer er i gymmet
    @OneToOne(mappedBy = "member", fetch = FetchType.LAZY)
    private GymRoster rosterEntry;
}
