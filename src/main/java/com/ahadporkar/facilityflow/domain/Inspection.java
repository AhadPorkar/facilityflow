package com.ahadporkar.facilityflow.domain;

import jakarta.persistence.*;
import java.time.*;
import java.util.*;

@Entity
@Table(name = "inspection")
public class Inspection extends BaseEntity {
    @Column(name = "template_id")
    private UUID templateId;

    @Column(name = "asset_id")
    private UUID assetId;

    @Column(name = "inspector_user_id")
    private UUID inspectorUserId;

    @Column(name = "performed_at")
    private Instant performedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private Enums.InspectionStatus status;

    @Column(name = "notes")
    private String notes;

    @Column(name = "number")
    private String number;

    public Inspection() {}

    public UUID getTemplateId() { return templateId; }
    public void setTemplateId(UUID value) { this.templateId = value; }
    public UUID getAssetId() { return assetId; }
    public void setAssetId(UUID value) { this.assetId = value; }
    public UUID getInspectorUserId() { return inspectorUserId; }
    public void setInspectorUserId(UUID value) { this.inspectorUserId = value; }
    public Instant getPerformedAt() { return performedAt; }
    public void setPerformedAt(Instant value) { this.performedAt = value; }
    public Enums.InspectionStatus getStatus() { return status; }
    public void setStatus(Enums.InspectionStatus value) { this.status = value; }
    public String getNotes() { return notes; }
    public void setNotes(String value) { this.notes = value; }
    public String getNumber() { return number; }
    public void setNumber(String value) { this.number = value; }
}
