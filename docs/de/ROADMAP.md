# Roadmap, Grenzen und Produktionsreife

[English](../en/ROADMAP.md) · [Übersicht](INDEX.md)

FacilityFlow v1 ist eine funktionsfähige Portfolio-Referenz für einen klar abgegrenzten Prozess. Die Roadmap unterscheidet ausdrücklich zwischen **bereits realisierten Funktionen** und **offenen Aufgaben** für einen professionellen Betreiber.

## In v1 umgesetzt

- Session-Anmeldung mit Rollen- und Mandantenprüfung.
- Gebäudeverwaltung, Anlagenkataster, PostgreSQL-Constraints und Audit-Ereignisse.
- Wartungsintervalle mit Scheduler und manueller Auslösung sowie Dublettenprävention.
- Statusautomat mit getrennter Ausführung und Managerfreigabe.
- Prüfvorlagen, strukturierte Prüfergebnisse, automatische Mängel und Korrekturaufträge.
- Abschluss offener Mängel nach Korrekturfreigabe.
- Deutsch-/englischsprachige PDF-Betriebsberichte und responsives Interface.
- Statische Browser-Vorschau mit synthetischen Daten, ohne Live-Zugänge.
- Docker Compose/Caddy, GitHub-Pages-Workflow, Node-Tests und Java-Testcontainers-Testquellen.

## Stufe 1 — Technische Härtung vor echtem Pilotbetrieb

| Bereich | Maßnahme | Akzeptanz |
|---|---|---|
| Benutzer | MFA/SSO, Passwortreset, Rate Limiting, Kontosperre | Sichere Kontoführung, widerrufene Zugänge wirken nicht mehr |
| Datenschutz | Backups, Restore, Retention, Auskunft und Löschung | Nachweislich wiederherstellbare Daten und geprüfte Richtlinie |
| Schnittstelle | DTOs, Paginierung, OpenAPI, Sortierung | Versionierte API und stabile Antwortformate |
| Anwendungssicherheit | Secret-Manager, Scans, Security-Tests | Audit ohne offene kritische Schwachstellen |
| Betrieb | Metriken, Logs, Job-Alarmierung, Fehlerüberwachung | Fehlgeschlagene Wartungsjobs fallen zuverlässig auf |
| Berichte | Unicode-Schrift, Versionierung, Langzeitarchiv | Umlaute und längere Texte druckbar; Aufbewahrung geregelt |
| Qualität | Browser-E2E, Paralleltests, Lasttest, WCAG | Grüne CI und nachvollziehbare Qualitätskriterien |

## Stufe 2 — Marktnahe Erweiterungen

- SLA- und Eskalationsregeln mit E-Mail, SMS und In-App-Benachrichtigungen.
- Sichere Foto- und Dokumentenanhänge mit Malware-Prüfung und Objektspeicher.
- QR-Aufkleber für Anlagen mit Aufruf der Wartungshistorie.
- Externe Dienstleisterportale und mehrstufige Freigaben.
- Gesetzlich und vertraglich konfigurierbare Prüffristen.
- Abnahme- und Leistungsnachweise für Subunternehmer.
- Gebäudeplan-/Kartenansicht, erweiterte Filter und Berichte.

## Stufe 3 — Plattform- und Systemintegration

- Kundenkonfigurierbare BPMN-2.0-Prozessmodelle und Genehmigungswege.
- OIDC/SAML und Signaturdienst-Anbindung.
- Offline-fähige Techniker-App mit Konfliktbehandlung.
- IoT-Ereignisse, Message Queue und idempotente Verbraucher.
- CAFM/ERP-/Buchhaltungsschnittstellen sowie Import-/Exportformate.
- Mehrknotenbetrieb, robuste Scheduler-Koordination und observierbare Hintergrundjobs.

## Nicht Bestandteil der Version 1

Keine behördlich geprüften oder zertifizierten BetrSichV-/DGUV-Prüfbescheinigungen, keine qualifizierte elektronische Signatur, keine manipulationssichere Archivierung, kein automatisches Backup, kein E-Mail-Versand, keine Fotos/Anhänge, kein Sensor-Gateway und keine öffentliche Registrierung. GitHub Pages ist nur eine **statische Simulation**, keine Java-Hosting-Plattform.

## Checkliste zur Portfolio-Veröffentlichung

- [ ] `npm run check:js` erfolgreich.
- [ ] `mvn verify` mit Java 21 und Docker erfolgreich.
- [ ] Beide Sprachen fachlich gegengelesen.
- [ ] Responsive Ansichten auf 375px, 768px und 1440px geprüft.
- [ ] GitHub-Pages-Workflow aktiviert und Live-URL getestet.
- [ ] Dependabot/Sicherheitsscans im Repository eingeschaltet.
- [ ] Lizenz, CONTRIBUTING, Screenshots und Demo-Link geprüft.
- [ ] Keine `.env`-Datei oder vertrauliche Daten veröffentlicht.

Für kommerzielle Nutzung sind außerdem fachliche Festlegung von Prüfqualifikationen und Prüffristen, Rechts-/DSGVO-Prüfung, abgesicherter Betrieb und Datenschutzkonzept erforderlich.
