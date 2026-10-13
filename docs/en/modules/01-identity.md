# Identity, organizations and role permissions

[English](../../en/modules/01-identity.md) · [Deutsch](../../de/modules/01-identity.md) · [Index](../INDEX.md)

## Business purpose

Authentication turns a team member into an authenticated principal bound to one organization; administrator-managed users and role gates make the site and equipment catalog safe to share.

## Actors

ADMIN, MANAGER, TECHNICIAN and VIEWER; only ADMIN creates a new user.

## Data and implementation

`organization`, `app_user`, `PrincipalUser`, `CurrentUser`, `SecurityConfig`, `TeamService`.

## HTTP contract

`GET /api/auth/csrf`; `POST /api/auth/login`; `GET /api/auth/me`; `POST /api/auth/logout`; `GET /api/team`; `POST /api/team` (ADMIN only).

## Typical workflow

A client fetches CSRF → posts email/password → server checks BCrypt and rotates the session ID → user is scoped by organization → access role is verified before write operations → logout clears session.

## Rules, permissions and edge cases

Emails are globally unique; new passwords are 12–72 characters; `VIEWER` is read-only. A technician can only execute their assigned orders. User API views never expose password hashes. Bootstrap admin and opt-in demo account are not a public sign-up endpoint.

## Automated tests

`authenticationIsRequired`, `loginWorks`, `viewerIsReadOnly`, `teamRoleBoundary`, `noCrossTenantSites`; Node preview identity tests.

## Boundaries and future extensions

SSO, MFA, password reset, login rate limiting, disabling users and tenant self-provisioning are planned. Do not deploy the seeded demo account publicly.

**Related:** [API](../API.md) · [Architecture](../ARCHITECTURE.md) · [Testing](../TESTING.md) · [Roadmap](../ROADMAP.md).
