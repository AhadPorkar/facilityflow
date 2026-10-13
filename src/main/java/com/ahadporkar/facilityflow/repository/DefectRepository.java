package com.ahadporkar.facilityflow.repository;

import com.ahadporkar.facilityflow.domain.*;
import java.util.*;
import java.time.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;

public interface DefectRepository extends JpaRepository<Defect, UUID> {
    List<Defect> findAllByOrganizationIdOrderByCreatedAtDesc(UUID organizationId);
        Optional<Defect> findByIdAndOrganizationId(UUID id,UUID organizationId);
        List<Defect> findAllByInspectionIdOrderByCreatedAtAsc(UUID inspectionId);
    List<Defect> findAllByCorrectiveWorkOrderIdAndStatus(UUID workOrderId, Enums.DefectStatus status);
    long countByOrganizationIdAndStatus(UUID organizationId, Enums.DefectStatus status);
}
