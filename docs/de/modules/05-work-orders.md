# Arbeitsauftrag und Freigabeprozess

[English](../../en/modules/05-work-orders.md) · [Deutsch](../../de/modules/05-work-orders.md) · [Übersicht](../INDEX.md)

## Fachlicher Zweck

Arbeitsaufträge steuern die technische Durchführung, dokumentieren das Ergebnis und verlangen eine ausdrückliche fachliche Freigabe durch die zuständige Führungsebene.

## Beteiligte Rollen

Manager erstellt, weist zu und gibt frei; zugewiesener Techniker beginnt und beendet; VIEWER liest.

## Fachliche Daten / Implementierung

`work_order`: Nummer, Standort/Anlage, Priorität, Mitarbeiter, Fälligkeit, Status, Beschreibung, Ergebnis, Start-/Ende-/Freigabezeit und optionale Planreferenz.

## API-Vertrag

`GET /api/work-orders`; `GET /api/work-orders/{id}`; `POST /api/work-orders`; `POST /api/work-orders/{id}/{assign|start|complete|approve|cancel}`.

## Beispielablauf

OPEN → ASSIGNED (Zuweisung) → IN_PROGRESS (Beginn) → COMPLETED (Ergebnis dokumentiert) → APPROVED (Managerfreigabe). OPEN/ASSIGNED dürfen storniert werden, Neuzuweisungen nur vor Arbeitsbeginn.

## Geschäftsregeln, Rechte und Grenzfälle

Statusübergänge werden serverseitig geprüft. Techniker müssen zugewiesen sein, Manager dürfen eingreifen. Standort und Anlage müssen zusammenpassen. Für den Abschluss ist ein nichtleeres Ergebnis Pflicht. Die Freigabe eines Korrekturauftrags schließt zugehörige offene Mängel in derselben Transaktion.

## Automatisierte Tests

`lifecycle`, `transitionGuard`, `failureWorkflow`, `downloadableReport`, `auditEvents` und Statusautomaten-Tests der Vorschau.

## Bewusste Grenzen und Ausbau

Keine Stundenzettel, Ersatzteile, Rechnungen, rechtsverbindlichen Signaturen, Anhänge, Beobachter oder Benachrichtigungen.

**Querverweise:** [API](../API.md) · [Architektur](../ARCHITECTURE.md) · [Testkonzept](../TESTING.md) · [Roadmap](../ROADMAP.md).
