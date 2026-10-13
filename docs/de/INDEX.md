# FacilityFlow — Dokumentationsübersicht (Deutsch)

[**English**](../en/INDEX.md) · [Deutsche README](../../README.de.md)

FacilityFlow ist ein modularer Spring-Boot-Monolith für den technischen Gebäudebetrieb. Die Dokumentation beschreibt jeweils die **tatsächlich implementierten Funktionen**, deren Tests und Abgrenzungen. Die öffentliche Browser-Demo verwendet synthetische Daten im lokalen Browser-Speicher; die vollständige Anwendung arbeitet mit PostgreSQL und authentifizierten Sessions.

## Einstieg

| Dokument | Inhalt |
|---|---|
| [Anforderungen](REQUIREMENTS.md) | Zielgruppen, Anwendungsfälle, Rollen, Akzeptanzkriterien, deutscher Markt |
| [Architektur](ARCHITECTURE.md) | Komponenten, ER-Beziehungen, Transaktionen, Statusautomat, Entwurfsentscheidungen |
| [REST-API](API.md) | Endpunkte, JSON-Beispiele, Anmeldung, CSRF, Fehlermeldungen |
| [Bereitstellung](DEPLOYMENT.md) | Docker Compose, Caddy mit HTTPS, GitHub Pages, Railway, Geheimnisse, Wiederherstellung |
| [Sicherheit](SECURITY.md) | Bedrohungsmodell, Rollen, Mandantentrennung, offene Sicherheitsmaßnahmen |
| [Tests](TESTING.md) | Testbefehle, Abdeckungsmatrix, Integrationsszenarien, CI |
| [Roadmap](ROADMAP.md) | Ausbaustufen und Checkliste für den Produktiveinsatz |

## Moduldokumentation (jeweils auch auf Englisch)

1. [Benutzer und Mandanten](modules/01-identity.md)
2. [Standorte und Gebäude](modules/02-sites.md)
3. [Anlagen und Inventar](modules/03-assets.md)
4. [Präventive Wartung](modules/04-maintenance.md)
5. [Arbeitsaufträge und Freigaben](modules/05-work-orders.md)
6. [Prüfvorlagen](modules/06-templates.md)
7. [Prüfungen](modules/07-inspections.md)
8. [Mängel und Korrekturmaßnahmen](modules/08-defects.md)
9. [Berichterstellung](modules/09-reporting.md)
10. [Kennzahlen und Audit-Protokoll](modules/10-insights-audit.md)
11. [Zweisprachige Oberfläche und Vorschau](modules/11-ui-preview.md)

## Begriffe und Konventionen

- `organization_id` kennzeichnet einen Mandanten; die API begrenzt Lese- und Schreibzugriffe auf dessen Daten.
- Eine `UUID` dient der Identifikation, **nicht** als Zugriffsberechtigung.
- Datumsangaben verwenden `YYYY-MM-DD`; Zeitpunkte werden in UTC gespeichert.
- Der Status eines Arbeitsauftrags wird serverseitig geprüft und kann nicht beliebig geändert werden.
- Request-DTOs akzeptieren keine fremde Mandanten-ID; diese wird aus der Session abgeleitet.
- Englisch ist die Standardsprache; die Oberfläche bietet zusätzlich Deutsch mit gespeicherter Auswahl.
- Geplante Wartungen werden in der Backend-Anwendung von einem Spring-Scheduler verarbeitet.

## Beispiel eines Ende-zu-Ende-Prozesses

Ein Facility-Manager legt den Standort **Büro Berlin** an, erfasst eine **Lüftungsanlage** und definiert eine Wartung alle 90 Tage. Ein fälliger Arbeitsauftrag wird automatisch erzeugt und einem Techniker zugewiesen. Der Techniker beginnt und dokumentiert die Arbeiten; anschließend gibt der Manager den Auftrag frei. Wird bei einer gesonderten Prüfung ein Kontrollpunkt nicht bestanden, entstehen ein Mangel und ein Korrekturauftrag. Ein deutschsprachiger PDF-Prüfbericht dokumentiert das Ergebnis. Die zentralen Schritte sind im Audit-Protokoll nachvollziehbar.

## Fachlicher Kontext

- [Betriebssicherheitsverordnung (BetrSichV)](https://www.gesetze-im-internet.de/betrsichv_2015/)
- [Deutsche Gesetzliche Unfallversicherung (DGUV)](https://www.dguv.de/)
- [Spring Boot](https://docs.spring.io/spring-boot/)
- [Spring Security: CSRF](https://docs.spring.io/spring-security/reference/servlet/exploits/csrf.html)
- [Docker Compose](https://docs.docker.com/compose/)

Die Verweise erläutern den fachlichen Hintergrund. Eine **behördliche oder rechtliche Zertifizierung** der Software wird nicht behauptet.
