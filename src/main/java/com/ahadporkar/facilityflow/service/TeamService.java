package com.ahadporkar.facilityflow.service;

import com.ahadporkar.facilityflow.api.*;
import com.ahadporkar.facilityflow.domain.UserAccount;
import com.ahadporkar.facilityflow.repository.UserAccountRepository;
import com.ahadporkar.facilityflow.security.CurrentUser;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class TeamService {
    public record UserView(UUID id,String displayName,String email,String role,boolean enabled) {
        public static UserView of(UserAccount u) {
            return new UserView(u.getId(),u.getDisplayName(),u.getEmail(),u.getRole().name(),Boolean.TRUE.equals(u.getEnabled()));
        }
    }
    private final UserAccountRepository users;
    private final CurrentUser current;
    private final PasswordEncoder encoder;
    private final AuditService audit;
    public TeamService(UserAccountRepository users,CurrentUser current,PasswordEncoder encoder,AuditService audit) {
        this.users=users;this.current=current;this.encoder=encoder;this.audit=audit;
    }
    @Transactional(readOnly=true)
    public List<UserView> team() {
        return users.findAllByOrganizationIdOrderByDisplayNameAsc(current.organizationId())
            .stream().map(UserView::of).toList();
    }
    @Transactional
    public UserView create(Requests.UserCreate r) {
        String email=r.email().strip().toLowerCase(Locale.ROOT);
        if (users.findByEmailIgnoreCase(email).isPresent())
            throw new ApiException(HttpStatus.CONFLICT,"DUPLICATE_EMAIL","Account already exists");
        UserAccount u=new UserAccount(); u.setOrganizationId(current.organizationId());
        u.setDisplayName(r.displayName().strip()); u.setEmail(email);
        u.setRole(r.role()); u.setEnabled(true); u.setPasswordHash(encoder.encode(r.password()));
        users.saveAndFlush(u); audit.record("USER",u.getId(),"CREATED","Team member provisioned");
        return UserView.of(u);
    }
}
