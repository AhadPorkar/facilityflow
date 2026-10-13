package com.ahadporkar.facilityflow.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "organization")
public class Organization {
    @Id private UUID id = UUID.randomUUID();
    @Column(nullable = false) private String name;
    @Column(name = "created_at", nullable = false) private Instant createdAt = Instant.now();
    public Organization() {}
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
