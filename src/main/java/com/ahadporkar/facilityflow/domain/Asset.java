package com.ahadporkar.facilityflow.domain;

import jakarta.persistence.*;
import java.time.*;
import java.util.*;

@Entity
@Table(name = "asset")
public class Asset extends BaseEntity {
    @Column(name = "site_id")
    private UUID siteId;

    @Column(name = "code")
    private String code;

    @Column(name = "name")
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "type")
    private Enums.AssetType type;

    @Column(name = "location")
    private String location;

    @Column(name = "manufacturer")
    private String manufacturer;

    @Column(name = "serial_number")
    private String serialNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private Enums.AssetStatus status;

    @Column(name = "installed_on")
    private LocalDate installedOn;

    public Asset() {}

    public UUID getSiteId() { return siteId; }
    public void setSiteId(UUID value) { this.siteId = value; }
    public String getCode() { return code; }
    public void setCode(String value) { this.code = value; }
    public String getName() { return name; }
    public void setName(String value) { this.name = value; }
    public Enums.AssetType getType() { return type; }
    public void setType(Enums.AssetType value) { this.type = value; }
    public String getLocation() { return location; }
    public void setLocation(String value) { this.location = value; }
    public String getManufacturer() { return manufacturer; }
    public void setManufacturer(String value) { this.manufacturer = value; }
    public String getSerialNumber() { return serialNumber; }
    public void setSerialNumber(String value) { this.serialNumber = value; }
    public Enums.AssetStatus getStatus() { return status; }
    public void setStatus(Enums.AssetStatus value) { this.status = value; }
    public LocalDate getInstalledOn() { return installedOn; }
    public void setInstalledOn(LocalDate value) { this.installedOn = value; }
}
