package com.ahadporkar.facilityflow.domain;

import jakarta.persistence.*;
import java.time.*;
import java.util.*;

@Entity
@Table(name = "defect")
public class Defect extends BaseEntity {
    @Column(name = "asset_id")
    private UUID assetId;

    @Column(name = "inspection_id")
    private UUID inspectionId;

    @Column(name = "corrective_work_order_id")
    private UUID correctiveWorkOrderId;

    @Column(name = "title")
    private String title;

    @Column(name = "details")
    private String details;

    @Enumerated(EnumType.STRING)
    @Column(name = "severity")
    private Enums.Priority severity;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private Enums.DefectStatus status;

    @Column(name = "resolved_at")
    private Instant resolvedAt;

    public Defect() {}

    public UUID getAssetId() { return assetId; }
    public void setAssetId(UUID value) { this.assetId = value; }
    public UUID getInspectionId() { return inspectionId; }
    public void setInspectionId(UUID value) { this.inspectionId = value; }
    public UUID getCorrectiveWorkOrderId() { return correctiveWorkOrderId; }
    public void setCorrectiveWorkOrderId(UUID value) { this.correctiveWorkOrderId = value; }
    public String getTitle() { return title; }
    public void setTitle(String value) { this.title = value; }
    public String getDetails() { return details; }
    public void setDetails(String value) { this.details = value; }
    public Enums.Priority getSeverity() { return severity; }
    public void setSeverity(Enums.Priority value) { this.severity = value; }
    public Enums.DefectStatus getStatus() { return status; }
    public void setStatus(Enums.DefectStatus value) { this.status = value; }
    public Instant getResolvedAt() { return resolvedAt; }
    public void setResolvedAt(Instant value) { this.resolvedAt = value; }
}
