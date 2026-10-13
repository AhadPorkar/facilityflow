# Teststrategie und Abdeckungsmatrix

[English](../en/TESTING.md) · [Übersicht](INDEX.md)

## 1. Testkonzept

FacilityFlow testet zentrale Geschäftsprozesse als **HTTP-Integrationstests gegen eine echte PostgreSQL-Instanz**. Damit werden Anmeldung, Berechtigungen, Flyway-Datenbankschema, Controller und transaktionale Services gemeinsam untersucht. Eine zweite Node.js-Testsuite prüft Übersetzungen, UI-Hilfsfunktionen und die Geschäftslogik der synthetischen Browser-Demo. Last-, Sicherheits-, Barrierefreiheits- und echte Browser-End-to-End-Tests bleiben als Erweiterung vorgesehen.

```text
Schicht 1: JUnit 5 + MockMvc + Spring Boot + Testcontainers/PostgreSQL 16
  - Authentifizierung, Datenbank, Mandantenfilter und fachliche Prozessabläufe
Schicht 2: node:test + assert/strict (Node 22)
  - Wörterbücher, Escaping, Datumshelfer, Demo-Prozesse
Schicht 3: Automatische Syntaxprüfung über npm run check:js
  - JavaScript-Dateien auf Syntaxfehler prüfen
```

## 2. Testausführung

```bash
# Java 21, Maven 3.9+ und laufender Docker-Daemon erforderlich.
mvn --batch-mode --no-transfer-progress verify

# Node.js 22; kein npm install und keine externen NPM-Abhängigkeiten nötig.
npm run check:js
```

Die Backendtests in `FacilityFlowIntegrationTest` verwenden `@Testcontainers`, PostgreSQL 16, `@DynamicPropertySource`, `@SpringBootTest` und `MockMvc`. Die Datenbank wird für die Tests temporär gestartet und durch Flyway migriert, ohne eine reale Nutzerdatenbank zu berühren. Ist Docker nicht verfügbar, können diese Tests nicht ausgeführt werden und dürfen nicht als erfolgreich bezeichnet werden.

## 3. Testfälle je Backendmodul

| Modul | Testmethoden | Überprüft |
|---|---|---|
| Anmeldung/Session | `authenticationIsRequired`, `loginWorks` | 401 ohne Session, BCrypt-Anmeldung, Benutzerrolle |
| Rechte | `viewerIsReadOnly`, `teamRoleBoundary`, `transitionGuard` | Nur lesender Viewer, ADMIN-Anlage von Konten, zugewiesener Techniker |
| Mandantentrennung | `noCrossTenantSites`, `assetsCannotCrossTenant`, `analyticsAreTenantScoped` | Fremde Gebäude und Anlagen nicht erreichbar, Kennzahlen getrennt |
| Standorte | `sitesCrud` | Anlegen 201, aktualisieren und anzeigen |
| Anlagen | `assetsCannotCrossTenant` | Fremdstandort verweigert, eigener Standort zulässig |
| Wartung | `scheduledJobIdempotency` | Fälliger Auftrag einmalig; erneuter Lauf erstellt keinen Doppelauftrag |
| Arbeitsaufträge | `transitionGuard`, `lifecycle` | Ungültiger Status 422; Beginn, Abschluss, Freigabe |
| Prüfvorlagen | `checklistCreation` | Neue Vorlage mit zwei Prüfpunkten |
| Prüfungen | `failureWorkflow`, `incompleteInspection` | Negatives Ergebnis erzeugt Mangel/Korrekturauftrag; fehlende Antwort 422 |
| Mängel | `resolveDefect`, `failureWorkflow` | Mangel kann durch Manager geschlossen werden |
| Kennzahlen/Audit | `analyticsAreTenantScoped`, `auditEvents` | Mandantenbezogene KPI, Aktivitätsprotokoll und Leserechte |
| PDF | `downloadableReport` | HTTP 200, PDF-Format und `%PDF-`-Signatur |
| Mitarbeiter | `teamRoleBoundary` | Benutzeranlage nur mit ADMIN-Rolle |

## 4. Frontendtests

`locale-and-utils.test.js` prüft die Konsistenz und Benutzung der EN-/DE-Übersetzungen, Escaping, Datumshelfer und Kürzel. `preview-workflow.test.js` testet Initialdaten, Auftragsstatus, einmalige Wartungserzeugung, fehlgeschlagene Prüfung und Mangelbehebung. Die im Projekt enthaltene Node-Suite umfasst **13 automatisierte Tests**.

## 5. Continuous Integration

`.github/workflows/ci.yml` startet bei jedem Push oder Pull Request zwei unabhängige Jobs: `frontend` mit Node.js 22 und `npm run check:js` sowie `backend` mit Temurin Java 21 und `mvn verify`. Scheitert Maven, werden Surefire-Testberichte als Artefakt gespeichert. Die PostgreSQL-Instanz wird durch Testcontainers auf dem Docker-Daemon des GitHub-Ubuntu-Runners gestartet.

**Wichtig:** Erst das tatsächliche grüne CI-Ergebnis bestätigt eine erfolgreiche Java-/PostgreSQL-Testausführung. Testdateien allein sind kein Nachweis dafür.

## 6. Empfohlene zusätzliche Tests

| Priorität | Test | Zweck |
|---|---|---|
| P0 | Browser-E2E mit Playwright | Klickabläufe und Session-/CSRF-Verhalten |
| P0 | Fremdmandant bei allen Berichts- und Schreibendpunkten | Ausweitung der Berechtigungstests |
| P0 | Deterministische Scheduler-Grenzwerte mit festem Clock | Zeitzonen, Überfälligkeit, Schaltjahre |
| P0 | Automatisches Mängelschließen bei Freigabe explizit testen | Wichtiges fachliches Ergebnis absichern |
| P1 | Parallele Wartungsgenerierung in zwei Transaktionen | Sperren und Unique-Constraint unter Last |
| P1 | Künstlicher Fehler in der Prüftransaktion | Keine halbfertigen Datensätze |
| P1 | Parametrisierte Statusautomaten-Matrix | Alle Rollen und ungültigen Übergänge |
| P1 | WCAG-Test und Screenreader-Check | Barrierefreiheit |
| P1 | OWASP-Tests, Ratenlimits, Schwachstellenscan | Web- und API-Sicherheit |
| P1 | Lasttest mit 10.000 Anlagen und 100.000 Aufträgen | Paginierungs-/Indexbedarf |
| P2 | Visuelle Regressionstests | Responsives UI stabil halten |
| P2 | Wiederherstellungstest aus Sicherung | Betriebssicherheit |

## 7. Fehlersuche

- Docker-Umgebung nicht gefunden: Docker Desktop/Daemon starten und `docker info` prüfen.
- Flyway-Fehler: PostgreSQL-Version, Migrationsname und manuelle Schemaänderungen prüfen.
- Java-Compilerfehler: Java 21 mit `mvn -v` kontrollieren; Maven-Central-Erreichbarkeit sicherstellen.
- Statische Demo bleibt leer: ES-Module über lokalen HTTP-Server statt `file://` laden.
- CSRF-403 bei curl: `/api/auth/csrf` abrufen, Cookies speichern und `X-XSRF-TOKEN` mitsenden.

Der Quellcode ist auf reproduzierbare CI-Läufe ausgerichtet. Maven-Tests müssen auf einer Umgebung mit Docker und Maven Central **tatsächlich ausgeführt** werden, bevor sie als bestanden gelten.
