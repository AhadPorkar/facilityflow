package com.ahadporkar.facilityflow.repository;

import com.ahadporkar.facilityflow.domain.*;
import java.util.*;
import java.time.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;

public interface AuditEventRepository extends JpaRepository<AuditEvent, UUID> {
    List<AuditEvent> findTop100ByOrganizationIdOrderByCreatedAtDesc(UUID organizationId);
}
