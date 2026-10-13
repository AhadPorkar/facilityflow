# Responsives zweisprachiges Interface und interaktive Vorschau

[English](../../en/modules/11-ui-preview.md) · [Deutsch](../../de/modules/11-ui-preview.md) · [Übersicht](../INDEX.md)

## Fachlicher Zweck

Die Oberfläche zeigt ein professionell gestaltetes Dashboard, klare Navigation, strukturierte Formulare und zusammenhängende Geschäftsprozesse auf Englisch und Deutsch.

## Beteiligte Rollen

Recruiter, alle angemeldeten Mitarbeiter und potenzielle Kunden im Gebäudemanagement.

## Fachliche Daten / Implementierung

`static/index.html`, `static/css/app.css`, `static/js/app.js`, `i18n.js`, `api.js`, `preview-data.js`, `utils.js`, `icons.js`.

## API-Vertrag

Clientseitige Ansichten: `/#/dashboard`, `/#/orders`, `/#/sites`, `/#/assets`, `/#/plans`, `/#/inspections`, `/#/templates`, `/#/defects`, `/#/reports`, `/#/activity`, `/#/team`, `/#/settings`.

## Beispielablauf

Besucher öffnet GitHub-Pages-Demo → der Vorschauadapter erzeugt Gebäude, Anlagen, Wartungen, Aufträge und Personen → Formulare ermöglichen Anlage, Zuweisung, Statuswechsel, Prüfung oder Mangelabschluss → Änderungen werden im lokalen Browserspeicher gehalten → EN/DE-Schalter speichert die Sprachwahl.

## Geschäftsregeln, Rechte und Grenzfälle

Englisch ist Standard. `*.github.io` oder `?preview=1` aktiviert die synthetische Vorschau, sonst wird die echte Spring-API verwendet. Eingaben werden HTML-sicher dargestellt. Das Layout reagiert auf kleinere Bildschirme; ES-Module laufen ohne Build-Tool. Die öffentliche Demo besitzt keine echte Mehrbenutzerberechtigung. Berichte werden in der Vorschau über Browser-Druck behandelt, im Backend als PDF geladen.

## Automatisierte Tests

`frontend-tests/locale-and-utils.test.js` und `frontend-tests/preview-workflow.test.js` mit insgesamt 13 Node-Tests. Zusätzliche echte Browser-/Responsive-Tests sind empfehlenswert.

## Bewusste Grenzen und Ausbau

Keine vollständige WCAG-Zertifizierung, Offline-Synchronisation, garantierte Dialog-Fokusführung, automatisierte Screenshottests, Push-Benachrichtigungen oder native Mobil-App.

**Querverweise:** [API](../API.md) · [Architektur](../ARCHITECTURE.md) · [Testkonzept](../TESTING.md) · [Roadmap](../ROADMAP.md).
