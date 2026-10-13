package com.ahadporkar.facilityflow.repository;

import com.ahadporkar.facilityflow.domain.*;
import java.util.*;
import java.time.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;

public interface WorkOrderRepository extends JpaRepository<WorkOrder, UUID> {
    List<WorkOrder> findAllByOrganizationIdOrderByCreatedAtDesc(UUID organizationId);
        Optional<WorkOrder> findByIdAndOrganizationId(UUID id,UUID organizationId);
        long countByOrganizationIdAndStatus(UUID organizationId, Enums.WorkStatus status);
        long countByOrganizationIdAndDueDateBeforeAndStatusNotIn(UUID organizationId, LocalDate dueDate,Collection<Enums.WorkStatus> statuses);
        boolean existsByMaintenancePlanIdAndScheduledFor(UUID maintenancePlanId,LocalDate scheduledFor);
}
