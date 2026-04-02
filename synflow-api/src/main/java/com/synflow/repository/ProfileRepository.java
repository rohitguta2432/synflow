package com.synflow.repository;

import com.synflow.entity.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface ProfileRepository extends JpaRepository<Profile, UUID> {

    @Query("SELECT p FROM Profile p WHERE " +
           "(:search IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(p.industryFocus) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "AND (:industry IS NULL OR LOWER(p.industryFocus) = LOWER(:industry)) " +
           "AND (:type IS NULL OR p.type = :type) " +
           "AND (:status IS NULL OR p.contactStatus = :status)")
    Page<Profile> findWithFilters(
            @Param("search") String search,
            @Param("industry") String industry,
            @Param("type") Profile.ProfileType type,
            @Param("status") Profile.ContactStatus status,
            Pageable pageable);

    long count();

    boolean existsByUniqueCode(String uniqueCode);
}
