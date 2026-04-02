package com.synflow.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "profile_connections",
       uniqueConstraints = @UniqueConstraint(columnNames = {"profile_id", "connected_profile_id"}))
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class ProfileConnection {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_id", nullable = false)
    private Profile profile;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "connected_profile_id", nullable = false)
    private Profile connectedProfile;

    @Column(name = "connection_type")
    private String connectionType;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
