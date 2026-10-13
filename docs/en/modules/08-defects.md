# Defect tracking and corrective action

[English](../../en/modules/08-defects.md) · [Deutsch](../../de/modules/08-defects.md) · [Index](../INDEX.md)

## Business purpose

A negative inspection question becomes a persisted finding with a link to the corrective job. The finding stays visible until a manager resolves it or formally approves the correction.

## Actors

Quality lead and facility manager own finding closure; technician performs corrective job.

## Data and implementation

`defect` (asset, inspection, corrective_work_order_id, title, details, severity, status, resolvedAt). `DefectStatus`: OPEN/RESOLVED.

## HTTP contract

`GET /api/defects`; `POST /api/defects/{id}/resolve`. Indirectly created via `POST /api/inspections` and closed by `/work-orders/{id}/approve`.

## Typical workflow

FAILED checklist item → defect OPEN; one corrective work order for the inspection → assignment/execution → manager approves corrective work → all its linked open defects close transactionally.

## Rules, permissions and edge cases

Each failed item generates its own defect. A required failed item produces HIGH severity; a nonrequired failed item produces MEDIUM. A defect cannot be manually resolved twice. Closing a corrective work order without approval does not resolve its defects.

## Automated tests

`failureWorkflow`, `resolveDefect`, `lifecycle`; Node preview defect resolution tests.

## Boundaries and future extensions

No root cause classifications, CAPA effectiveness verification, SLA escalation, evidence attachments, re-open operation or mandatory independent reviewer. Formal compliance needs these controls by domain.

**Related:** [API](../API.md) · [Architecture](../ARCHITECTURE.md) · [Testing](../TESTING.md) · [Roadmap](../ROADMAP.md).
