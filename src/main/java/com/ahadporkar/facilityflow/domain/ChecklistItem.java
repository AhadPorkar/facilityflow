package com.ahadporkar.facilityflow.domain;

import jakarta.persistence.*;
import java.time.*;
import java.util.*;

@Entity
@Table(name = "checklist_item")
public class ChecklistItem extends BaseEntity {
    @Column(name = "template_id")
    private UUID templateId;

    @Column(name = "position")
    private Integer position;

    @Column(name = "label")
    private String label;

    @Column(name = "required")
    private Boolean required;

    public ChecklistItem() {}

    public UUID getTemplateId() { return templateId; }
    public void setTemplateId(UUID value) { this.templateId = value; }
    public Integer getPosition() { return position; }
    public void setPosition(Integer value) { this.position = value; }
    public String getLabel() { return label; }
    public void setLabel(String value) { this.label = value; }
    public Boolean getRequired() { return required; }
    public void setRequired(Boolean value) { this.required = value; }
}
