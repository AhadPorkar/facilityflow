# Anlagenkataster und Anlagenlebenszyklus

[English](../../en/modules/03-assets.md) · [Deutsch](../../de/modules/03-assets.md) · [Übersicht](../INDEX.md)

## Fachlicher Zweck

Das Anlagenverzeichnis verknüpft wartbare Geräte mit ihrem Gebäude. Technische Kategorie und Zustand steuern Wartungen und passende Prüflisten.

## Beteiligte Rollen

Facility-Manager, Anlagenverantwortlicher und Techniker.

## Fachliche Daten / Implementierung

`asset`: siteId, eindeutige Kennung, Name, Anlagentyp, Raum/Standort, Hersteller, Seriennummer, Status und Installationsdatum.

## API-Vertrag

`GET /api/assets`; `GET /api/assets/{id}`; `POST /api/assets`; `PUT /api/assets/{id}`.

## Beispielablauf

Manager erfasst eine Lüftungsanlage in einem Gebäude → setzt Zustand ACTIVE/OUT_OF_SERVICE/RETIRED → plant Wartung → verwendet eine zum Anlagentyp passende Prüfliste.

## Geschäftsregeln, Rechte und Grenzfälle

Der Standort muss zum selben Mandanten gehören. Die Anlagenkennung ist je Mandant eindeutig. Der Standortwechsel einer bestehenden Anlage wird bewusst abgelehnt, da hierfür ein gesonderter auditierbarer Übertragungsprozess nötig wäre. Kategorien: HVAC, ELECTRICAL, FIRE_SAFETY, PLUMBING, ELEVATOR, OTHER.

## Automatisierte Tests

`assetsCannotCrossTenant`, `failureWorkflow`, `analyticsAreTenantScoped` sowie Vorschautests.

## Bewusste Grenzen und Ausbau

Keine QR-Codes, Fotos/Dokumente, Anlagenhierarchien, Zählerstände, OEM-Schnittstellen, Ersatzteile oder buchhalterische Abschreibung.

**Querverweise:** [API](../API.md) · [Architektur](../ARCHITECTURE.md) · [Testkonzept](../TESTING.md) · [Roadmap](../ROADMAP.md).
