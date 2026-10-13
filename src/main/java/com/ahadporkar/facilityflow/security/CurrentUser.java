package com.ahadporkar.facilityflow.security;

import com.ahadporkar.facilityflow.api.ApiException;
import com.ahadporkar.facilityflow.domain.UserAccount;
import com.ahadporkar.facilityflow.repository.UserAccountRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import java.util.UUID;

@Component
public class CurrentUser {
    private final UserAccountRepository users;
    public CurrentUser(UserAccountRepository users) { this.users = users; }
    public UserAccount get() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!(principal instanceof PrincipalUser user)) throw ApiException.forbidden();
        UserAccount account = users.findById(user.userId()).orElseThrow(ApiException::forbidden);
        if (!Boolean.TRUE.equals(account.getEnabled())) throw ApiException.forbidden();
        return account;
    }
    public UUID organizationId() { return get().getOrganizationId(); }
    public UUID userId() { return get().getId(); }
    public boolean isManager() {
        var role = get().getRole();
        return role == com.ahadporkar.facilityflow.domain.Enums.Role.ADMIN ||
               role == com.ahadporkar.facilityflow.domain.Enums.Role.MANAGER;
    }
}
