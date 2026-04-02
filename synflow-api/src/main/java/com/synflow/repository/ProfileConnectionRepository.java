package com.synflow.repository;

import com.synflow.entity.ProfileConnection;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ProfileConnectionRepository extends JpaRepository<ProfileConnection, UUID> {
    List<ProfileConnection> findByProfileId(UUID profileId);
    List<ProfileConnection> findByProfileIdOrConnectedProfileId(UUID profileId, UUID connectedProfileId);
}
