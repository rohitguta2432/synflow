package com.synflow.service;

import com.synflow.dto.*;
import com.synflow.entity.Profile;
import com.synflow.entity.ProfileConnection;
import com.synflow.entity.User;
import com.synflow.repository.ProfileConnectionRepository;
import com.synflow.repository.ProfileRepository;
import com.synflow.repository.UserRepository;
import com.synflow.util.EncryptionUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProfileServiceTest {

    @Mock private ProfileRepository profileRepository;
    @Mock private ProfileConnectionRepository connectionRepository;
    @Mock private UserRepository userRepository;
    @Mock private EncryptionUtil encryptionUtil;
    @Mock private EmbeddingService embeddingService;

    @InjectMocks private ProfileService profileService;

    private Profile buildProfile(UUID id, String name, String industry) {
        Profile p = Profile.builder()
                .id(id).uniqueCode("Member_001").name(name).type(Profile.ProfileType.REAL)
                .expertise(List.of("M&A", "FinTech")).servicesOffered("enc_services")
                .industryFocus(industry).geographicReach(List.of("EMEA"))
                .trackRecord("enc_track").contactStatus(Profile.ContactStatus.ACTIVE)
                .summary("Summary").aiGenerated(false)
                .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now())
                .build();
        p.setConnections(new ArrayList<>());
        p.setMatches(new ArrayList<>());
        return p;
    }

    private User buildUser(UUID id) {
        return User.builder().id(id).email("a@b.com").fullName("Test").role(User.Role.ADMIN).passwordHash("h").build();
    }

    @Test
    void findAll_returnsPagedResults() {
        Profile p = buildProfile(UUID.randomUUID(), "Julian", "FinTech");
        p.setCreatedBy(buildUser(UUID.randomUUID()));
        Page<Profile> page = new PageImpl<>(List.of(p));
        when(profileRepository.findWithFilters(any(), any(), any(), any(), any())).thenReturn(page);
        when(encryptionUtil.decrypt("enc_services")).thenReturn("services");
        when(encryptionUtil.decrypt("enc_track")).thenReturn("track");

        Page<ProfileDto> result = profileService.findAll(null, null, null, null, PageRequest.of(0, 20));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).name()).isEqualTo("Julian");
    }

    @Test
    void findAll_withFilters_passesCorrectly() {
        when(profileRepository.findWithFilters(eq("search"), eq("FinTech"), eq(Profile.ProfileType.REAL), eq(Profile.ContactStatus.ACTIVE), any()))
                .thenReturn(new PageImpl<>(List.of()));

        profileService.findAll("search", "FinTech", "REAL", "ACTIVE", PageRequest.of(0, 20));

        verify(profileRepository).findWithFilters("search", "FinTech", Profile.ProfileType.REAL, Profile.ContactStatus.ACTIVE, PageRequest.of(0, 20));
    }

    @Test
    void findById_existingProfile_returnsDetail() {
        UUID id = UUID.randomUUID();
        Profile p = buildProfile(id, "Alice", "Healthcare");
        p.setCreatedBy(buildUser(UUID.randomUUID()));
        when(profileRepository.findById(id)).thenReturn(Optional.of(p));
        when(encryptionUtil.decrypt(any())).thenReturn("decrypted");

        ProfileDto dto = profileService.findById(id);

        assertThat(dto.id()).isEqualTo(id);
        assertThat(dto.name()).isEqualTo("Alice");
        assertThat(dto.connections()).isEmpty();
        assertThat(dto.matches()).isEmpty();
    }

    @Test
    void findById_notFound_throwsException() {
        UUID id = UUID.randomUUID();
        when(profileRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> profileService.findById(id))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Profile not found");
    }

    @Test
    void create_validRequest_savesAndReturns() {
        UUID userId = UUID.randomUUID();
        User user = buildUser(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(profileRepository.existsByUniqueCode(any())).thenReturn(false);
        when(encryptionUtil.encrypt(any())).thenReturn("encrypted");
        when(encryptionUtil.decrypt(any())).thenReturn("decrypted");

        Profile saved = buildProfile(UUID.randomUUID(), "New Profile", "Energy");
        saved.setCreatedBy(user);
        when(profileRepository.save(any())).thenReturn(saved);

        ProfileRequest req = new ProfileRequest("New Profile", "REAL", List.of("Solar"), "services",
                "Energy", List.of("APAC"), "track", "ACTIVE", "summary", false);

        ProfileDto dto = profileService.create(req, userId);

        assertThat(dto.name()).isEqualTo("New Profile");
        verify(encryptionUtil).encrypt("services");
        verify(encryptionUtil).encrypt("track");
        verify(profileRepository).save(any());
    }

    @Test
    void create_userNotFound_throwsException() {
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> profileService.create(
                new ProfileRequest("X", "REAL", List.of(), null, null, List.of(), null, null, null, false), userId))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("User not found");
    }

    @Test
    void update_existingProfile_updatesFields() {
        UUID id = UUID.randomUUID();
        Profile existing = buildProfile(id, "Old Name", "FinTech");
        existing.setCreatedBy(buildUser(UUID.randomUUID()));
        when(profileRepository.findById(id)).thenReturn(Optional.of(existing));
        when(encryptionUtil.encrypt(any())).thenReturn("encrypted");
        when(encryptionUtil.decrypt(any())).thenReturn("decrypted");
        when(profileRepository.save(any())).thenReturn(existing);

        ProfileRequest req = new ProfileRequest("New Name", "SHADOW", List.of("Tag1"), "new svc",
                "Healthcare", List.of("UK"), "new track", "EXTERNAL", "new summary", false);
        profileService.update(id, req);

        assertThat(existing.getName()).isEqualTo("New Name");
        assertThat(existing.getType()).isEqualTo(Profile.ProfileType.SHADOW);
        assertThat(existing.getContactStatus()).isEqualTo(Profile.ContactStatus.EXTERNAL);
        verify(profileRepository).save(existing);
    }

    @Test
    void update_notFound_throwsException() {
        UUID id = UUID.randomUUID();
        when(profileRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> profileService.update(id,
                new ProfileRequest("X", "REAL", List.of(), null, null, List.of(), null, null, null, false)))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void delete_callsRepository() {
        UUID id = UUID.randomUUID();
        profileService.delete(id);
        verify(profileRepository).deleteById(id);
    }

    @Test
    void addConnection_validProfiles_savesConnection() {
        UUID profileId = UUID.randomUUID();
        UUID connectedId = UUID.randomUUID();
        Profile profile = buildProfile(profileId, "A", "X");
        Profile connected = buildProfile(connectedId, "B", "Y");
        when(profileRepository.findById(profileId)).thenReturn(Optional.of(profile));
        when(profileRepository.findById(connectedId)).thenReturn(Optional.of(connected));

        ProfileConnection savedConn = ProfileConnection.builder()
                .id(UUID.randomUUID()).profile(profile).connectedProfile(connected)
                .connectionType("advisor").build();
        when(connectionRepository.save(any())).thenReturn(savedConn);

        ProfileDto.ConnectionDto result = profileService.addConnection(profileId, new ConnectionRequest(connectedId, "advisor"));

        assertThat(result.connectedProfileName()).isEqualTo("B");
        assertThat(result.connectionType()).isEqualTo("advisor");
    }

    @Test
    void addConnection_profileNotFound_throws() {
        UUID id = UUID.randomUUID();
        when(profileRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> profileService.addConnection(id, new ConnectionRequest(UUID.randomUUID(), "x")))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void removeConnection_callsRepository() {
        UUID profileId = UUID.randomUUID();
        UUID connId = UUID.randomUUID();
        profileService.removeConnection(profileId, connId);
        verify(connectionRepository).deleteById(connId);
    }

    @Test
    void getGraph_returnsNodesAndEdges() {
        UUID profileId = UUID.randomUUID();
        UUID connectedId = UUID.randomUUID();
        Profile p1 = buildProfile(profileId, "A", "X");
        Profile p2 = buildProfile(connectedId, "B", "Y");

        ProfileConnection conn = ProfileConnection.builder()
                .id(UUID.randomUUID()).profile(p1).connectedProfile(p2).connectionType("partner").build();
        when(connectionRepository.findByProfileIdOrConnectedProfileId(profileId, profileId))
                .thenReturn(List.of(conn));
        when(profileRepository.findAllById(any())).thenReturn(List.of(p1, p2));

        GraphDto graph = profileService.getGraph(profileId);

        assertThat(graph.nodes()).hasSize(2);
        assertThat(graph.edges()).hasSize(1);
        assertThat(graph.edges().get(0).connectionType()).isEqualTo("partner");
    }
}
