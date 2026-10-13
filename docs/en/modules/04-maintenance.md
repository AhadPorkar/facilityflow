# Preventive maintenance and recurring scheduling

[English](../../en/modules/04-maintenance.md) · [Deutsch](../../de/modules/04-maintenance.md) · [Index](../INDEX.md)

## Business purpose

A maintenance plan expresses a repeatable service obligation as a due date and interval. A transactional daily scheduler creates actionable work orders and advances the next due date.

## Actors

Facility manager, maintenance planner, technician.

## Data and implementation

`maintenance_plan` (asset, interval_days, next_due_date, assignee, active, instructions); `work_order.maintenance_plan_id` and `scheduled_for`.

## HTTP contract

`GET /api/maintenance-plans`; `GET /api/maintenance-plans/{id}`; `POST /api/maintenance-plans`; `PUT /api/maintenance-plans/{id}`; `POST /api/maintenance-plans/generate-due`.

## Typical workflow

Manager defines 90-day HVAC plan → at 03:00 UTC scheduler locks due plans → inserts one work order for the current due cycle → advances next due into future; manual manager trigger uses the same engine.

## Rules, permissions and edge cases

Interval between 1 and 3650 days, active plans only. Pessimistic database lock plus unique `(maintenance_plan_id, scheduled_for)` prevent duplicate cycle. If a service was missed for multiple periods, one catch-up order is generated, **not** one per skipped interval. Optional assignee must belong to current tenant.

## Automated tests

`scheduledJobIdempotency`, `analyticsAreTenantScoped`, `noCrossTenantSites`; Node preview recurrence tests.

## Boundaries and future extensions

Interval choice is an operator decision, not a legal certification; no SLA notifications, holiday calendars, capacity planning or cluster HA scheduler. See architecture for concurrency assumptions.

**Related:** [API](../API.md) · [Architecture](../ARCHITECTURE.md) · [Testing](../TESTING.md) · [Roadmap](../ROADMAP.md).
