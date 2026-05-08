package com.synflow.service;

import com.synflow.dto.MatchDto;
import com.synflow.dto.StatsDto;
import com.synflow.entity.Deal;
import com.synflow.entity.Match;
import com.synflow.entity.Profile;
import com.synflow.repository.DealRepository;
import com.synflow.repository.MatchRepository;
import com.synflow.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

@Service
@RequiredArgsConstructor
public class MatchService {

    private final MatchRepository matchRepository;
    private final DealRepository dealRepository;
    private final ProfileRepository profileRepository;
    private final EmbeddingService embeddingService;

    // Cosine similarity below this baseline gets no bonus — OpenAI embeddings
    // for unrelated text typically sit around 0.5–0.7, so we only reward genuine
    // semantic closeness.
    private static final double SEMANTIC_BASELINE = 0.70;
    private static final double SEMANTIC_MAX_BONUS = 30.0;
    private static final int FINAL_SCORE_CAP = 100;

    @Transactional
    public List<MatchDto> matchDealToProfiles(UUID dealId) {
        Deal deal = dealRepository.findById(dealId)
                .orElseThrow(() -> new RuntimeException("Deal not found"));

        // Make sure the deal has an embedding so semantic similarity can run.
        // Best-effort: if OpenAI is unreachable, embeddings stay null and the
        // similarity map is empty, leaving the rule-based score in charge.
        embeddingService.ensureDealEmbedding(deal);
        Map<UUID, Double> similarities = Optional
                .ofNullable(embeddingService.findSimilaritiesForDeal(dealId))
                .orElse(Map.of());

        // Clear previous matches for this deal
        matchRepository.deleteByDealId(dealId);

        List<Profile> allProfiles = profileRepository.findAll();
        List<MatchResult> results = new ArrayList<>();

        for (Profile profile : allProfiles) {
            int score = 0;
            List<String> reasons = new ArrayList<>();

            // Industry match (40 points)
            if (deal.getIndustry().equalsIgnoreCase(profile.getIndustryFocus())) {
                score += 40;
                reasons.add(profile.getIndustryFocus() + " (industry match)");
            } else if (profile.getExpertise() != null && profile.getExpertise().stream()
                    .anyMatch(tag -> tag.toLowerCase().contains(deal.getIndustry().toLowerCase()))) {
                score += 20;
                reasons.add(deal.getIndustry() + " (partial industry match via expertise)");
            }

            // Expertise tag overlap (10 points each, max 40)
            int tagScore = 0;
            if (profile.getExpertise() != null) {
                String searchText = ((deal.getRequirements() != null ? deal.getRequirements() : "") + " " +
                        deal.getTitle()).toLowerCase();
                for (String tag : profile.getExpertise()) {
                    if (searchText.contains(tag.toLowerCase())) {
                        tagScore += 10;
                        reasons.add(tag + " (expertise)");
                    }
                    if (tagScore >= 40) break;
                }
            }
            score += tagScore;

            // Geography overlap (10 points each, max 20)
            int geoScore = 0;
            if (profile.getGeographicReach() != null && deal.getGeography() != null) {
                for (String region : profile.getGeographicReach()) {
                    if (deal.getGeography().stream()
                            .anyMatch(g -> g.equalsIgnoreCase(region))) {
                        geoScore += 10;
                        reasons.add(region + " (geography)");
                    }
                    if (geoScore >= 20) break;
                }
            }
            score += geoScore;

            // Semantic bonus from embedding similarity (0–30 points, capped at 100 total).
            Double similarity = similarities.get(profile.getId());
            if (similarity != null && similarity > SEMANTIC_BASELINE) {
                double scaled = (similarity - SEMANTIC_BASELINE)
                        / (1.0 - SEMANTIC_BASELINE) * SEMANTIC_MAX_BONUS;
                int bonus = (int) Math.min(SEMANTIC_MAX_BONUS, scaled);
                if (bonus > 0) {
                    score = Math.min(FINAL_SCORE_CAP, score + bonus);
                    reasons.add("semantic similarity " + Math.round(similarity * 100) + "%");
                }
            }

            if (score > 0) {
                results.add(new MatchResult(profile, score,
                        "Matched on: " + String.join(", ", reasons)));
            }
        }

        // Sort and limit to top 20
        results.sort(Comparator.comparingInt(MatchResult::score).reversed());
        List<MatchResult> topResults = results.stream().limit(20).toList();

        // Save matches
        List<Match> savedMatches = new ArrayList<>();
        for (MatchResult result : topResults) {
            Match match = Match.builder()
                    .deal(deal)
                    .profile(result.profile())
                    .relevanceScore(BigDecimal.valueOf(result.score()))
                    .matchReason(result.reason())
                    .build();
            savedMatches.add(matchRepository.save(match));
        }

        return savedMatches.stream()
                .map(m -> new MatchDto(m.getId(), deal.getId(), deal.getTitle(), deal.getIndustry(),
                        m.getProfile().getId(), m.getProfile().getName(),
                        m.getProfile().getExpertise(),
                        m.getRelevanceScore(), m.getMatchReason(), m.getMatchedAt()))
                .toList();
    }

    public Page<MatchDto> findAll(UUID dealId, UUID profileId, BigDecimal minScore, Pageable pageable) {
        return matchRepository.findWithFilters(dealId, profileId, minScore, pageable)
                .map(m -> new MatchDto(m.getId(), m.getDeal().getId(), m.getDeal().getTitle(),
                        m.getDeal().getIndustry(), m.getProfile().getId(), m.getProfile().getName(),
                        m.getProfile().getExpertise(),
                        m.getRelevanceScore(), m.getMatchReason(), m.getMatchedAt()));
    }

    public StatsDto getStats() {
        return new StatsDto(
                profileRepository.count(),
                dealRepository.countByStatus(Deal.DealStatus.ACTIVE),
                matchRepository.count()
        );
    }

    private record MatchResult(Profile profile, int score, String reason) {}
}
