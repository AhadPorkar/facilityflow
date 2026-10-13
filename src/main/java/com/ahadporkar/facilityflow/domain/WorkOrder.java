package com.ahadporkar.facilityflow.domain;

import jakarta.persistence.*;
import java.time.*;
import java.util.*;

@Entity
@Table(name = "work_order")
public class WorkOrder extends BaseEntity {
    @Column(name = "site_id")
    private UUID siteId;

    @Column(name = "asset_id")
    private UUID assetId;

    @Column(name = "maintenance_plan_id")
    private UUID maintenancePlanId;

    @Column(name = "scheduled_for")
    private LocalDate scheduledFor;

    @Column(name = "number")
    private String number;

    @Column(name = "title")
    private String title;

    @Column(name = "description")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private Enums.WorkStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority")
    private Enums.Priority priority;

    @Column(name = "assigned_to_user_id")
    private UUID assignedToUserId;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Column(name = "resolution")
    private String resolution;

    @Column(name = "started_at")
    private Instant startedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(name = "approved_at")
    private Instant approvedAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @Column(name = "created_by_user_id")
    private UUID createdByUserId;

    public WorkOrder() {}

    public UUID getSiteId() { return siteId; }
    public void setSiteId(UUID value) { this.siteId = value; }
    public UUID getAssetId() { return assetId; }
    public void setAssetId(UUID value) { this.assetId = value; }
    public UUID getMaintenancePlanId() { return maintenancePlanId; }
    public void setMaintenancePlanId(UUID value) { this.maintenancePlanId = value; }
    public LocalDate getScheduledFor() { return scheduledFor; }
    public void setScheduledFor(LocalDate value) { this.scheduledFor = value; }
    public String getNumber() { return number; }
    public void setNumber(String value) { this.number = value; }
    public String getTitle() { return title; }
    public void setTitle(String value) { this.title = value; }
    public String getDescription() { return description; }
    public void setDescription(String value) { this.description = value; }
    public Enums.WorkStatus getStatus() { return status; }
    public void setStatus(Enums.WorkStatus value) { this.status = value; }
    public Enums.Priority getPriority() { return priority; }
    public void setPriority(Enums.Priority value) { this.priority = value; }
    public UUID getAssignedToUserId() { return assignedToUserId; }
    public void setAssignedToUserId(UUID value) { this.assignedToUserId = value; }
    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate value) { this.dueDate = value; }
    public String getResolution() { return resolution; }
    public void setResolution(String value) { this.resolution = value; }
    public Instant getStartedAt() { return startedAt; }
    public void setStartedAt(Instant value) { this.startedAt = value; }
    public Instant getCompletedAt() { return completedAt; }
    public void setCompletedAt(Instant value) { this.completedAt = value; }
    public Instant getApprovedAt() { return approvedAt; }
    public void setApprovedAt(Instant value) { this.approvedAt = value; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant value) { this.updatedAt = value; }
    public UUID getCreatedByUserId() { return createdByUserId; }
    public void setCreatedByUserId(UUID value) { this.createdByUserId = value; }
}
