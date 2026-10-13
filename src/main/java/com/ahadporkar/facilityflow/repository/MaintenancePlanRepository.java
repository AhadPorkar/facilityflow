package com.ahadporkar.facilityflow.repository;

import com.ahadporkar.facilityflow.domain.*;
import java.util.*;
import java.time.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;

public interface MaintenancePlanRepository extends JpaRepository<MaintenancePlan, UUID> {
    List<MaintenancePlan> findAllByOrganizationIdOrderByNextDueDateAsc(UUID organizationId);
        Optional<MaintenancePlan> findByIdAndOrganizationId(UUID id, UUID organizationId);
        @Lock(LockModeType.PESSIMISTIC_WRITE)
        @Query("select p from MaintenancePlan p where p.active = true and p.nextDueDate <= :date")
        List<MaintenancePlan> lockDuePlans(@Param("date") LocalDate date);
        long countByOrganizationIdAndActiveTrue(UUID organizationId);
}
