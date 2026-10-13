package com.ahadporkar.facilityflow.security;

import com.ahadporkar.facilityflow.domain.UserAccount;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public class PrincipalUser implements UserDetails {
    private final UUID userId;
    private final UUID organizationId;
    private final String email;
    private final String password;
    private final boolean enabled;
    private final String role;

    public PrincipalUser(UserAccount user) {
        userId = user.getId(); organizationId = user.getOrganizationId();
        email = user.getEmail(); password = user.getPasswordHash();
        enabled = Boolean.TRUE.equals(user.getEnabled()); role = user.getRole().name();
    }
    public UUID userId() { return userId; }
    public UUID organizationId() { return organizationId; }
    public String role() { return role; }
    @Override public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_"+role));
    }
    @Override public String getUsername() { return email; }
    @Override public String getPassword() { return password; }
    @Override public boolean isEnabled() { return enabled; }
}
