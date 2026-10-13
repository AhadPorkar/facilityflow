# Responsive bilingual experience and interactive preview

[English](../../en/modules/11-ui-preview.md) · [Deutsch](../../de/modules/11-ui-preview.md) · [Index](../INDEX.md)

## Business purpose

The browser UI is designed to demonstrate professional information hierarchy, accessible navigation intentions, complete forms and real business-process interactions in two languages.

## Actors

Recruiters, all authenticated users, prospective facility management buyers.

## Data and implementation

`static/index.html`, `static/css/app.css`, `static/js/app.js`, `i18n.js`, `api.js`, `preview-data.js`, `utils.js`, `icons.js`.

## HTTP contract

Pages are client routes under `/#/dashboard`, `/#/orders`, `/#/sites`, `/#/assets`, `/#/plans`, `/#/inspections`, `/#/templates`, `/#/defects`, `/#/reports`, `/#/activity`, `/#/team`, `/#/settings`.

## Typical workflow

Visitor opens a static Pages demo → preview adapter loads sample buildings, assets, schedules, orders, inspections and people → user can create, assign, transition, inspect or resolve using dialogs → state is kept in `localStorage` → language switch EN/DE persists across navigation.

## Rules, permissions and edge cases

English is default. `*.github.io` or `?preview=1` forces offline synthetic preview; other locations call authenticated Spring APIs. User-provided content is HTML-escaped. The display adapts to smaller screens; JavaScript modules run natively with no bundler. Public preview has no real roles or multi-user persistence. The report action uses browser print in preview; server mode downloads real PDF.

## Automated tests

`frontend-tests/locale-and-utils.test.js`, `frontend-tests/preview-workflow.test.js` (13 Node tests); manual responsive and visual-browser QA still recommended.

## Boundaries and future extensions

No full WCAG certification, offline background synchronization, keyboard trap/focus-manager guarantees for every drawer, browser screenshot test automation, push notifications or separate native mobile app.

**Related:** [API](../API.md) · [Architecture](../ARCHITECTURE.md) · [Testing](../TESTING.md) · [Roadmap](../ROADMAP.md).
