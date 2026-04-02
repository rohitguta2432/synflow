package com.synflow.repository;

import com.synflow.entity.Deal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface DealRepository extends JpaRepository<Deal, UUID> {

    @Query("SELECT d FROM Deal d WHERE " +
           "(:search IS NULL OR LOWER(d.title) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(d.industry) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "AND (:industry IS NULL OR LOWER(d.industry) = LOWER(:industry)) " +
           "AND (:dealType IS NULL OR d.dealType = :dealType) " +
           "AND (:status IS NULL OR d.status = :status)")
    Page<Deal> findWithFilters(
            @Param("search") String search,
            @Param("industry") String industry,
            @Param("dealType") Deal.DealType dealType,
            @Param("status") Deal.DealStatus status,
            Pageable pageable);

    long countByStatus(Deal.DealStatus status);
}
