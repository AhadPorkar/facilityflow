# Security Model, Threats & Hardening

[Deutsch](../de/SECURITY.md) · [Index](INDEX.md)

## Scope and assurance statement

This is an educational/reference implementation with security-conscious defaults. It is **not penetration-tested, certified, or production-hardened**. Running an internet-accessible instance with real user data requires the controls in the last section, privacy review, and organization-specific policies. The public GitHub Pages preview is **not an authenticated system** and deliberately stores synthetic data in browser local storage.

## Authentication and session lifecycle

- Passwords are encoded with **BCrypt** at cost 12, and raw hashes are not returned in the `/api/team` response (`UserView`).
- `UserDetailsService` loads a user by email; the model has a global unique email constraint. Account must be enabled.
- Login uses Spring Security authentication manager, regenerates the HTTP session ID, installs a security context and persists it with `HttpSessionSecurityContextRepository`.
- Session cookie: HTTP-only, SameSite Lax, 45-minute timeout, Secure flag configured with `SESSION_COOKIE_SECURE=true` for HTTPS.
- Logout invalidates the session via Spring's security-context logout handler.
- No self-service public registration exists; only `ADMIN` may provision user accounts.
- **Missing controls:** email verification, password reset, lockout/rate limit, MFA, SSO/OIDC, forced password rotation for known first-login secrets, device sessions, administrative disable/deletion endpoints.

## CSRF and browser boundaries

The same-origin UI uses Spring Security's `CookieCsrfTokenRepository.withHttpOnlyFalse()` with a `CsrfTokenRequestAttributeHandler`. A GET request to `/api/auth/csrf` issues a token; front end sends cookie `XSRF-TOKEN` in header `X-XSRF-TOKEN` on every write request and refreshes it after login. This readable **CSRF token cookie is not the authentication session cookie**. State-changing APIs remain protected even if a browser's cookie is attached to a forged form submission.

CSP restricts scripts/styles/images to local assets, prohibits frames and object embedding, and restricts form submissions to self. `X-Content-Type-Options` and other protective headers are managed by Spring Security/Caddy. Dynamic HTML data is escaped with `utils.e()` in the UI, and user-entered form data is treated as text. Periodic dependency scanning and CSP audit are still needed.

## Authorization: role vs resource scope

Every `/api/**` route (except the two authentication setup operations) requires authentication. Controller method authorization via `@PreAuthorize` checks business role. `CurrentUser` resolves account ID and organization ID from the authenticated principal. Service methods then load resources using repository methods such as `findByIdAndOrganizationId` and validate referenced site/asset/assignee ownership.

**Why both?** A manager with a valid session must *still not* be able to access another customer's asset by guessing a UUID. Work-order execution additionally checks the technician's assignment. PDF endpoints load the underlying record through the same tenant-scoped service, so a report URL is not a bypass.

**Database consideration:** physical child FKs currently reference target `id`, not `(organization_id,id)`. Service-level checks enforce the tenant equality. A production defense-in-depth migration could add composite FKs or PostgreSQL row-level security, but that is **not** implemented here.

## Threat-to-control map

| Threat | Implemented control | Remaining risk / recommended action |
|---|---|---|
| Unauthorized API access | Session requirement and role checks | Add brute-force limits, SSO/MFA, alerting |
| Broken object-level authorization | Tenant-filtered reads and reference checks | Add automated security property tests, composite FKs/RLS |
| CSRF against cookie session | Token cookie + header + session rotation | Review reverse-proxy behavior and same-site config |
| XSS from record content | Escaped dynamic text, CSP, no external JS CDN | Browser-based DOM XSS audit and dependency audit |
| Duplicate job execution | DB unique constraint + pessimistic row locking | Multi-instance resilience / lock timeout monitoring |
| SQL injection | Spring Data JPA parameterized queries | Review any future native query endpoints |
| Secret leakage | `.env` ignored; sample placeholders; env-driven setup | Add a vault, rotation and secret scanning |
| Database exposure | Internal Compose network, no public 5432 | Firewall, TLS, least privilege, patching |
| Loss of operational records | PostgreSQL volume | Encrypted off-host backups and restore drills not bundled |
| Malicious user edits to audit | Append-oriented service event writes | Tamper-evident remote/WORM audit store not bundled |
| Excessive personal data | Team views omit hashes; tenant scoping | GDPR DPIA if needed, retention/deletion policies, DPA |
| Denial of service | Limited endpoint surface, validation, Hikari pool | Rate limiting, pagination, request body limits, quotas |

## Data privacy and German requirements

Names/emails of employees and records of assigned activities can be personal data. A real operator must identify controller/processor roles, legal bases, data subjects, retention schedules, access procedures, backup location, breach reporting, and whether labor co-determination applies. GDPR compliance is **not automatically obtained** by using EU-hosted servers. The current audit implementation is an operational change log, not a legally mandated qualified inspection attestation.

Relevant context: [GDPR](https://eur-lex.europa.eu/eli/reg/2016/679/oj), [BetrSichV](https://www.gesetze-im-internet.de/betrsichv_2015/), [BSI](https://www.bsi.bund.de/), [OWASP ASVS](https://owasp.org/www-project-application-security-verification-standard/).

## Mandatory pre-production security checklist

1. Disable demo seeding and verify that no fixed sample user exists.
2. Create unique strong admin credentials, use secret manager and least-privilege database user.
3. Force HTTPS/HSTS, secure cookies, restrictive DNS/firewall, eliminate direct public app/database access.
4. Configure application, reverse-proxy, database and infrastructure logging **without leaking passwords or inspection personal data**.
5. Add login rate limits, MFA/SSO and account lifecycle procedures; introduce high-risk operation confirmation if needed.
6. Implement audited data retention, export and deletion processes; verify legal requirements per equipment category.
7. Add encrypted backups with tested recovery point and recovery time objectives.
8. Perform dependency vulnerability scans, DAST/penetration test and an OWASP ASVS-based review.
9. Load-test and add pagination, per-tenant quotas and resilience around scheduled work generation.
10. Implement monitored service uptime, scheduler failure alerts and incident response runbooks.

## Responsible disclosure

Do not post secrets or personal data in issues. For security matters, use a private reporting channel with the maintainer (portfolio URL in README). See root `SECURITY.md` for repository reporting practices.
