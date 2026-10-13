# Operational KPIs and audit events

[English](../../en/modules/10-insights-audit.md) · [Deutsch](../../de/modules/10-insights-audit.md) · [Index](../INDEX.md)

## Business purpose

Give managers one tenant-specific view of asset counts, open tasks, overdue work, open defects, status distribution and important historical transitions.

## Actors

Managers and viewers use overview; only ADMIN/MANAGER may browse detailed audit trail.

## Data and implementation

`AnalyticsService` aggregates `site`, `asset`, `maintenance_plan`, `work_order`, `defect`; `AuditService` persists `audit_event` with actor, entity, action and timestamp.

## HTTP contract

`GET /api/dashboard`; `GET /api/audit-events` (manager/admin).

## Typical workflow

When an operation changes business state, a corresponding audit event is saved in its transaction; dashboard queries tenant-only counts and recent lists; managers inspect the 100 newest events.

## Rules, permissions and edge cases

Dashboard overdue means due date before current local date in injected `Clock`, excluding APPROVED/CANCELLED; it is not a promised SLA. Events do not reveal other tenants. Audit log is application append-oriented, **not cryptographically immutable**.

## Automated tests

`analyticsAreTenantScoped`, `auditEvents`, `sitesCrud`, `scheduledJobIdempotency`; Node preview dashboard tests.

## Boundaries and future extensions

No export, advanced metrics, immutable audit hash-chain, WORM storage, alerting, event-streaming, historical trend warehouse or machine-learning forecasting.

**Related:** [API](../API.md) · [Architecture](../ARCHITECTURE.md) · [Testing](../TESTING.md) · [Roadmap](../ROADMAP.md).
