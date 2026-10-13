# Roadmap, Boundaries & Production Readiness

[Deutsch](../de/ROADMAP.md) · [Index](INDEX.md)

FacilityFlow v1 is a portfolio application with a working demo-oriented product slice. The roadmap makes clear what is **delivered**, what is **not delivered**, and which capabilities require legal and operational specialization before a commercial launch.

## Delivered (v1.0)

- Identity, session-based login, role-based access and service-level tenant checks.
- Site/asset registers with PostgreSQL constraints and audit events.
- Preventive maintenance interval engine, daily scheduled and manager-triggered execution, duplicate-cycle guard.
- Work-order finite-state machine and technician/manager separation.
- Template-based digital inspections, failed-check defects and linked corrective orders.
- Manager sign-off and automatic closure of linked defects.
- Bilingual PDF operational reports and responsive EN/DE UI.
- Synthetic public browser showcase with local state and no live credentials.
- Docker Compose/Caddy skeleton, GitHub Pages deployment workflow, Node/Java integration test source and CI workflow.

## Stage 1 — Deployment hardening (essential for real pilot)

| Workstream | Target | Exit criteria |
|---|---|---|
| Identity | MFA/SSO, reset, lockouts, stronger user lifecycle | Brute-force resistant; revoked accounts no longer work |
| Data protection | Backup, restore, retention/export/deletion | Tested RPO/RTO and GDPR-approved policy |
| API | DTOs, pagination, OpenAPI, query filters | Versioned contract, stable paged responses |
| Security | Threat-based tests, secret vault, dependency scanning | Security review accepted; no critical unpatched issues |
| Observability | Structured logs, metrics, traces, scheduler failure alerts | Alerts for stuck jobs, failed cron, 5xx spikes |
| Reports | Unicode/embedded fonts, versioned files, per-record retention | German special characters and long notes render correctly |
| Tests | Browser E2E, concurrency, load, WCAG review | Green CI, measurable baselines and coverage |

## Stage 2 — Commercial workflows

- Escalation rules and SLAs with email/SMS/in-app notifications.
- Technician photo attachment and evidence capture with secure object storage and malware scanning.
- QR code labels linking technicians to an asset's maintenance history.
- Role-based contractor portal and multi-party approval.
- Recurring inspection scheduling based on equipment classes and configurable contract policies.
- Supplier and subcontractor work completion certificates.
- A map/floor-plan location view for large commercial properties.
- Search, filters, custom dashboard widgets and export formats.

## Stage 3 — Platform integration

- BPMN 2.0-driven customer-configurable workflows and complex approval routing.
- External identity federation (OIDC/SAML), electronic signature service integration.
- Offline-first technician mobile app with conflict resolution.
- IoT measurements and asset condition signals, event queue, idempotent consumers.
- CAFM/ERP/accounting connectors; standardized asset import/export.
- Multi-region/Multi-instance resilience and message-backed background processing.

## Explicit exclusions from v1

There are no certified DGUV/BetrSichV examination certificates, qualified electronic signatures, legally guaranteed immutable archives, automated backups, real email sending, machine telemetry, booking engine, file uploads, or public open registration. The Java source uses a modular monolith, not independent microservices. Github Pages is a static simulation, not a free Spring Boot hosting trick.

## Portfolio release checklist

- [ ] `npm run check:js` passes.
- [ ] `mvn verify` passes in Java21/Docker environment.
- [ ] Both languages reviewed by a native speaker for technical terminology.
- [ ] Responsive view reviewed at 375px, 768px, 1440px.
- [ ] GitHub Pages Actions deployment configured, URL verified.
- [ ] Code scanning and Dependabot enabled on repository.
- [ ] License, contribution guide, screenshots and live-demo URL checked.
- [ ] Published repository includes no `.env` file, passwords or private data.

## Before commercial production

Everything above **plus** a German equipment/legal specialist to determine applicable inspection scope, qualifications and records; GDPR-controller assessment; hosting/backup policies; operational SLAs; security validation and accessibility audit.
