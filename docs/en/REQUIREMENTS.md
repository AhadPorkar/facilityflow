# Product Requirements Document (PRD) — FacilityFlow

[Deutsch](../de/REQUIREMENTS.md) · [Index](INDEX.md)

**Version:** 1.0 portfolio implementation · **Market:** German building maintenance / technical facility services · **Default language:** English · **Second language:** German

## 1. Problem and users

Technical facility management often spans preventive service plans, equipment registries, field inspections, contractor work orders, defects, approval, and evidence collection. The cost is not just typing into forms: it is handoffs, missing documentation, duplicated coordination, and late preventive work.

Primary users:

| Persona | Goal | Main pain point | Product support |
|---|---|---|---|
| Facility manager | Keep properties operational and review completed work | Lack of visibility across sites | KPIs, plans, approvals, defects, audit |
| Maintenance coordinator | Schedule repairs and allocate work | Repeated manual follow-up | Due-plan job, assignment, statuses |
| Technician | Complete assigned jobs and inspections | Paper forms and unclear history | Mobile-capable web UI, checklist, completion |
| Quality / safety lead | Trace inspections and findings | Evidence scattered in PDFs/emails | Templates, stored results, defects, PDF |
| Management / read-only stakeholder | See operational condition | No consolidated overview | Scoped dashboards, read-only access |

## 2. Goals, non-goals and measurable outcomes

**Goals**: demonstrate a coherent digital maintenance-to-approval process; prevent unauthorized/cross-tenant access; avoid duplicate recurrence jobs; retain structured inspection history; output bilingual reports; provide recruiter-friendly zero-setup demo.

**Non-goals in v1**: booking management, sensor IoT ingestion, BIM/CAFM integration, contractor marketplace, accounting, HR planning, public permit filings, legally qualified signature service, real-time offline sync, certified inspection procedures.

**Illustrative success metrics for a pilot** (targets, not observed results): percentage of maintenance tasks created before due dates; average assignment/approval lead time; number of unresolved high-priority defects; completeness rate of required inspection checks; minutes of reporting effort saved per job; proportion of orders approved after documentation.

## 3. Functional requirements and acceptance criteria

| ID | Requirement | Acceptance criteria | Modules |
|---|---|---|---|
| FR-01 | Session authentication | Correct credentials establish session; wrong credentials return 401 | Identity |
| FR-02 | Role-based access | VIEWER cannot mutate; ADMIN can manage users; MANAGER approves jobs | Identity, orders |
| FR-03 | Organization segregation | Tenant A cannot read/refer to Tenant B sites, assets or users | All backend modules |
| FR-04 | Site registry | Manager can create, list, edit buildings with tenant-unique site code | Sites |
| FR-05 | Asset registry | Asset belongs to same-tenant site; manager can add and edit asset details | Assets |
| FR-06 | Preventive plans | Configure interval 1–3650 days, next due date, assigned technician | Maintenance |
| FR-07 | Due automation | Scheduled/manual trigger generates one order per plan cycle; moves next due forward | Maintenance |
| FR-08 | Work-order state machine | Reject illegal status jumps; only assignee/manager can perform work | Orders |
| FR-09 | Manager sign-off | Only manager/admin can approve `COMPLETED` order | Orders |
| FR-10 | Templates | Manager publishes 1–30 check items, optionally category-specific | Templates |
| FR-11 | Complete inspections | Every template item answered exactly once, with results saved | Inspections |
| FR-12 | Corrective actions | Failed checks create defects and one linked corrective work order | Inspections, defects |
| FR-13 | Defect closure | Manager manually resolves a defect OR approves its corrective order | Defects, orders |
| FR-14 | Reports | Inspection/work-order report downloadable as PDF, EN or DE | Reporting |
| FR-15 | Audit | Create/update/state-change actions recorded with organization, actor, time | Audit |
| FR-16 | KPI dashboard | Per-tenant counts for sites/assets/statuses/overdue/defects | Analytics |
| FR-17 | Localization | English is initial UI language; German toggle changes user-facing labels | UI |
| FR-18 | Preview | Public local sample dataset allows realistic operations without credentials | UI preview |

