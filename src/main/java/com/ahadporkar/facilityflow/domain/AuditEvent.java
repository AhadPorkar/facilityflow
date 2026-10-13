package com.ahadporkar.facilityflow.domain;

import jakarta.persistence.*;
import java.time.*;
import java.util.*;

@Entity
@Table(name = "audit_event")
public class AuditEvent extends BaseEntity {
    @Column(name = "actor_user_id")
    private UUID actorUserId;

    @Column(name = "entity_type")
    private String entityType;

    @Column(name = "entity_id")
    private UUID entityId;

    @Column(name = "action")
    private String action;

    @Column(name = "description")
    private String description;

    public AuditEvent() {}

    public UUID getActorUserId() { return actorUserId; }
    public void setActorUserId(UUID value) { this.actorUserId = value; }
    public String getEntityType() { return entityType; }
    public void setEntityType(String value) { this.entityType = value; }
    public UUID getEntityId() { return entityId; }
    public void setEntityId(UUID value) { this.entityId = value; }
    public String getAction() { return action; }
    public void setAction(String value) { this.action = value; }
    public String getDescription() { return description; }
    public void setDescription(String value) { this.description = value; }
}
