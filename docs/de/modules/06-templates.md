# Wiederverwendbare Prüfvorlagen

[English](../../en/modules/06-templates.md) · [Deutsch](../../de/modules/06-templates.md) · [Übersicht](../INDEX.md)

## Fachlicher Zweck

Prüfvorlagen standardisieren Kontrollpunkte, damit Techniker vollständige und vergleichbare Ergebnisse für verschiedene Anlagenkategorien erfassen können.

## Beteiligte Rollen

Qualitäts-/Facility-Manager erstellt, Techniker verwendet, VIEWER liest.

## Fachliche Daten / Implementierung

`inspection_template` (Name, Beschreibung, optionaler Anlagentyp, aktiv) und `checklist_item` (Text, Reihenfolge, Pflichtflag).

## API-Vertrag

`GET /api/inspection-templates`; `GET /api/inspection-templates/{id}`; `POST /api/inspection-templates`.

## Beispielablauf

Manager benennt die Prüfliste und erfasst pro Zeile einen Kontrollpunkt → veröffentlicht Vorlage → ordnet optional HVAC/ELECTRICAL/usw. zu → Techniker kann sie für passende Anlagen verwenden.

## Geschäftsregeln, Rechte und Grenzfälle

1–30 Prüfpunkte; leere Texte werden abgelehnt. Jeder Punkt besitzt eine UUID und Reihenfolge. Bei festem Anlagentyp wird die Nutzung für andere Typen verhindert. `required=false` steuert derzeit nur die Schwere eines resultierenden Mangels; **auch optionale Punkte müssen in v1 beantwortet werden**.

## Automatisierte Tests

`checklistCreation`, `failureWorkflow`, `incompleteInspection` und Vorlagentests der Browser-Demo.

## Bewusste Grenzen und Ausbau

Keine API zum Bearbeiten/Archivieren bereits publizierter Vorlagen, keine bedingten Fragen, Messwertgrenzen, vollständige Vorlagenrevision oder Anhänge.

**Querverweise:** [API](../API.md) · [Architektur](../ARCHITECTURE.md) · [Testkonzept](../TESTING.md) · [Roadmap](../ROADMAP.md).
