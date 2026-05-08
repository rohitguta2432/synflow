package com.synflow.service;

import com.synflow.dto.DealDto;
import com.synflow.dto.DealRequest;
import com.synflow.entity.Deal;
import com.synflow.entity.User;
import com.synflow.repository.DealRepository;
import com.synflow.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DealServiceTest {

    @Mock private DealRepository dealRepository;
    @Mock private UserRepository userRepository;
    @Mock private EmbeddingService embeddingService;

    @InjectMocks private DealService dealService;

    private Deal buildDeal(UUID id, String title, String industry) {
        Deal d = Deal.builder()
                .id(id).title(title).industry(industry).dealType(Deal.DealType.INVESTMENT)
                .ticketSize("$50M").geography(List.of("EMEA")).requirements("Some requirements")
                .status(Deal.DealStatus.ACTIVE)
                .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now())
                .build();
        d.setMatches(new ArrayList<>());
        return d;
    }

    private User buildUser(UUID id) {
        return User.builder().id(id).email("a@b.com").fullName("Test").role(User.Role.ADMIN).passwordHash("h").build();
    }

    @Test
    void findAll_returnsPagedResults() {
        Deal deal = buildDeal(UUID.randomUUID(), "Project Alpha", "FinTech");
        deal.setCreatedBy(buildUser(UUID.randomUUID()));
        when(dealRepository.findWithFilters(any(), any(), any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of(deal)));

        Page<DealDto> result = dealService.findAll(null, null, null, null, PageRequest.of(0, 20));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).title()).isEqualTo("Project Alpha");
    }

    @Test
    void findAll_withFilters_passesCorrectEnums() {
        when(dealRepository.findWithFilters(any(), any(), any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of()));

        dealService.findAll("search", "FinTech", "ADVISORY", "ACTIVE", PageRequest.of(0, 10));

        verify(dealRepository).findWithFilters("search", "FinTech", Deal.DealType.ADVISORY, Deal.DealStatus.ACTIVE, PageRequest.of(0, 10));
    }

    @Test
    void findById_existingDeal_returnsDetail() {
        UUID id = UUID.randomUUID();
        Deal deal = buildDeal(id, "Test Deal", "Energy");
        deal.setCreatedBy(buildUser(UUID.randomUUID()));
        when(dealRepository.findById(id)).thenReturn(Optional.of(deal));

        DealDto dto = dealService.findById(id);

        assertThat(dto.id()).isEqualTo(id);
        assertThat(dto.title()).isEqualTo("Test Deal");
        assertThat(dto.industry()).isEqualTo("Energy");
        assertThat(dto.matches()).isEmpty();
    }

    @Test
    void findById_notFound_throwsException() {
        UUID id = UUID.randomUUID();
        when(dealRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> dealService.findById(id))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Deal not found");
    }

    @Test
    void create_validRequest_savesAndReturns() {
        UUID userId = UUID.randomUUID();
        User user = buildUser(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        Deal saved = buildDeal(UUID.randomUUID(), "New Deal", "CleanTech");
        saved.setCreatedBy(user);
        when(dealRepository.save(any())).thenReturn(saved);

        DealRequest req = new DealRequest("New Deal", "CleanTech", "INVESTMENT", "$45M", List.of("EMEA"), "Requirements", "ACTIVE");
        DealDto dto = dealService.create(req, userId);

        assertThat(dto.title()).isEqualTo("New Deal");
        verify(dealRepository).save(any());
    }

    @Test
    void create_userNotFound_throwsException() {
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> dealService.create(
                new DealRequest("X", "Y", "INVESTMENT", null, List.of(), null, null), userId))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void create_nullStatus_defaultsToActive() {
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.of(buildUser(userId)));
        Deal saved = buildDeal(UUID.randomUUID(), "D", "I");
        saved.setCreatedBy(buildUser(userId));
        when(dealRepository.save(any())).thenReturn(saved);

        dealService.create(new DealRequest("D", "I", "ADVISORY", null, List.of(), null, null), userId);

        verify(dealRepository).save(argThat(deal -> deal.getStatus() == Deal.DealStatus.ACTIVE));
    }

    @Test
    void update_existingDeal_updatesFields() {
        UUID id = UUID.randomUUID();
        Deal existing = buildDeal(id, "Old", "Old Industry");
        existing.setCreatedBy(buildUser(UUID.randomUUID()));
        when(dealRepository.findById(id)).thenReturn(Optional.of(existing));
        when(dealRepository.save(any())).thenReturn(existing);

        DealRequest req = new DealRequest("Updated", "New Industry", "PARTNERSHIP", "$100M", List.of("APAC"), "New reqs", "CLOSED");
        dealService.update(id, req);

        assertThat(existing.getTitle()).isEqualTo("Updated");
        assertThat(existing.getIndustry()).isEqualTo("New Industry");
        assertThat(existing.getDealType()).isEqualTo(Deal.DealType.PARTNERSHIP);
        assertThat(existing.getStatus()).isEqualTo(Deal.DealStatus.CLOSED);
        verify(dealRepository).save(existing);
    }

    @Test
    void update_notFound_throwsException() {
        UUID id = UUID.randomUUID();
        when(dealRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> dealService.update(id,
                new DealRequest("X", "Y", "INVESTMENT", null, List.of(), null, null)))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void delete_callsRepository() {
        UUID id = UUID.randomUUID();
        dealService.delete(id);
        verify(dealRepository).deleteById(id);
    }
}
