# Work-order process and manager approvals

[English](../../en/modules/05-work-orders.md) · [Deutsch](../../de/modules/05-work-orders.md) · [Index](../INDEX.md)

## Business purpose

Work orders coordinate human execution, capture an outcome and require explicit management acceptance before the work is considered approved.

## Actors

Coordinator/manager creates, assigns and approves. Assigned technician starts/completes. Viewer reads.

## Data and implementation

`work_order`: number, site/asset, priority, assignee, dueDate, status, description, resolution, started/completed/approved timestamps, optional corrective-plan references.

## HTTP contract

`GET /api/work-orders`; `GET /api/work-orders/{id}`; `POST /api/work-orders`; `POST /api/work-orders/{id}/{assign|start|complete|approve|cancel}`.

## Typical workflow

OPEN → ASSIGNED (assign) → IN_PROGRESS (start) → COMPLETED (resolution required) → APPROVED (manager). OPEN/ASSIGNED may be CANCELLED; reassignment is possible only before work starts.

## Rules, permissions and edge cases

Status changes are validated in the service; technician must be assigned or a manager; managers approve. Asset and site must correspond. Completion requires nonblank resolution. Approving a corrective job resolves linked OPEN defects transactionally. No implicit approval on technician completion.

## Automated tests

`lifecycle`, `transitionGuard`, `failureWorkflow`, `downloadableReport`, `auditEvents`; Node preview work-order transition tests.

## Boundaries and future extensions

No time sheets, spare parts, supplier invoices, approvals with qualified e-signatures, attachments, watchers or reminders. Status and permission rules are enforced server-side.

**Related:** [API](../API.md) · [Architecture](../ARCHITECTURE.md) · [Testing](../TESTING.md) · [Roadmap](../ROADMAP.md).
