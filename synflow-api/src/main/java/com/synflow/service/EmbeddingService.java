package com.synflow.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.synflow.entity.Deal;
import com.synflow.entity.Profile;
import com.synflow.repository.DealRepository;
import com.synflow.repository.ProfileRepository;
import com.synflow.util.EncryptionUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmbeddingService {

    private final JdbcTemplate jdbcTemplate;
    private final ProfileRepository profileRepository;
    private final DealRepository dealRepository;
    private final EncryptionUtil encryptionUtil;
    private final ObjectMapper objectMapper;

    @Value("${app.openai.api-key}")
    private String apiKey;

    @Value("${app.openai.embedding-model}")
    private String embeddingModel;

    @Value("${app.openai.base-url}")
    private String baseUrl;

    public void embedProfile(Profile profile) {
        String text = profileText(profile);
        float[] vector = embed(text);
        if (vector == null) return;
        jdbcTemplate.update(
                "UPDATE profiles SET embedding = ?::vector WHERE id = ?",
                toVectorLiteral(vector), profile.getId());
    }

    public void embedDeal(Deal deal) {
        String text = dealText(deal);
        float[] vector = embed(text);
        if (vector == null) return;
        jdbcTemplate.update(
                "UPDATE deals SET embedding = ?::vector WHERE id = ?",
                toVectorLiteral(vector), deal.getId());
    }

    public void ensureDealEmbedding(Deal deal) {
        Integer present = jdbcTemplate.queryForObject(
                "SELECT CASE WHEN embedding IS NULL THEN 0 ELSE 1 END FROM deals WHERE id = ?",
                Integer.class, deal.getId());
        if (present != null && present == 1) return;
        embedDeal(deal);
    }

    /** Returns profile id → cosine similarity in [0,1]. Empty if deal has no embedding. */
    public Map<UUID, Double> findSimilaritiesForDeal(UUID dealId) {
        Map<UUID, Double> result = new HashMap<>();
        jdbcTemplate.query("""
                SELECT p.id, 1 - (p.embedding <=> d.embedding) AS similarity
                FROM profiles p, deals d
                WHERE d.id = ?
                  AND p.embedding IS NOT NULL
                  AND d.embedding IS NOT NULL
                """,
                rs -> {
                    UUID id = (UUID) rs.getObject("id");
                    double sim = rs.getDouble("similarity");
                    result.put(id, sim);
                },
                dealId);
        return result;
    }

    /** Backfill: re-embeds every profile and deal. Returns count of rows updated. */
    public int reindexAll() {
        int count = 0;
        for (Profile p : profileRepository.findAll()) {
            embedProfile(p);
            count++;
        }
        for (Deal d : dealRepository.findAll()) {
            embedDeal(d);
            count++;
        }
        return count;
    }

    private String profileText(Profile p) {
        StringBuilder sb = new StringBuilder();
        append(sb, p.getName());
        append(sb, p.getIndustryFocus());
        if (p.getExpertise() != null) append(sb, String.join(", ", p.getExpertise()));
        append(sb, p.getSummary());
        append(sb, encryptionUtil.decrypt(p.getServicesOffered()));
        append(sb, encryptionUtil.decrypt(p.getTrackRecord()));
        if (p.getGeographicReach() != null) append(sb, String.join(", ", p.getGeographicReach()));
        return sb.toString();
    }

    private String dealText(Deal d) {
        StringBuilder sb = new StringBuilder();
        append(sb, d.getTitle());
        append(sb, d.getIndustry());
        if (d.getDealType() != null) append(sb, d.getDealType().name());
        append(sb, d.getRequirements());
        if (d.getGeography() != null) append(sb, String.join(", ", d.getGeography()));
        return sb.toString();
    }

    private void append(StringBuilder sb, String s) {
        if (s == null || s.isBlank()) return;
        if (sb.length() > 0) sb.append(" | ");
        sb.append(s);
    }

    private float[] embed(String text) {
        if (text == null || text.isBlank()) return null;
        try {
            RestClient client = RestClient.builder()
                    .baseUrl(baseUrl)
                    .defaultHeader("Authorization", "Bearer " + apiKey)
                    .defaultHeader("Content-Type", "application/json")
                    .build();

            Map<String, Object> body = Map.of(
                    "model", embeddingModel,
                    "input", text);

            String response = client.post()
                    .uri("/v1/embeddings")
                    .body(body)
                    .retrieve()
                    .body(String.class);

            JsonNode node = objectMapper.readTree(response).at("/data/0/embedding");
            if (!node.isArray()) return null;
            float[] out = new float[node.size()];
            for (int i = 0; i < node.size(); i++) out[i] = (float) node.get(i).asDouble();
            return out;
        } catch (Exception e) {
            log.warn("OpenAI embedding call failed: {}", e.getMessage());
            return null;
        }
    }

    private String toVectorLiteral(float[] v) {
        StringBuilder sb = new StringBuilder(v.length * 8);
        sb.append('[');
        for (int i = 0; i < v.length; i++) {
            if (i > 0) sb.append(',');
            sb.append(String.format(Locale.US, "%.6f", v[i]));
        }
        sb.append(']');
        return sb.toString();
    }
}
