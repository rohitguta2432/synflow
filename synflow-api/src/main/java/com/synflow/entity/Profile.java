package com.synflow.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "profiles")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Profile {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "unique_code", unique = true, nullable = false)
    private String uniqueCode;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProfileType type;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(columnDefinition = "text[]")
    private List<String> expertise;

    @Column(name = "services_offered")
    private String servicesOffered;

    @Column(name = "industry_focus")
    private String industryFocus;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "geographic_reach", columnDefinition = "text[]")
    private List<String> geographicReach;

    @Column(name = "track_record")
    private String trackRecord;

    @Enumerated(EnumType.STRING)
    @Column(name = "contact_status")
    private ContactStatus contactStatus;

    private String summary;

    @Column(name = "ai_generated")
    private Boolean aiGenerated;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @OneToMany(mappedBy = "profile", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProfileConnection> connections;

    @OneToMany(mappedBy = "profile", cascade = CascadeType.ALL)
    private List<Match> matches;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum ProfileType {
        REAL, SHADOW
    }

    public enum ContactStatus {
        ACTIVE, EXTERNAL, NOT_ONBOARDED
    }
}
