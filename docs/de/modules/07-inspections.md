# Digitale Prüfungen und Ergebnis-Snapshots

[English](../../en/modules/07-inspections.md) · [Deutsch](../../de/modules/07-inspections.md) · [Übersicht](../INDEX.md)

## Fachlicher Zweck

Ein befugter Mitarbeiter führt eine standardisierte Prüfung aus, dokumentiert jeden Kontrollpunkt und stößt bei Auffälligkeiten automatisch Folgearbeiten an.

## Beteiligte Rollen

Techniker, Facility-Manager und Qualitätsverantwortlicher.

## Fachliche Daten / Implementierung

`inspection`, `inspection_result` (gespeicherter Punkttext, UUID, bestanden/nicht bestanden, Kommentar), Referenzen auf Anlage, Vorlage und Prüfer.

## API-Vertrag

`GET /api/inspections`; `GET /api/inspections/{id}`; `POST /api/inspections`; `GET /api/inspections/{id}/report?lang=de`.

## Beispielablauf

Techniker wählt Anlage und passende Vorlage → beantwortet jeden Punkt PASS/FAIL mit Notiz → Server prüft Vollständigkeit → speichert Snapshots → bei Fehler entstehen Mängel und ein Korrekturauftrag → Bericht kann geladen werden.

## Geschäftsregeln, Rechte und Grenzfälle

Alle Prüfpunkt-IDs sind genau einmal erforderlich; Dubletten und Auslassungen führen zu 422. Vorlage muss aktiv und passend sein, Referenzen mandantengleich. Der ursprüngliche FAIL-Befund bleibt auch nach Mangelbehebung bestehen.

## Automatisierte Tests

`failureWorkflow`, `incompleteInspection`, `downloadableReport` und Prüftests der Browser-Demo.

## Bewusste Grenzen und Ausbau

Keine Identitätsprüfung befähigter Personen, Kalibrierprotokolle, Messwerte mit Einheiten, Fotos, Signaturen, rechtsverbindliche Protokollversionierung oder behördliche Zertifizierung.

**Querverweise:** [API](../API.md) · [Architektur](../ARCHITECTURE.md) · [Testkonzept](../TESTING.md) · [Roadmap](../ROADMAP.md).
