# Equipment register and asset lifecycle

[English](../../en/modules/03-assets.md) · [Deutsch](../../de/modules/03-assets.md) · [Index](../INDEX.md)

## Business purpose

The asset register links serviceable equipment to its building, identifies its technical category and captures equipment condition for inspections and maintenance.

## Actors

Facility manager, asset coordinator and technician.

## Data and implementation

`asset` stores siteId, unique code, name, type, location, manufacturer, serial number, status and installation date; `AssetType` and `AssetStatus` enums.

## HTTP contract

`GET /api/assets`; `GET /api/assets/{id}`; `POST /api/assets`; `PUT /api/assets/{id}`.

## Typical workflow

Manager registers an HVAC unit in a selected building → chooses ACTIVE/OUT_OF_SERVICE/RETIRED → schedules service for this equipment → uses suitable inspection checklist.

## Rules, permissions and edge cases

The selected site must belong to the same organization. Asset code is tenant-unique. Changing an existing asset’s site is deliberately rejected: a transfer should be a separate audited process. Types: HVAC, ELECTRICAL, FIRE_SAFETY, PLUMBING, ELEVATOR, OTHER.

## Automated tests

`assetsCannotCrossTenant`, `failureWorkflow`, `analyticsAreTenantScoped`; Node preview asset and inspection data tests.

## Boundaries and future extensions

No QR scan/print, photo/document attachment, parent-child assets, meter readings, OEM integrations, spare-part inventory or accounting depreciation.

**Related:** [API](../API.md) · [Architecture](../ARCHITECTURE.md) · [Testing](../TESTING.md) · [Roadmap](../ROADMAP.md).
