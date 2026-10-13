package com.ahadporkar.facilityflow.domain;

import jakarta.persistence.*;
import java.time.*;
import java.util.*;

@Entity
@Table(name = "maintenance_plan")
public class MaintenancePlan extends BaseEntity {
    @Column(name = "asset_id")
    private UUID assetId;

    @Column(name = "title")
    private String title;

    @Column(name = "interval_days")
    private Integer intervalDays;

    @Column(name = "next_due_date")
    private LocalDate nextDueDate;

    @Column(name = "instructions")
    private String instructions;

    @Column(name = "assigned_to_user_id")
    private UUID assignedToUserId;

    @Column(name = "active")
    private Boolean active;

    public MaintenancePlan() {}

    public UUID getAssetId() { return assetId; }
    public void setAssetId(UUID value) { this.assetId = value; }
    public String getTitle() { return title; }
    public void setTitle(String value) { this.title = value; }
    public Integer getIntervalDays() { return intervalDays; }
    public void setIntervalDays(Integer value) { this.intervalDays = value; }
    public LocalDate getNextDueDate() { return nextDueDate; }
    public void setNextDueDate(LocalDate value) { this.nextDueDate = value; }
    public String getInstructions() { return instructions; }
    public void setInstructions(String value) { this.instructions = value; }
    public UUID getAssignedToUserId() { return assignedToUserId; }
    public void setAssignedToUserId(UUID value) { this.assignedToUserId = value; }
    public Boolean getActive() { return active; }
    public void setActive(Boolean value) { this.active = value; }
}
