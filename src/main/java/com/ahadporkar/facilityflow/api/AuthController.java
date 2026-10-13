package com.ahadporkar.facilityflow.api;

import com.ahadporkar.facilityflow.domain.UserAccount;
import com.ahadporkar.facilityflow.repository.OrganizationRepository;
import com.ahadporkar.facilityflow.security.CurrentUser;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    public record Account(UUID id,String name,String email,String role,String organization) {}
    private final AuthenticationManager authentication;
    private final SecurityContextRepository contexts;
    private final CurrentUser current;
    private final OrganizationRepository organizations;
    public AuthController(AuthenticationManager authentication, SecurityContextRepository contexts,
                          CurrentUser current, OrganizationRepository organizations) {
        this.authentication=authentication; this.contexts=contexts;
        this.current=current; this.organizations=organizations;
    }
    @GetMapping("/csrf")
    public Map<String,String> csrf(CsrfToken token) { return Map.of("token",token.getToken()); }
    @PostMapping("/login")
    public Account login(@Valid @RequestBody Requests.Login request,
                         HttpServletRequest servletRequest, HttpServletResponse response) {
        try {
            var authenticated = authentication.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(),request.password()));
            servletRequest.getSession(true);
            servletRequest.changeSessionId();
            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(authenticated);
            SecurityContextHolder.setContext(context);
            contexts.saveContext(context,servletRequest,response);
            return me();
        } catch (BadCredentialsException ex) {
            throw new ApiException(HttpStatus.UNAUTHORIZED,"INVALID_CREDENTIALS","Invalid email or password");
        }
    }
    @GetMapping("/me")
    public Account me() {
        UserAccount user=current.get();
        String org=organizations.findById(user.getOrganizationId())
          .orElseThrow(() -> ApiException.notFound("Organization")).getName();
        return new Account(user.getId(),user.getDisplayName(),user.getEmail(),user.getRole().name(),org);
    }
    @PostMapping("/logout")
    public Map<String,Boolean> logout(HttpServletRequest request, HttpServletResponse response) {
        new SecurityContextLogoutHandler().logout(request,response,SecurityContextHolder.getContext().getAuthentication());
        return Map.of("success",true);
    }
}
