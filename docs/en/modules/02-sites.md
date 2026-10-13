# Buildings and site registry

[English](../../en/modules/02-sites.md) · [Deutsch](../../de/modules/02-sites.md) · [Index](../INDEX.md)

## Business purpose

A site is the physical property/location managed by one business organization and is the anchor for assets and work orders.

## Actors

Facility manager, coordinator and read-only management stakeholders.

## Data and implementation

`site` includes UUID, organization, unique site code, name, address, city, postal code, contact name and email.

## HTTP contract

`GET /api/sites`; `GET /api/sites/{id}`; `POST /api/sites`; `PUT /api/sites/{id}`.

## Typical workflow

Manager registers a building with a code such as BER-001 and an address → equipment is linked to this building → new work orders and building KPIs are scoped to the same organization.

## Rules, permissions and edge cases

Unique `(organization_id,code)`; server uppercases the code and trims input; write endpoints require ADMIN or MANAGER; foreign organization site IDs return 404 rather than leaking metadata.

## Automated tests

`sitesCrud`, `noCrossTenantSites`, `analyticsAreTenantScoped`; Node preview CRUD tests.

## Boundaries and future extensions

No site deletion, geocoding, interactive map, geofencing, attachments, floor plan or ownership transfer. Contacts are reference fields, not an outbound notification system.

**Related:** [API](../API.md) · [Architecture](../ARCHITECTURE.md) · [Testing](../TESTING.md) · [Roadmap](../ROADMAP.md).
