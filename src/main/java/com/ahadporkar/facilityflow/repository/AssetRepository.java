package com.ahadporkar.facilityflow.repository;

import com.ahadporkar.facilityflow.domain.*;
import java.util.*;
import java.time.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;

public interface AssetRepository extends JpaRepository<Asset, UUID> {
    List<Asset> findAllByOrganizationIdOrderByNameAsc(UUID organizationId);
        Optional<Asset> findByIdAndOrganizationId(UUID id,UUID organizationId);
        long countByOrganizationId(UUID organizationId);
        long countByOrganizationIdAndStatus(UUID organizationId, Enums.AssetStatus status);
}
