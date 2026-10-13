package com.ahadporkar.facilityflow.service;

import com.ahadporkar.facilityflow.domain.AuditEvent;
import com.ahadporkar.facilityflow.repository.AuditEventRepository;
import com.ahadporkar.facilityflow.security.CurrentUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.UUID;

@Service
public class AuditService {
    private final AuditEventRepository events;
    private final CurrentUser current;
    public AuditService(AuditEventRepository events, CurrentUser current) {
        this.events=events;this.current=current;
    }
    /** Event-only log. Avoid PII, credentials or untrusted document contents in descriptions. */
    public void record(UUID orgId, UUID actorId, String type, UUID entityId, String action, String description) {
        AuditEvent event = new AuditEvent(); event.setOrganizationId(orgId);
        event.setActorUserId(actorId); event.setEntityType(type);
        event.setEntityId(entityId); event.setAction(action); event.setDescription(description);
        events.save(event);
    }
    public void record(String type,UUID entityId,String action,String description) {
        record(current.organizationId(),current.userId(),type,entityId,action,description);
    }
    @Transactional(readOnly=true)
    public List<AuditEvent> recent() {
        return events.findTop100ByOrganizationIdOrderByCreatedAtDesc(current.organizationId());
    }
}
