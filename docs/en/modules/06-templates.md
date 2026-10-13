# Reusable inspection checklist templates

[English](../../en/modules/06-templates.md) · [Deutsch](../../de/modules/06-templates.md) · [Index](../INDEX.md)

## Business purpose

Managers standardize inspection questions so technicians record comparable, complete results for equipment classes.

## Actors

Quality manager creates; technician selects during inspection; viewers see templates.

## Data and implementation

`inspection_template` (name, description, optional assetType, active) and ordered `checklist_item` (label, position, required).

## HTTP contract

`GET /api/inspection-templates`; `GET /api/inspection-templates/{id}`; `POST /api/inspection-templates`.

## Typical workflow

Manager enters checklist name and one question per line → publishes template → assigns optional HVAC/ELECTRICAL/etc type → technician selects template when inspecting compatible equipment.

## Rules, permissions and edge cases

1–30 items; blank item labels invalid. Each item has UUID and position. If template has a specific asset type it cannot be used on a different category. `required=false` currently influences resulting defect severity, but **all questions must still receive an answer** in v1.

## Automated tests

`checklistCreation`, `failureWorkflow`, `incompleteInspection`; Node preview template tests.

## Boundaries and future extensions

Published templates cannot be updated/archived from the API; no branching questions, measurement thresholds, revision snapshots of entire template or document attachments.

**Related:** [API](../API.md) · [Architecture](../ARCHITECTURE.md) · [Testing](../TESTING.md) · [Roadmap](../ROADMAP.md).
