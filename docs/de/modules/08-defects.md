# Mängelverfolgung und Korrekturmaßnahmen

[English](../../en/modules/08-defects.md) · [Deutsch](../../de/modules/08-defects.md) · [Übersicht](../INDEX.md)

## Fachlicher Zweck

Ein negativer Prüfpunkt wird als eigenständiger Mangel gespeichert und mit einem Korrekturauftrag verbunden. Der Mangel bleibt sichtbar, bis eine berechtigte Person ihn bestätigt oder die Korrektur freigibt.

## Beteiligte Rollen

Qualitäts-/Facility-Manager schließt Mängel; Techniker führt Korrekturen aus.

## Fachliche Daten / Implementierung

`defect` (Anlage, Prüfung, Korrekturauftrag, Titel, Details, Schwere, Status, Behebungszeit). `DefectStatus`: OPEN/RESOLVED.

## API-Vertrag

`GET /api/defects`; `POST /api/defects/{id}/resolve`; automatische Erzeugung über Prüfungen und Abschluss über Auftragsfreigabe.

## Beispielablauf

Negativer Prüfpunkt → Mangel OPEN → ein Korrekturauftrag pro Prüfung → Zuweisung und Durchführung → Managerfreigabe → alle zugehörigen offenen Mängel werden transaktional geschlossen.

## Geschäftsregeln, Rechte und Grenzfälle

Pro negativem Punkt entsteht ein eigener Mangel. Ein als `required` markierter Kontrollpunkt erzeugt Schwere HIGH; ein nichtpflichtiger Punkt MEDIUM. Wiederholtes manuelles Schließen ist nicht zulässig. Ein technischer Abschluss ohne Managerfreigabe reicht nicht für die Mangelbehebung.

## Automatisierte Tests

`failureWorkflow`, `resolveDefect`, `lifecycle` und Mangeltests der Demo.

## Bewusste Grenzen und Ausbau

Keine Ursachenklassifikation, Wirksamkeitsprüfung, Eskalation, Anhänge, Wiederöffnung oder zwingend unabhängiger Prüfer. Solche Maßnahmen können rechtlich/fachlich erforderlich sein.

**Querverweise:** [API](../API.md) · [Architektur](../ARCHITECTURE.md) · [Testkonzept](../TESTING.md) · [Roadmap](../ROADMAP.md).
