package com.ahadporkar.facilityflow.domain;

import jakarta.persistence.*;
import java.time.*;
import java.util.*;

@Entity
@Table(name = "inspection_template")
public class InspectionTemplate extends BaseEntity {
    @Column(name = "name")
    private String name;

    @Column(name = "description")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "asset_type")
    private Enums.AssetType assetType;

    @Column(name = "active")
    private Boolean active;

    public InspectionTemplate() {}

    public String getName() { return name; }
    public void setName(String value) { this.name = value; }
    public String getDescription() { return description; }
    public void setDescription(String value) { this.description = value; }
    public Enums.AssetType getAssetType() { return assetType; }
    public void setAssetType(Enums.AssetType value) { this.assetType = value; }
    public Boolean getActive() { return active; }
    public void setActive(Boolean value) { this.active = value; }
}
