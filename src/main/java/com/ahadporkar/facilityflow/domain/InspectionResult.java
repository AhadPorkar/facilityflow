package com.ahadporkar.facilityflow.domain;

import jakarta.persistence.*;
import java.time.*;
import java.util.*;

@Entity
@Table(name = "inspection_result")
public class InspectionResult extends BaseEntity {
    @Column(name = "inspection_id")
    private UUID inspectionId;

    @Column(name = "checklist_item_id")
    private UUID checklistItemId;

    @Column(name = "label_snapshot")
    private String labelSnapshot;

    @Column(name = "position")
    private Integer position;

    @Column(name = "passed")
    private Boolean passed;

    @Column(name = "note")
    private String note;

    public InspectionResult() {}

    public UUID getInspectionId() { return inspectionId; }
    public void setInspectionId(UUID value) { this.inspectionId = value; }
    public UUID getChecklistItemId() { return checklistItemId; }
    public void setChecklistItemId(UUID value) { this.checklistItemId = value; }
    public String getLabelSnapshot() { return labelSnapshot; }
    public void setLabelSnapshot(String value) { this.labelSnapshot = value; }
    public Integer getPosition() { return position; }
    public void setPosition(Integer value) { this.position = value; }
    public Boolean getPassed() { return passed; }
    public void setPassed(Boolean value) { this.passed = value; }
    public String getNote() { return note; }
    public void setNote(String value) { this.note = value; }
}
