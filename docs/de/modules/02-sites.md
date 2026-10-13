# Gebäude- und Standortverwaltung

[English](../../en/modules/02-sites.md) · [Deutsch](../../de/modules/02-sites.md) · [Übersicht](../INDEX.md)

## Fachlicher Zweck

Ein Standort beschreibt ein konkretes Objekt innerhalb eines Unternehmens und bildet den Bezugspunkt für Anlagen und Arbeitsaufträge.

## Beteiligte Rollen

Facility-Manager, Wartungsdisponent und lesende Leitung.

## Fachliche Daten / Implementierung

`site` mit UUID, Mandant, eindeutiger Kennung, Bezeichnung, Anschrift, Ort, Postleitzahl und Ansprechpartner.

## API-Vertrag

`GET /api/sites`; `GET /api/sites/{id}`; `POST /api/sites`; `PUT /api/sites/{id}`.

## Beispielablauf

Manager erfasst beispielsweise BER-001 mit Adresse → Anlagen werden diesem Gebäude zugeordnet → Arbeitsaufträge und Kennzahlen werden auf das richtige Objekt bezogen.

## Geschäftsregeln, Rechte und Grenzfälle

`(organization_id,code)` ist eindeutig; Kennungen werden großgeschrieben, Texte gekürzt. Schreibrechte nur für ADMIN/MANAGER. Fremde Standort-IDs führen zu 404, um keine Daten offenzulegen.

## Automatisierte Tests

`sitesCrud`, `noCrossTenantSites`, `analyticsAreTenantScoped` und CRUD-Tests der Browser-Demo.

## Bewusste Grenzen und Ausbau

Keine Löschfunktion, keine Landkarte, keine Geschosspläne, kein Geofencing, keine Anhänge und kein Eigentümerwechsel. Kontaktdaten lösen noch keine Benachrichtigungen aus.

**Querverweise:** [API](../API.md) · [Architektur](../ARCHITECTURE.md) · [Testkonzept](../TESTING.md) · [Roadmap](../ROADMAP.md).
