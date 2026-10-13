# Präventive Wartungsplanung und Wiederholung

[English](../../en/modules/04-maintenance.md) · [Deutsch](../../de/modules/04-maintenance.md) · [Übersicht](../INDEX.md)

## Fachlicher Zweck

Ein Wartungsplan beschreibt regelmäßige Instandhaltung über Termin und Intervall. Der transaktionale Scheduler erzeugt daraus Arbeitsaufträge und schreibt die nächste Fälligkeit fort.

## Beteiligte Rollen

Facility-Manager, Wartungsplaner und Techniker.

## Fachliche Daten / Implementierung

`maintenance_plan` (Anlage, Intervall, nächste Fälligkeit, Mitarbeiter, Aktivierung, Anweisung); im Auftrag `maintenance_plan_id` und `scheduled_for`.

## API-Vertrag

`GET /api/maintenance-plans`; `GET /api/maintenance-plans/{id}`; `POST /api/maintenance-plans`; `PUT /api/maintenance-plans/{id}`; `POST /api/maintenance-plans/generate-due`.

## Beispielablauf

Manager erfasst einen 90-Tage-Plan → täglicher Job um 03:00 UTC sperrt fällige Datensätze → erstellt einen Auftrag je Termin → verschiebt nächste Fälligkeit. Alternativ löst ein Manager den Lauf manuell aus.

## Geschäftsregeln, Rechte und Grenzfälle

Intervalle 1–3650 Tage, nur aktive Pläne. Pessimistische Sperre und Unique-Constraint für `(maintenance_plan_id, scheduled_for)` schützen vor Doppelaufträgen. Mehrere versäumte Zyklen werden **zu einem Nachholauftrag zusammengefasst**. Zugewiesene Mitarbeiter müssen zum Mandanten gehören.

## Automatisierte Tests

`scheduledJobIdempotency`, `analyticsAreTenantScoped`, `noCrossTenantSites` sowie Wiederholungstests der Demo.

## Bewusste Grenzen und Ausbau

Wartungsintervall ist eine Betreiberentscheidung, kein automatischer Rechtsnachweis. SLA-Benachrichtigungen, Feiertagskalender, Ressourcenplanung und verteilter HA-Scheduler fehlen.

**Querverweise:** [API](../API.md) · [Architektur](../ARCHITECTURE.md) · [Testkonzept](../TESTING.md) · [Roadmap](../ROADMAP.md).
