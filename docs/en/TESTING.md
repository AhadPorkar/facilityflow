# Testing Strategy & Coverage Matrix

[Deutsch](../de/TESTING.md) · [Index](INDEX.md)

## 1. Test pyramid

FacilityFlow prioritizes **real API behavior against a real PostgreSQL engine**. Integration tests exercise authentication, HTTP status codes, role checks, Flyway schema, domain services and persistence together. A second no-dependency Node.js suite exercises the bilingual browser-preview domain engine and UI utility functions. This is suitable for a showcase, but it is not a substitute for performance, accessibility, security and live-browser end-to-end tests.

```text
Layer 1: JUnit 5 + MockMvc + Spring Boot + Testcontainers/PostgreSQL 16
  - Cross-module scenarios including database constraints and authorization
Layer 2: node:test + assert/strict (Node 22)
  - Locale dictionaries, escaping, date utilities, synthetic business flows
Layer 3: automated source checks in npm check:js
  - JavaScript syntax verification
```

## 2. How to run

### Backend: real PostgreSQL container

```bash
# Java 21, Maven 3.9+, Docker daemon must be available.
mvn --batch-mode --no-transfer-progress verify
```

The `FacilityFlowIntegrationTest` JUnit class uses `@Testcontainers`, `@Container` PostgreSQL 16, `@DynamicPropertySource`, `@SpringBootTest`, `@AutoConfigureMockMvc`, and rollback-backed setup. The database is ephemeral and uses **Flyway migrations**; it does not connect to your real application DB. If Docker is unavailable, Testcontainers cannot start; that is a runtime prerequisite, not a reason to silently skip the tests.

### Front-end: no npm install required

```bash
node --version     # Node.js 22 recommended
npm run check:js
```

The `check:js` script parses `app.js`, `api.js`, and `preview-data.js`, then runs `node --test frontend-tests/*.test.js`. No CDN, npm install or browser test runner is needed.

## 3. Backend module test matrix

| Module | Integration test(s) | Assertions |
|---|---|---|
| Session and login | `authenticationIsRequired`, `loginWorks` | 401 anonymous, BCrypt login, identity/role |
| Authorization | `viewerIsReadOnly`, `teamRoleBoundary`, `transitionGuard` | Viewer cannot edit; admin-only account creation; unassigned tech rejected |
| Tenant isolation | `noCrossTenantSites`, `assetsCannotCrossTenant`, `analyticsAreTenantScoped` | Foreign site not visible/referencable; KPIs scoped |
| Buildings | `sitesCrud` | POST 201, PUT updated, GET list |
| Equipment | `assetsCannotCrossTenant` | Foreign site denied, own-tenant asset accepted |
| Maintenance plans | `scheduledJobIdempotency` | Due plan generates one order; repeated run zero; unique cycle |
| Work-order lifecycle | `transitionGuard`, `lifecycle` | Invalid transition 422; start → complete → approve |
| Inspection template | `checklistCreation` | New template and exactly two checklist items |
| Inspections | `failureWorkflow`, `incompleteInspection` | Failed check creates defect + corrective order; omission 422 |
| Defects | `resolveDefect`, `failureWorkflow` | Manager resolves defect; failure creates finding |
| Dashboard & audit | `analyticsAreTenantScoped`, `auditEvents` | KPI tenant bounds, event persisted, viewer denied audit |
| PDF | `downloadableReport` | Status 200, PDF content type, `%PDF-` file signature |
| Team management | `teamRoleBoundary` | Only admin may provision staff |

## 4. Front-end coverage

`frontend-tests/locale-and-utils.test.js` checks dictionary consistency, EN/DE text, special-character escaping, initials, dates and utility functions. `frontend-tests/preview-workflow.test.js` exercises browser demo initialization and data shape, work-order status transitions, maintenance generation idempotency, inspection failure/defect creation and closing of corrective work. There are **13 automated Node tests** in the included suite.

## 5. GitHub Actions CI

`.github/workflows/ci.yml` starts two independent jobs for every `push` and `pull_request`:

1. `frontend`: ubuntu-latest, `actions/setup-node@v4` with Node 22, runs `npm run check:js`.
2. `backend`: ubuntu-latest, `actions/setup-java@v4` Temurin 21 with Maven cache, runs `mvn verify`, uploads `target/surefire-reports/` if the job fails.

Testcontainers relies on the GitHub-hosted Ubuntu runner's Docker daemon. CI status must be checked after publishing: providing test source files is **not the same** as obtaining a green backend build.

## 6. Suggested next test cases

| Priority | Test to add | Rationale |
|---|---|---|
| P0 | Full browser E2E against deployed app (Playwright) | Click-level regressions and real session cookie behavior |
| P0 | Test cross-tenant report URLs and all write endpoints | Regression safety for new API routes |
| P0 | MockClock-based scheduler boundary/date tests | Deterministic UTC, leap years, overdue intervals |
| P0 | Verify corrective defect **auto-closure** after approval | Existing business rule merits dedicated assertion |
| P1 | Parallel due-job attempts on 2 concurrent transactions | Prove locks + DB uniqueness under contention |
| P1 | Transaction failure injection mid-inspection | No partial defect/work-order commit |
| P1 | Parameterized state machine transitions for all roles | Better coverage than selected happy/error paths |
| P1 | WCAG keyboard/screenreader and contrast audit | Accessibility for a professional German product |
| P1 | OWASP ASVS/API security scanning | XSS, CSRF, account enumeration, rate limits |
| P1 | Baseline load tests at 10k assets / 100k work orders | Determine pagination, indexing, and pool needs |
| P2 | Visual regression snapshots in Chromium | Detect responsive layout/UI regressions |
| P2 | Restoration test from a real backup | Operational recovery confidence |

## 7. Troubleshooting

- **`Could not find a valid Docker environment`** → Start Docker Desktop/daemon, verify `docker info`, rerun Maven.
- **Flyway schema error** → Check PostgreSQL version and migration name; avoid adding tables by Hibernate.
- **Backend test compilation error** → Verify Java 21 and run `mvn -v`; ensure Maven resolves dependencies from Maven Central.
- **Browser preview inaccessible** → Serve static files over HTTP (e.g. `python -m http.server 5500`); ES modules do not reliably work with `file://`.
- **CSRF 403 from curl** → Fetch `/api/auth/csrf`, persist cookie jar and pass `X-XSRF-TOKEN` header.
- **CI slow on first run** → Maven and container images must be downloaded initially; use cache, avoid skipping tests.

The source was authored for reproducible CI execution. Evaluate the actual status of `mvn verify` on an environment with Maven Central and Docker rather than assuming the backend suite passed because it was written.
