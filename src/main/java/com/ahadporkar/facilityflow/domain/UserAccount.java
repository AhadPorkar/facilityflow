package com.ahadporkar.facilityflow.domain;

import jakarta.persistence.*;
import java.time.*;
import java.util.*;

@Entity
@Table(name = "app_user")
public class UserAccount extends BaseEntity {
    @Column(name = "email")
    private String email;

    @Column(name = "password_hash")
    private String passwordHash;

    @Column(name = "display_name")
    private String displayName;

    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    private Enums.Role role;

    @Column(name = "enabled")
    private Boolean enabled;

    public UserAccount() {}

    public String getEmail() { return email; }
    public void setEmail(String value) { this.email = value; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String value) { this.passwordHash = value; }
    public String getDisplayName() { return displayName; }
    public void setDisplayName(String value) { this.displayName = value; }
    public Enums.Role getRole() { return role; }
    public void setRole(Enums.Role value) { this.role = value; }
    public Boolean getEnabled() { return enabled; }
    public void setEnabled(Boolean value) { this.enabled = value; }
}
