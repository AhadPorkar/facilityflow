package com.ahadporkar.facilityflow.repository;

import com.ahadporkar.facilityflow.domain.*;
import java.util.*;
import java.time.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;

public interface InspectionRepository extends JpaRepository<Inspection, UUID> {
    List<Inspection> findAllByOrganizationIdOrderByPerformedAtDesc(UUID organizationId);
        Optional<Inspection> findByIdAndOrganizationId(UUID id,UUID organizationId);
}
