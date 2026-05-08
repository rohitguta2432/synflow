package com.synflow.service;

import com.synflow.dto.MatchDto;
import com.synflow.dto.StatsDto;
import com.synflow.entity.Deal;
import com.synflow.entity.Match;
import com.synflow.entity.Profile;
import com.synflow.repository.DealRepository;
import com.synflow.repository.MatchRepository;
import com.synflow.repository.ProfileRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MatchServiceTest {

    @Mock private MatchRepository matchRepository;
    @Mock private DealRepository dealRepository;
    @Mock private ProfileRepository profileRepository;
    @Mock private EmbeddingService embeddingService;

    @InjectMocks private MatchService matchService;

    private Deal buildDeal(String title, String industry, List<String> geography, String requirements) {
        return Deal.builder().id(UUID.randomUUID()).title(title).industry(industry)
                .dealType(Deal.DealType.INVESTMENT).geography(geography).requirements(requirements)
                .status(Deal.DealStatus.ACTIVE).build();
    }

    private Profile buildProfile(String name, String industry, List<String> expertise, List<String> geo) {
        return Profile.builder().id(UUID.randomUUID()).uniqueCode("M_001").name(name)
                .type(Profile.ProfileType.REAL).industryFocus(industry).expertise(expertise)
                .geographicReach(geo).contactStatus(Profile.ContactStatus.ACTIVE).build();
    }

    @Test
    void matchDealToProfiles_exactIndustryMatch_scores40() {
        Deal deal = buildDeal("Test Deal", "FinTech", List.of(), "some requirements");
        Profile profile = buildProfile("Expert", "FinTech", List.of(), List.of());
        when(dealRepository.findById(deal.getId())).thenReturn(Optional.of(deal));
        when(profileRepository.findAll()).thenReturn(List.of(profile));
        when(matchRepository.save(any())).thenAnswer(inv -> {
            Match m = inv.getArgument(0);
            m.setId(UUID.randomUUID());
            m.setMatchedAt(LocalDateTime.now());
            return m;
        });

        List<MatchDto> results = matchService.matchDealToProfiles(deal.getId());

        assertThat(results).hasSize(1);
        assertThat(results.get(0).relevanceScore()).isEqualByComparingTo(BigDecimal.valueOf(40));
        assertThat(results.get(0).matchReason()).contains("industry match");
    }

    @Test
    void matchDealToProfiles_partialIndustryViaExpertise_scores20() {
        Deal deal = buildDeal("Test", "FinTech", List.of(), "requirements");
        Profile profile = buildProfile("Expert", "Banking", List.of("FinTech Consulting"), List.of());
        when(dealRepository.findById(deal.getId())).thenReturn(Optional.of(deal));
        when(profileRepository.findAll()).thenReturn(List.of(profile));
        when(matchRepository.save(any())).thenAnswer(inv -> {
            Match m = inv.getArgument(0);
            m.setId(UUID.randomUUID());
            m.setMatchedAt(LocalDateTime.now());
            return m;
        });

        List<MatchDto> results = matchService.matchDealToProfiles(deal.getId());

        assertThat(results).hasSize(1);
        assertThat(results.get(0).relevanceScore()).isEqualByComparingTo(BigDecimal.valueOf(20));
        assertThat(results.get(0).matchReason()).contains("partial industry match");
    }

    @Test
    void matchDealToProfiles_expertiseTagOverlap_scoresCorrectly() {
        Deal deal = buildDeal("M&A Advisory", "FinTech", List.of(), "Need M&A strategy and IPO preparation");
        Profile profile = buildProfile("Advisor", "FinTech", List.of("M&A", "IPO", "Valuation"), List.of());
        when(dealRepository.findById(deal.getId())).thenReturn(Optional.of(deal));
        when(profileRepository.findAll()).thenReturn(List.of(profile));
        when(matchRepository.save(any())).thenAnswer(inv -> {
            Match m = inv.getArgument(0);
            m.setId(UUID.randomUUID());
            m.setMatchedAt(LocalDateTime.now());
            return m;
        });

        List<MatchDto> results = matchService.matchDealToProfiles(deal.getId());

        // 40 (industry) + 10 (M&A tag) + 10 (IPO tag) = 60
        assertThat(results).hasSize(1);
        assertThat(results.get(0).relevanceScore().intValue()).isGreaterThanOrEqualTo(60);
    }

    @Test
    void matchDealToProfiles_geographyOverlap_scoresCorrectly() {
        Deal deal = buildDeal("Deal", "Energy", List.of("EMEA", "APAC"), "requirements");
        Profile profile = buildProfile("Pro", "Energy", List.of(), List.of("EMEA", "APAC"));
        when(dealRepository.findById(deal.getId())).thenReturn(Optional.of(deal));
        when(profileRepository.findAll()).thenReturn(List.of(profile));
        when(matchRepository.save(any())).thenAnswer(inv -> {
            Match m = inv.getArgument(0);
            m.setId(UUID.randomUUID());
            m.setMatchedAt(LocalDateTime.now());
            return m;
        });

        List<MatchDto> results = matchService.matchDealToProfiles(deal.getId());

        // 40 (industry) + 20 (2 geo matches, max 20) = 60
        assertThat(results).hasSize(1);
        assertThat(results.get(0).relevanceScore().intValue()).isEqualTo(60);
    }

    @Test
    void matchDealToProfiles_noOverlap_noResults() {
        Deal deal = buildDeal("Deal", "FinTech", List.of("EMEA"), "some text");
        Profile profile = buildProfile("Pro", "Healthcare", List.of("Genomics"), List.of("APAC"));
        when(dealRepository.findById(deal.getId())).thenReturn(Optional.of(deal));
        when(profileRepository.findAll()).thenReturn(List.of(profile));

        List<MatchDto> results = matchService.matchDealToProfiles(deal.getId());

        assertThat(results).isEmpty();
        verify(matchRepository, never()).save(any());
    }

    @Test
    void matchDealToProfiles_deletesPreviousMatches() {
        Deal deal = buildDeal("Deal", "FinTech", List.of(), "");
        when(dealRepository.findById(deal.getId())).thenReturn(Optional.of(deal));
        when(profileRepository.findAll()).thenReturn(List.of());

        matchService.matchDealToProfiles(deal.getId());

        verify(matchRepository).deleteByDealId(deal.getId());
    }

    @Test
    void matchDealToProfiles_dealNotFound_throws() {
        UUID id = UUID.randomUUID();
        when(dealRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> matchService.matchDealToProfiles(id))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Deal not found");
    }

    @Test
    void matchDealToProfiles_limitsTo20Results() {
        Deal deal = buildDeal("Deal", "FinTech", List.of(), "");
        List<Profile> profiles = new ArrayList<>();
        for (int i = 0; i < 30; i++) {
            profiles.add(buildProfile("Pro" + i, "FinTech", List.of(), List.of()));
        }
        when(dealRepository.findById(deal.getId())).thenReturn(Optional.of(deal));
        when(profileRepository.findAll()).thenReturn(profiles);
        when(matchRepository.save(any())).thenAnswer(inv -> {
            Match m = inv.getArgument(0);
            m.setId(UUID.randomUUID());
            m.setMatchedAt(LocalDateTime.now());
            return m;
        });

        List<MatchDto> results = matchService.matchDealToProfiles(deal.getId());

        assertThat(results).hasSize(20);
    }

    @Test
    void matchDealToProfiles_sortsByScoreDescending() {
        Deal deal = buildDeal("M&A Deal", "FinTech", List.of("EMEA"), "M&A expertise needed");
        Profile highScore = buildProfile("High", "FinTech", List.of("M&A"), List.of("EMEA"));
        Profile lowScore = buildProfile("Low", "FinTech", List.of(), List.of());
        when(dealRepository.findById(deal.getId())).thenReturn(Optional.of(deal));
        when(profileRepository.findAll()).thenReturn(List.of(lowScore, highScore));
        when(matchRepository.save(any())).thenAnswer(inv -> {
            Match m = inv.getArgument(0);
            m.setId(UUID.randomUUID());
            m.setMatchedAt(LocalDateTime.now());
            return m;
        });

        List<MatchDto> results = matchService.matchDealToProfiles(deal.getId());

        assertThat(results).hasSizeGreaterThanOrEqualTo(2);
        assertThat(results.get(0).relevanceScore()).isGreaterThanOrEqualTo(results.get(1).relevanceScore());
    }

    @Test
    void matchDealToProfiles_nullExpertise_handledGracefully() {
        Deal deal = buildDeal("Deal", "FinTech", List.of(), "text");
        Profile profile = Profile.builder().id(UUID.randomUUID()).uniqueCode("M_X").name("P")
                .type(Profile.ProfileType.REAL).industryFocus("FinTech")
                .expertise(null).geographicReach(null)
                .contactStatus(Profile.ContactStatus.ACTIVE).build();
        when(dealRepository.findById(deal.getId())).thenReturn(Optional.of(deal));
        when(profileRepository.findAll()).thenReturn(List.of(profile));
        when(matchRepository.save(any())).thenAnswer(inv -> {
            Match m = inv.getArgument(0);
            m.setId(UUID.randomUUID());
            m.setMatchedAt(LocalDateTime.now());
            return m;
        });

        List<MatchDto> results = matchService.matchDealToProfiles(deal.getId());

        assertThat(results).hasSize(1);
        assertThat(results.get(0).relevanceScore()).isEqualByComparingTo(BigDecimal.valueOf(40));
    }

    @Test
    void findAll_delegatesToRepository() {
        when(matchRepository.findWithFilters(any(), any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of()));

        matchService.findAll(null, null, null, PageRequest.of(0, 20));

        verify(matchRepository).findWithFilters(null, null, null, PageRequest.of(0, 20));
    }

    @Test
    void getStats_returnsCorrectCounts() {
        when(profileRepository.count()).thenReturn(100L);
        when(dealRepository.countByStatus(Deal.DealStatus.ACTIVE)).thenReturn(25L);
        when(matchRepository.count()).thenReturn(500L);

        StatsDto stats = matchService.getStats();

        assertThat(stats.totalProfiles()).isEqualTo(100);
        assertThat(stats.totalDeals()).isEqualTo(25);
        assertThat(stats.totalMatches()).isEqualTo(500);
    }

    @Test
    void matchDealToProfiles_expertiseTagMaxCapped_at40() {
        Deal deal = buildDeal("Big Deal", "Other", List.of(), "alpha beta gamma delta epsilon");
        Profile profile = buildProfile("Tags", "Other", List.of("alpha", "beta", "gamma", "delta", "epsilon"), List.of());
        when(dealRepository.findById(deal.getId())).thenReturn(Optional.of(deal));
        when(profileRepository.findAll()).thenReturn(List.of(profile));
        when(matchRepository.save(any())).thenAnswer(inv -> {
            Match m = inv.getArgument(0);
            m.setId(UUID.randomUUID());
            m.setMatchedAt(LocalDateTime.now());
            return m;
        });

        List<MatchDto> results = matchService.matchDealToProfiles(deal.getId());

        // 40 (industry) + 40 (tag cap) = 80 max
        assertThat(results.get(0).relevanceScore().intValue()).isLessThanOrEqualTo(80);
    }
}
