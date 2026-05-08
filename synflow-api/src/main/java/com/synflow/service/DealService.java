package com.synflow.service;

import com.synflow.dto.*;
import com.synflow.entity.Deal;
import com.synflow.entity.User;
import com.synflow.repository.DealRepository;
import com.synflow.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
public class DealService {

    private final DealRepository dealRepository;
    private final UserRepository userRepository;
    private final EmbeddingService embeddingService;

    public Page<DealDto> findAll(String search, String industry, String dealType, String status, Pageable pageable) {
        Deal.DealType type = dealType != null ? Deal.DealType.valueOf(dealType) : null;
        Deal.DealStatus dealStatus = status != null ? Deal.DealStatus.valueOf(status) : null;

        return dealRepository.findWithFilters(search, industry, type, dealStatus, pageable)
                .map(this::toDto);
    }

    public DealDto findById(UUID id) {
        Deal deal = dealRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Deal not found"));
        return toDetailDto(deal);
    }

    @Transactional
    public DealDto create(DealRequest request, UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Deal deal = Deal.builder()
                .title(request.title())
                .industry(request.industry())
                .dealType(Deal.DealType.valueOf(request.dealType()))
                .ticketSize(request.ticketSize())
                .geography(request.geography())
                .requirements(request.requirements())
                .status(request.status() != null ? Deal.DealStatus.valueOf(request.status()) : Deal.DealStatus.ACTIVE)
                .createdBy(user)
                .build();

        Deal saved = dealRepository.save(deal);
        embeddingService.embedDeal(saved);
        return toDto(saved);
    }

    @Transactional
    public DealDto update(UUID id, DealRequest request) {
        Deal deal = dealRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Deal not found"));

        deal.setTitle(request.title());
        deal.setIndustry(request.industry());
        deal.setDealType(Deal.DealType.valueOf(request.dealType()));
        deal.setTicketSize(request.ticketSize());
        deal.setGeography(request.geography());
        deal.setRequirements(request.requirements());
        if (request.status() != null) {
            deal.setStatus(Deal.DealStatus.valueOf(request.status()));
        }

        Deal saved = dealRepository.save(deal);
        embeddingService.embedDeal(saved);
        return toDto(saved);
    }

    @Transactional
    public void delete(UUID id) {
        dealRepository.deleteById(id);
    }

    private DealDto toDto(Deal d) {
        return new DealDto(d.getId(), d.getTitle(), d.getIndustry(), d.getDealType().name(),
                d.getTicketSize(), d.getGeography(), d.getRequirements(), d.getStatus().name(),
                d.getCreatedBy() != null ? d.getCreatedBy().getFullName() : null,
                null, d.getCreatedAt(), d.getUpdatedAt());
    }

    private DealDto toDetailDto(Deal d) {
        List<MatchDto> matches = Optional.ofNullable(d.getMatches())
                .orElse(Collections.emptyList()).stream()
                .map(m -> new MatchDto(m.getId(), d.getId(), d.getTitle(), d.getIndustry(),
                        m.getProfile().getId(), m.getProfile().getName(),
                        m.getProfile().getExpertise(),
                        m.getRelevanceScore(), m.getMatchReason(), m.getMatchedAt()))
                .sorted(Comparator.comparing(MatchDto::relevanceScore).reversed())
                .toList();

        return new DealDto(d.getId(), d.getTitle(), d.getIndustry(), d.getDealType().name(),
                d.getTicketSize(), d.getGeography(), d.getRequirements(), d.getStatus().name(),
                d.getCreatedBy() != null ? d.getCreatedBy().getFullName() : null,
                matches, d.getCreatedAt(), d.getUpdatedAt());
    }
}
