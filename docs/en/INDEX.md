# FacilityFlow — Documentation (English)

[**Deutsch**](../de/INDEX.md) · [Main README](../../README.md)

FacilityFlow is a modular-monolith Spring Boot application for technical building operations. Each page documents the **implemented behavior**, how to use it, its tests, and where future work begins. The interactive browser showcase is a simulated, local-storage-based environment; the real deployment is a PostgreSQL-backed, session-authenticated application.

## Start here

| Document | Contents |
|---|---|
| [Requirements](REQUIREMENTS.md) | Target users, workflows, user stories, roles, acceptance criteria, German market relevance |
| [Architecture](ARCHITECTURE.md) | Diagrams, ER relationships, transaction boundaries, state machine, design decisions |
| [REST API](API.md) | Endpoints, JSON examples, CSRF/session usage, errors, lifecycle constraints |
| [Deployment](DEPLOYMENT.md) | Docker Compose, HTTPS Caddy, GitHub Pages preview, Railway, secrets, rollback |
| [Security](SECURITY.md) | Threat model, authorization, tenant isolation, production gaps |
| [Testing](TESTING.md) | Test commands, coverage matrix, integration scenarios, CI behavior |
| [Roadmap](ROADMAP.md) | Transparent boundaries, P1/P2 features, production-readiness checklist |

## Modules (each has a German equivalent)

1. [Identity, access and organizations](modules/01-identity.md)
2. [Buildings and sites](modules/02-sites.md)
3. [Equipment and assets](modules/03-assets.md)
4. [Preventive maintenance](modules/04-maintenance.md)
5. [Work orders and approvals](modules/05-work-orders.md)
6. [Inspection templates](modules/06-templates.md)
7. [Inspections](modules/07-inspections.md)
8. [Defects and corrective actions](modules/08-defects.md)
9. [Reporting](modules/09-reporting.md)
10. [Insights, KPIs and audit events](modules/10-insights-audit.md)
11. [Bilingual UI and interactive preview](modules/11-ui-preview.md)

## Key conventions

- `organization_id` identifies a business tenant; all API reads and writes are scoped to the current authenticated organization.
- `UUID` values are identifiers, not authorization grants.
- Dates are ISO `YYYY-MM-DD`; instants are UTC in the database. UI formatting depends on locale.
- `WorkStatus` is a server-enforced state machine, not a UI-only filter.
- JSON request bodies do **not** accept an organization ID; the server determines it from the authenticated user.
- English is the default UI/report language; the UI supports German via a persisted choice.
- API error responses have machine-readable codes and user-readable messages; see [API](API.md).
- Real scheduled maintenance is a Spring job; the public Pages demo simulates recurrence locally.

## Workflow narrative

A facility manager creates **Berlin Central Office**, registers an **air handling unit**, configures a maintenance plan due every 90 days, and generates a work order. The manager assigns the work to a technician. The technician starts work, records the resolution, and the manager approves completion. During an inspection, a failed checklist item opens a defect and a corrective work order. A downloadable German-language PDF contains the inspection result. The audit log records the operational transitions.

## Reference sites

- [German BetrSichV](https://www.gesetze-im-internet.de/betrsichv_2015/)
- [DGUV](https://www.dguv.de/)
- [Spring Boot](https://docs.spring.io/spring-boot/)
- [Spring Security CSRF](https://docs.spring.io/spring-security/reference/servlet/exploits/csrf.html)
- [Docker Compose](https://docs.docker.com/compose/)
- [Caddy](https://caddyserver.com/docs/automatic-https)
- [GitHub Pages](https://docs.github.com/en/pages)

These are contextual references, **not** evidence that this software has undergone regulatory conformity assessment.
