package com.synflow.repository;

import com.synflow.entity.Match;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface MatchRepository extends JpaRepository<Match, UUID> {

    List<Match> findByDealIdOrderByRelevanceScoreDesc(UUID dealId);

    List<Match> findByProfileIdOrderByRelevanceScoreDesc(UUID profileId);

    void deleteByDealId(UUID dealId);

    @Query("SELECT m FROM Match m WHERE " +
           "(:dealId IS NULL OR m.deal.id = :dealId) " +
           "AND (:profileId IS NULL OR m.profile.id = :profileId) " +
           "AND (:minScore IS NULL OR m.relevanceScore >= :minScore)")
    Page<Match> findWithFilters(
            @Param("dealId") UUID dealId,
            @Param("profileId") UUID profileId,
            @Param("minScore") BigDecimal minScore,
            Pageable pageable);
}
