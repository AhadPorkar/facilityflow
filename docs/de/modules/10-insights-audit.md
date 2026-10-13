# Betriebskennzahlen und Audit-Protokoll

[English](../../en/modules/10-insights-audit.md) · [Deutsch](../../de/modules/10-insights-audit.md) · [Übersicht](../INDEX.md)

## Fachlicher Zweck

Manager erhalten mandantenbezogene Kennzahlen zu Anlagen, Aufträgen, Fälligkeiten und Mängeln sowie eine Historie wichtiger Prozessschritte.

## Beteiligte Rollen

Manager und VIEWER nutzen das Dashboard; das detaillierte Audit dürfen nur ADMIN/MANAGER lesen.

## Fachliche Daten / Implementierung

`AnalyticsService` aggregiert Standorte, Anlagen, Wartungspläne, Aufträge und Mängel; `AuditService` schreibt `audit_event` mit Benutzer, Objekt, Aktion und Zeit.

## API-Vertrag

`GET /api/dashboard`; `GET /api/audit-events` (Manager/Administrator).

## Beispielablauf

Bei fachlichen Änderungen wird ein Audit-Ereignis innerhalb derselben Transaktion gespeichert → Dashboard zeigt aktuelle, mandantengefilterte Kennzahlen und Listen → Management kann die letzten 100 Protokollereignisse einsehen.

## Geschäftsregeln, Rechte und Grenzfälle

Überfällig bedeutet Fälligkeitsdatum vor dem heutigen Datum gemäß `Clock`, ausgenommen APPROVED/CANCELLED. Dies ist keine automatisch zugesicherte SLA. Mandantenübergreifende Ereignisse sind nicht sichtbar. Das Audit ist anwendungsseitig fortlaufend, **nicht kryptografisch manipulationsgeschützt**.

## Automatisierte Tests

`analyticsAreTenantScoped`, `auditEvents`, `sitesCrud`, `scheduledJobIdempotency` und Dashboard-Tests der Demo.

## Bewusste Grenzen und Ausbau

Keine unveränderbare Audit-Kette, kein WORM-Speicher, keine Benachrichtigungen, Trend-Datenbank, BI-Exporte oder Prognosemodelle.

**Querverweise:** [API](../API.md) · [Architektur](../ARCHITECTURE.md) · [Testkonzept](../TESTING.md) · [Roadmap](../ROADMAP.md).
