# REST API Reference (v1)

[Deutsch](../de/API.md) · [Index](INDEX.md)

**Base URL:** `/api` (same-origin). **Transport:** JSON over HTTPS; two report endpoints return `application/pdf`. **Authentication:** HTTP session cookie and anti-CSRF token. **Identifiers:** UUID. **Dates:** ISO `YYYY-MM-DD`. **Instant timestamps:** ISO UTC. API enum strings are invariant and are localized only for display.

> These are the concrete implemented controller contracts, not a proposed OpenAPI definition. An OpenAPI UI/code generator is a roadmap item.

## 1. Authentication and CSRF

| Method | Path | Permission | Result |
|---|---|---|---|
| GET | `/api/auth/csrf` | Public | `{ "token": "..." }` and readable `XSRF-TOKEN` cookie |
| POST | `/api/auth/login` | Public + CSRF | Authenticated session + current user |
| GET | `/api/auth/me` | Signed-in | `id`, `name`, `email`, `role`, `organization` |
| POST | `/api/auth/logout` | Signed-in + CSRF | `{ "success": true }` |

For browser callers: 1. GET `/api/auth/csrf` with credentials; 2. read `XSRF-TOKEN` cookie and send it as `X-XSRF-TOKEN` on POST/PUT/etc; 3. POST `/api/auth/login` with JSON credentials and session cookie; 4. after authentication, fetch a fresh CSRF token; 5. send same-origin cookies for every authenticated call. Server-side `CookieCsrfTokenRepository` is configured with an explicit token request handler.

```bash
# Local development only. The curl cookie jar remembers the session.
curl -s -c cookies.txt http://localhost:8080/api/auth/csrf
# Use the token from the response (or URL-decoded XSRF-TOKEN cookie):
TOKEN='paste-token-from-response'
curl -c cookies.txt -b cookies.txt -X POST http://localhost:8080/api/auth/login \
  -H 'Content-Type: application/json' -H "X-XSRF-TOKEN: $TOKEN" \
  -d '{"email":"demo@facilityflow.local","password":"DemoPass123!"}'
# Refresh GET /api/auth/csrf after login before more writes.
```

In a production environment always use HTTPS. Do not copy test credentials outside a local demo.

## 2. Roles

| Operation class | ADMIN | MANAGER | TECHNICIAN | VIEWER |
|---|:---:|:---:|:---:|:---:|
| Read own-tenant operational data | ✓ | ✓ | ✓ | ✓ |
| Create/update sites, assets, plans, templates | ✓ | ✓ | — | — |
| Create/assign/approve/cancel work orders | ✓ | ✓ | — | — |
| Start/complete assigned work | ✓ | ✓ | ✓* | — |
| Submit inspection | ✓ | ✓ | ✓ | — |
| Resolve defect manually | ✓ | ✓ | — | — |
| View audit records | ✓ | ✓ | — | — |
| Create team member | ✓ | — | — | — |

`*` For technicians, the work order must be assigned to their account. Managers can act on any order within their tenant.

## 3. Endpoint inventory

| Method | Endpoint | Output / purpose | Write role |
|---|---|---|---|
| GET | `/sites` | Site list | all signed-in |
| GET | `/sites/{id}` | Site details | all signed-in |
| POST | `/sites` | Create site (201) | ADMIN/MANAGER |
| PUT | `/sites/{id}` | Update site | ADMIN/MANAGER |
| GET | `/assets` | Asset list | all |
| GET | `/assets/{id}` | Asset details | all |
| POST | `/assets` | Create asset (201) | ADMIN/MANAGER |
| PUT | `/assets/{id}` | Update asset | ADMIN/MANAGER |
| GET | `/maintenance-plans` | Plan list | all |
| GET | `/maintenance-plans/{id}` | Plan details | all |
| POST | `/maintenance-plans` | Create plan (201) | ADMIN/MANAGER |
| PUT | `/maintenance-plans/{id}` | Update plan | ADMIN/MANAGER |
| POST | `/maintenance-plans/generate-due` | Create due jobs (`{ "created": n }`) | ADMIN/MANAGER |
| GET | `/work-orders` | Work-order list | all |
| GET | `/work-orders/{id}` | Work-order detail | all |
| POST | `/work-orders` | Create (201) | ADMIN/MANAGER |
| POST | `/work-orders/{id}/assign` | Assign/reassign | ADMIN/MANAGER |
| POST | `/work-orders/{id}/start` | Start work | ADMIN/MANAGER/assigned TECHNICIAN |
| POST | `/work-orders/{id}/complete` | Complete with resolution | ADMIN/MANAGER/assigned TECHNICIAN |
| POST | `/work-orders/{id}/approve` | Approve and close linked defects | ADMIN/MANAGER |
| POST | `/work-orders/{id}/cancel` | Cancel open/assigned | ADMIN/MANAGER |
| GET | `/inspection-templates` | Template summary list | all |
| GET | `/inspection-templates/{id}` | `{ template, items }` | all |
| POST | `/inspection-templates` | Publish checklist (201) | ADMIN/MANAGER |
| GET | `/inspections` | Inspection summary list | all |
| GET | `/inspections/{id}` | `{ inspection, results, defects }` | all |
| POST | `/inspections` | Submit an inspection (201) | ADMIN/MANAGER/TECHNICIAN |
| GET | `/defects` | Defect list | all |
| POST | `/defects/{id}/resolve` | Manually resolve finding | ADMIN/MANAGER |
| GET | `/dashboard` | Counts, distribution, recent items | all |
| GET | `/audit-events` | Up to 100 newest tenant events | ADMIN/MANAGER |
| GET | `/inspections/{id}/report?lang=de` | PDF inspection report, EN/DE | all |
| GET | `/work-orders/{id}/report?lang=en` | PDF work-order report, EN/DE | all |
| GET | `/team` | Team summary list (no password hashes) | all |
| POST | `/team` | Create user (201) | ADMIN |

