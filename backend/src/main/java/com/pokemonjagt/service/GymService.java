package com.pokemonjagt.service;

import com.pokemonjagt.dto.*;
import com.pokemonjagt.entity.*;
import com.pokemonjagt.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class GymService {

    private static final int GYM_MONEY     = 50_000;
    private static final int MAX_ROSTER    = 6;

    private final GymMemberRepository  gymMemberRepository;
    private final GymRosterRepository  gymRosterRepository;
    private final TrainerPokemonRepository trainerPokemonRepository;
    private final AuthService          authService;

    // -------------------------------------------------------------------------
    // Profil
    // -------------------------------------------------------------------------

    @Transactional
    public GymMemberResponse createOwnProfile(String userId, GymProfileRequest req) {
        GymMember member = new GymMember();
        member.setUserId(UUID.fromString(userId));
        member.setName(req.getName());
        member.setEmail(req.getEmail());
        member.setPhoneNumber(req.getPhoneNumber());
        member.setAddress(req.getAddress());
        member.setRole(req.getRole());
        gymMemberRepository.save(member);
        return toResponse(member, false);
    }

    @Transactional
    public Optional<GymMemberResponse> getMyProfile(String userId) {
        return gymMemberRepository.findByUserId(UUID.fromString(userId))
                .map(m -> toResponse(m, gymRosterRepository.existsByMemberId(m.getId())));
    }

    // -------------------------------------------------------------------------
    // Gym leader: brugerstyring
    // -------------------------------------------------------------------------

    @Transactional
    public GymMemberResponse createMemberAsGymLeader(String callerUserId, CreateMemberRequest req) {
        requireRole(callerUserId, MemberRole.GYM_LEADER);

        String newUserUuid = authService.createUserAsAdmin(req.getEmail(), req.getPassword());

        GymMember member = new GymMember();
        member.setUserId(UUID.fromString(newUserUuid));
        member.setName(req.getName());
        member.setEmail(req.getEmail());
        member.setPhoneNumber(req.getPhoneNumber());
        member.setAddress(req.getAddress());
        member.setRole(req.getRole());
        gymMemberRepository.save(member);
        return toResponse(member, false);
    }

    @Transactional
    public List<GymMemberResponse> getAllMembers(String callerUserId) {
        requireRole(callerUserId, MemberRole.GYM_LEADER);
        return gymMemberRepository.findAll().stream()
                .map(m -> toResponse(m, gymRosterRepository.existsByMemberId(m.getId())))
                .toList();
    }

    // -------------------------------------------------------------------------
    // Gym leader: roster (max 6 trainers i gymmet ad gangen)
    // -------------------------------------------------------------------------

    @Transactional
    public void addToRoster(String callerUserId, UUID memberId) {
        requireRole(callerUserId, MemberRole.GYM_LEADER);

        if (gymRosterRepository.count() >= MAX_ROSTER) {
            throw new IllegalStateException("Gym roster er fuld — max " + MAX_ROSTER + " trainers");
        }
        if (gymRosterRepository.existsByMemberId(memberId)) {
            throw new IllegalStateException("Denne trainer er allerede i gymmet");
        }

        GymMember member = gymMemberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("Bruger ikke fundet"));

        if (member.getRole() != MemberRole.TRAINER) {
            throw new IllegalArgumentException("Kun trainers kan tilføjes til gym roster");
        }

        GymRoster entry = new GymRoster();
        entry.setMember(member);
        gymRosterRepository.save(entry);
    }

    @Transactional
    public void removeFromRoster(String callerUserId, UUID memberId) {
        requireRole(callerUserId, MemberRole.GYM_LEADER);

        GymRoster entry = gymRosterRepository.findByMemberId(memberId)
                .orElseThrow(() -> new IllegalArgumentException("Denne trainer er ikke i gymmet"));

        gymRosterRepository.delete(entry);
    }

    // -------------------------------------------------------------------------
    // Rolle-baserede views
    // -------------------------------------------------------------------------

    @Transactional
    public List<TeamPokemonResponse> getGymLeaderTeam(String callerUserId) {
        requireRole(callerUserId, MemberRole.FARMER, MemberRole.GYM_LEADER);

        GymMember leader = gymMemberRepository.findByRole(MemberRole.GYM_LEADER)
                .stream().findFirst()
                .orElseThrow(() -> new IllegalStateException("Ingen gym leader fundet"));

        return trainerPokemonRepository.findByMemberIdOrderByCaughtAtDesc(leader.getId())
                .stream()
                .map(this::toTeamPokemonResponse)
                .toList();
    }

    public Map<String, Integer> getGymMoney(String callerUserId) {
        requireRole(callerUserId, MemberRole.BREEDER, MemberRole.GYM_LEADER);
        return Map.of("money", GYM_MONEY);
    }

    // -------------------------------------------------------------------------
    // Hjælpemetoder
    // -------------------------------------------------------------------------

    private void requireRole(String userId, MemberRole... allowed) {
        GymMember member = gymMemberRepository.findByUserId(UUID.fromString(userId))
                .orElseThrow(() -> new IllegalStateException("Ingen profil fundet — opret din profil først"));
        for (MemberRole role : allowed) {
            if (member.getRole() == role) return;
        }
        throw new AccessDeniedException("Din rolle (" + member.getRole() + ") har ikke adgang til denne ressource");
    }

    private GymMemberResponse toResponse(GymMember m, boolean inRoster) {
        return new GymMemberResponse(
                m.getId().toString(),
                m.getName(),
                m.getEmail(),
                m.getPhoneNumber(),
                m.getAddress(),
                m.getRole().name(),
                inRoster,
                m.getCreatedAt() != null ? m.getCreatedAt().toString() : null
        );
    }

    private TeamPokemonResponse toTeamPokemonResponse(TrainerPokemon tp) {
        return new TeamPokemonResponse(
                tp.getId().toString(),
                tp.getNickname(),
                tp.getLevel(),
                tp.getCaughtAt() != null ? tp.getCaughtAt().toString() : null,
                tp.getPokedex().getName(),
                tp.getPokedex().getType(),
                tp.getPokedex().getSpriteUrl()
        );
    }
}
