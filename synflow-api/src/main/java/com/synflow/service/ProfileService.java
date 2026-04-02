package com.synflow.service;

import com.synflow.dto.*;
import com.synflow.entity.Profile;
import com.synflow.entity.ProfileConnection;
import com.synflow.entity.User;
import com.synflow.repository.ProfileConnectionRepository;
import com.synflow.repository.ProfileRepository;
import com.synflow.repository.UserRepository;
import com.synflow.util.EncryptionUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final ProfileRepository profileRepository;
    private final ProfileConnectionRepository connectionRepository;
    private final UserRepository userRepository;
    private final EncryptionUtil encryptionUtil;

    private static final AtomicLong counter = new AtomicLong(System.currentTimeMillis() % 10000);

    public Page<ProfileDto> findAll(String search, String industry, String type, String status, Pageable pageable) {
        Profile.ProfileType profileType = type != null ? Profile.ProfileType.valueOf(type) : null;
        Profile.ContactStatus contactStatus = status != null ? Profile.ContactStatus.valueOf(status) : null;

        return profileRepository.findWithFilters(search, industry, profileType, contactStatus, pageable)
                .map(this::toDto);
    }

    public ProfileDto findById(UUID id) {
        Profile profile = profileRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Profile not found"));
        return toDetailDto(profile);
    }

    @Transactional
    public ProfileDto create(ProfileRequest request, UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String uniqueCode = generateUniqueCode(request.type());

        Profile profile = Profile.builder()
                .uniqueCode(uniqueCode)
                .name(request.name())
                .type(Profile.ProfileType.valueOf(request.type()))
                .expertise(request.expertise())
                .servicesOffered(encryptionUtil.encrypt(request.servicesOffered()))
                .industryFocus(request.industryFocus())
                .geographicReach(request.geographicReach())
                .trackRecord(encryptionUtil.encrypt(request.trackRecord()))
                .contactStatus(request.contactStatus() != null
                        ? Profile.ContactStatus.valueOf(request.contactStatus())
                        : Profile.ContactStatus.ACTIVE)
                .summary(request.summary())
                .aiGenerated(request.aiGenerated() != null ? request.aiGenerated() : false)
                .createdBy(user)
                .build();

        return toDto(profileRepository.save(profile));
    }

    @Transactional
    public ProfileDto update(UUID id, ProfileRequest request) {
        Profile profile = profileRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Profile not found"));

        profile.setName(request.name());
        profile.setType(Profile.ProfileType.valueOf(request.type()));
        profile.setExpertise(request.expertise());
        profile.setServicesOffered(encryptionUtil.encrypt(request.servicesOffered()));
        profile.setIndustryFocus(request.industryFocus());
        profile.setGeographicReach(request.geographicReach());
        profile.setTrackRecord(encryptionUtil.encrypt(request.trackRecord()));
        if (request.contactStatus() != null) {
            profile.setContactStatus(Profile.ContactStatus.valueOf(request.contactStatus()));
        }
        profile.setSummary(request.summary());

        return toDto(profileRepository.save(profile));
    }

    @Transactional
    public void delete(UUID id) {
        profileRepository.deleteById(id);
    }

    @Transactional
    public ProfileDto.ConnectionDto addConnection(UUID profileId, ConnectionRequest request) {
        Profile profile = profileRepository.findById(profileId)
                .orElseThrow(() -> new RuntimeException("Profile not found"));
        Profile connected = profileRepository.findById(request.connectedProfileId())
                .orElseThrow(() -> new RuntimeException("Connected profile not found"));

        ProfileConnection connection = ProfileConnection.builder()
                .profile(profile)
                .connectedProfile(connected)
                .connectionType(request.connectionType())
                .build();

        connection = connectionRepository.save(connection);

        return new ProfileDto.ConnectionDto(
                connection.getId(),
                connected.getId(), connected.getName(),
                connected.getType().name(), connected.getIndustryFocus(),
                connection.getConnectionType());
    }

    @Transactional
    public void removeConnection(UUID profileId, UUID connectionId) {
        connectionRepository.deleteById(connectionId);
    }

    public GraphDto getGraph(UUID profileId) {
        List<ProfileConnection> connections = connectionRepository
                .findByProfileIdOrConnectedProfileId(profileId, profileId);

        Set<UUID> nodeIds = new HashSet<>();
        nodeIds.add(profileId);
        connections.forEach(c -> {
            nodeIds.add(c.getProfile().getId());
            nodeIds.add(c.getConnectedProfile().getId());
        });

        List<Profile> profiles = profileRepository.findAllById(nodeIds);
        List<GraphDto.GraphNode> nodes = profiles.stream()
                .map(p -> new GraphDto.GraphNode(p.getId(), p.getName(), p.getType().name(), p.getIndustryFocus()))
                .toList();

        List<GraphDto.GraphEdge> edges = connections.stream()
                .map(c -> new GraphDto.GraphEdge(c.getProfile().getId(),
                        c.getConnectedProfile().getId(), c.getConnectionType()))
                .toList();

        return new GraphDto(nodes, edges);
    }

    private String generateUniqueCode(String type) {
        String prefix = "REAL".equals(type) ? "Member" : "External";
        String code;
        do {
            code = prefix + "_" + String.format("%03d", counter.incrementAndGet() % 1000);
        } while (profileRepository.existsByUniqueCode(code));
        return code;
    }

    private ProfileDto toDto(Profile p) {
        return new ProfileDto(
                p.getId(), p.getUniqueCode(), p.getName(), p.getType().name(),
                p.getExpertise(),
                encryptionUtil.decrypt(p.getServicesOffered()),
                p.getIndustryFocus(), p.getGeographicReach(),
                encryptionUtil.decrypt(p.getTrackRecord()),
                p.getContactStatus().name(), p.getSummary(), p.getAiGenerated(),
                p.getCreatedBy() != null ? p.getCreatedBy().getFullName() : null,
                null, null,
                p.getCreatedAt(), p.getUpdatedAt());
    }

    private ProfileDto toDetailDto(Profile p) {
        List<ProfileDto.ConnectionDto> connections = Optional.ofNullable(p.getConnections())
                .orElse(Collections.emptyList()).stream()
                .map(c -> new ProfileDto.ConnectionDto(
                        c.getId(),
                        c.getConnectedProfile().getId(),
                        c.getConnectedProfile().getName(),
                        c.getConnectedProfile().getType().name(),
                        c.getConnectedProfile().getIndustryFocus(),
                        c.getConnectionType()))
                .toList();

        List<MatchDto> matches = Optional.ofNullable(p.getMatches())
                .orElse(Collections.emptyList()).stream()
                .map(m -> new MatchDto(m.getId(), m.getDeal().getId(), m.getDeal().getTitle(),
                        m.getDeal().getIndustry(), p.getId(), p.getName(), p.getExpertise(),
                        m.getRelevanceScore(), m.getMatchReason(), m.getMatchedAt()))
                .toList();

        return new ProfileDto(
                p.getId(), p.getUniqueCode(), p.getName(), p.getType().name(),
                p.getExpertise(),
                encryptionUtil.decrypt(p.getServicesOffered()),
                p.getIndustryFocus(), p.getGeographicReach(),
                encryptionUtil.decrypt(p.getTrackRecord()),
                p.getContactStatus().name(), p.getSummary(), p.getAiGenerated(),
                p.getCreatedBy() != null ? p.getCreatedBy().getFullName() : null,
                connections, matches,
                p.getCreatedAt(), p.getUpdatedAt());
    }
}