Routes in table are relative to `/api`. Unless explicitly listed, deletion/bulk operations are not implemented.

## 4. Representative JSON requests

### Buildings

`POST /api/sites`

```json
{
  "code": "BER-001",
  "name": "Berlin Central Office",
  "address": "Alexanderplatz 1",
  "city": "Berlin",
  "postalCode": "10178",
  "contactName": "Facility Desk",
  "contactEmail": "desk@example.invalid"
}
```

### Equipment

`POST /api/assets`: `siteId` must be a UUID returned from `/api/sites`.

```json
{
  "siteId": "00000000-0000-0000-0000-000000000001",
  "code": "HVAC-01",
  "name": "Air handling unit",
  "type": "HVAC",
  "location": "Roof level 4",
  "manufacturer": "Example Manufacturing",
  "status": "ACTIVE",
  "installedOn": "2023-06-15"
}
```

Available asset types: `HVAC`, `ELECTRICAL`, `FIRE_SAFETY`, `PLUMBING`, `ELEVATOR`, `OTHER`. Statuses: `ACTIVE`, `OUT_OF_SERVICE`, `RETIRED`.

### Planned work

`POST /api/maintenance-plans`:

```json
{
  "assetId": "00000000-0000-0000-0000-000000000002",
  "title": "Quarterly ventilation maintenance",
  "intervalDays": 90,
  "nextDueDate": "2026-11-01",
  "instructions": "Check filters, belts and motor bearings",
  "active": true
}
```

### Work order and lifecycle

`POST /api/work-orders`:

```json
{
  "siteId": "00000000-0000-0000-0000-000000000001",
  "assetId": "00000000-0000-0000-0000-000000000002",
  "title": "Replace HVAC filter",
  "description": "Replace filter and record condition",
  "priority": "HIGH",
  "dueDate": "2026-11-05"
}
```

Then `POST /work-orders/{id}/assign` with `{"userId":"<team UUID>"}`; `POST .../start` (empty body); `POST .../complete` with `{"resolution":"Filter changed; airflow verified"}`; `POST .../approve` (empty body). Possible priorities: `LOW`, `MEDIUM`, `HIGH`, `CRITICAL`.

### Inspection templates and checklists

`POST /api/inspection-templates`:

```json
{
  "name": "HVAC quarterly check",
  "description": "Safety and operating condition",
  "assetType": "HVAC",
  "items": [
    { "label": "Electrical isolation safe", "required": true },
    { "label": "No unusual vibration", "required": true }
  ]
}
```

`GET /api/inspection-templates/{id}` returns actual checklist item UUIDs. Use exactly those values in a submission:

```json
{
  "assetId": "00000000-0000-0000-0000-000000000002",
  "templateId": "00000000-0000-0000-0000-000000000003",
  "notes": "Inspection at regular service visit",
  "results": [
    { "itemId": "00000000-0000-0000-0000-000000000004", "passed": true },
    { "itemId": "00000000-0000-0000-0000-000000000005", "passed": false, "note": "Bearing vibration excessive" }
  ]
}
```

If any item fails, the response contains `inspection.status=FAIL`, defects and linked corrective work. Every checklist ID must appear **exactly once**; wrong asset categories, duplicates, omissions and foreign-tenant identifiers are rejected.

### Create team member

`POST /api/team` (`ADMIN` only):

```json
{
  "displayName": "Nina Technician",
  "email": "nina@example.invalid",
  "password": "ReplaceWithAStrongPassword!",
  "role": "TECHNICIAN"
}
```

## 5. Error format and status semantics

```json
{
  "code": "INVALID_WORKFLOW",
  "message": "Order must be assigned before start",
  "timestamp": "2026-10-13T10:00:00Z"
}
```

| HTTP | Typical reason | Code |
|---|---|---|
| 400 | Bean Validation error | `VALIDATION_ERROR` |
| 401 | Missing authentication / invalid credentials | `INVALID_CREDENTIALS` for login; Spring entry point for anonymous requests |
| 403 | Incorrect role or technician not assigned | `ACCESS_DENIED` |
| 404 | Missing resource or foreign tenant | `NOT_FOUND` |
| 409 | Duplicate email, site/asset code, DB relation violation | `DUPLICATE_EMAIL`, `DUPLICATE_OR_REFERENCE` |
| 422 | Invalid work-order or inspection business rule | `INVALID_WORKFLOW` |

Some framework-originated errors may use Spring's own problem response format. Do not hard-code frontend assumptions beyond the documented successful shapes and standard HTTP error statuses.

## 6. Versioning & constraints

v1 is unversioned under `/api`. A production release should add `/api/v1`, cursor pagination, stable response DTOs, OpenAPI schema validation, filtering/sorting, request correlation IDs, rate limiting and documented deprecations. Neither file attachments nor arbitrary inspection signature images are implemented.