## 4. Cross-functional requirements

**Security**: BCrypt passwords (cost 12); session cookie HTTP-only, SameSite Lax, Secure in HTTPS; CSRF token required for state-changing API calls; role authorization; tenant filtering; no secrets committed; no exposure of database port from Compose.

**Reliability**: service methods are transactional; PostgreSQL maintains referential integrity; unique `(maintenance_plan_id, scheduled_for)` is a database-level idempotency guard; recurring job uses pessimistic locks and a daily UTC schedule. A full distributed scheduler or HA coordination is not included.

**Performance**: structured indices on organization/status/due dates. The v1 list endpoints return bounded-by-tenant complete lists (no server pagination): suitable for portfolio/demo loads, **not** claimed as scale-tested.

**Accessibility**: responsive layout and keyboard-activatable list rows. Full WCAG 2.2 AA audit is pending. UI colors are not the sole source of status information.

**Internationalization**: English/German UI dictionary; PDF report labels support EN/DE; stable enum values remain untranslated for API compatibility. Long arbitrary non-Latin input has limitations in current PDF fonts; see [Reporting](modules/09-reporting.md).

**Compliance context**: BetrSichV and DGUV illustrate the domain's inspection/documentation needs, but operational legal conformity depends on the exact equipment, inspection method, qualified examiner, retention schedule, and relevant regulations. No compliance certificate is provided.

## 5. Primary end-to-end use cases

### UC-1 Planned service

Manager registers site + HVAC asset → creates 90-day plan → the 03:00 UTC job checks due plans → new work order created once for due cycle → technician assigned → technician starts → technician completes with resolution → manager approves → activity and PDF report available.

**Exceptions**: inactive plans ignored; a missing assignee produces OPEN status; illegal work status jumps rejected; repeated due trigger does not duplicate an order; downtime backlog is collapsed to one catch-up order, with next due advanced.

### UC-2 Inspection and correction

Manager publishes template → technician picks asset + compatible template → every item answered PASS/FAIL → failed item produces a defect → system creates one corrective work order for that inspection → manager assigns corrective work → technician completes → manager approves → linked open defects automatically marked RESOLVED.

**Exceptions**: wrong asset type, duplicate/missing checklist answer, inactive template or foreign tenant reference rejected. The initial inspection record is not silently rewritten when a defect is resolved.

### UC-3 Cross-company privacy boundary

Tenant A user requests Tenant B site ID → 404 (not 200); Tenant A tries creating a work order referencing Tenant B site → 404. The server does not accept `organizationId` in request DTOs.

## 6. Prioritization and release scope

- **P0 / v1 delivered**: FR-01 through FR-18 as described in this repository, test suite, bilingual public preview, Compose.
- **P1 / before real production**: structured logging/metrics; backup/restore practice; pagination; migration review; security penetration tests; public demo/login separation; richer PDF fonts; attachment storage with virus scanning; MFA/SSO for sensitive deployments.
- **P2**: SLA and notification engine, supplier portal, QR asset tags, configurable BPMN stages, offline field app, IoT triggers, calendar, CMMS/ERP connectors.

See [Roadmap](ROADMAP.md) for dependencies and quality gates.

## 7. Source references and assumptions

1. German [Betriebssicherheitsverordnung](https://www.gesetze-im-internet.de/betrsichv_2015/), particularly applicable provisions on equipment inspection/records.
2. [DGUV](https://www.dguv.de/) guidance for occupational safety, recurring technical inspections and responsibility allocation.
3. [Federal Office for Information Security (BSI)](https://www.bsi.bund.de/) for general IT-security practices.
4. [EU GDPR](https://eur-lex.europa.eu/eli/reg/2016/679/oj) for personal data considerations.

These references are background, not an assertion that every German organization has the same paperwork burden or that this product meets every legal obligation.
