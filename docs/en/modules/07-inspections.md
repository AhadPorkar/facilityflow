# Digital inspections and immutable answer snapshots

[English](../../en/modules/07-inspections.md) · [Deutsch](../../de/modules/07-inspections.md) · [Index](../INDEX.md)

## Business purpose

A qualified team member performs a consistent checklist-based inspection, records individual outcomes, and generates follow-up work when something fails.

## Actors

Technician, facility manager and quality lead.

## Data and implementation

`inspection`, `inspection_result` (label snapshot, item UUID, pass/fail, note), references to asset, template and inspector.

## HTTP contract

`GET /api/inspections`; `GET /api/inspections/{id}`; `POST /api/inspections`; `GET /api/inspections/{id}/report?lang=de`.

## Typical workflow

Technician selects asset and category-compatible template → answers every item PASS/FAIL with notes → service validates exact item set and persists snapshot → a failed result produces defects and one corrective order → report is available.

## Rules, permissions and edge cases

Every template item UUID appears exactly once; duplicates and omissions return 422. Template must be active and type-compatible; references stay within tenant. The original FAIL inspection remains unchanged when a defect is later resolved.

## Automated tests

`failureWorkflow`, `incompleteInspection`, `downloadableReport`; Node preview pass/fail inspection tests.

## Boundaries and future extensions

No qualified technical inspector identity verification, calibration data, measurements with units, photographic evidence, signing, reviewed test protocol versioning or government certification.

**Related:** [API](../API.md) · [Architecture](../ARCHITECTURE.md) · [Testing](../TESTING.md) · [Roadmap](../ROADMAP.md).
