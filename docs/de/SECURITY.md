# Sicherheitskonzept, Risiken und Härtung

[English](../en/SECURITY.md) · [Übersicht](INDEX.md)

## Geltungsbereich

FacilityFlow ist eine sicherheitsbewusst entwickelte Referenzanwendung für ein Entwicklerportfolio. Sie ist **nicht penetrationsgetestet, zertifiziert oder vollständig produktionsgehärtet**. Eine öffentliche Installation mit echten personenbezogenen Daten braucht die nachstehenden zusätzlichen Maßnahmen und eine Datenschutzprüfung. Die GitHub-Pages-Vorschau verarbeitet ausschließlich synthetische Browser-Daten und ist ausdrücklich **kein geschütztes Mehrbenutzersystem**.

## Anmeldung und Sitzungsverwaltung

- Kennwörter werden mittels **BCrypt** mit Kostenfaktor 12 gespeichert; Hashes erscheinen nicht in den API-Teamansichten (`UserView`).
- Die Benutzeridentifikation erfolgt anhand einer global eindeutigen E-Mail; das Konto muss aktiviert sein.
- Bei erfolgreicher Anmeldung wird die Session-ID erneuert und der Security Context in einer HTTP-Session gespeichert.
- Session-Cookie: HTTP-only, SameSite Lax, Ablauf nach 45 Minuten, Secure-Flag über `SESSION_COOKIE_SECURE=true` unter HTTPS.
- Abmeldung invalidiert die Session.
- Es gibt keine öffentliche Registrierung. Benutzer können nur von `ADMIN` angelegt werden.
- **Noch offen:** Passwortzurücksetzung, Login-Rate-Limit, Kontosperre, E-Mail-Verifikation, MFA, OIDC/SSO, Kennwortwechsel bei erster Anmeldung, Benutzer-Deaktivierung.

## CSRF und Browsergrenzen

Spring Security verwendet `CookieCsrfTokenRepository.withHttpOnlyFalse()` und einen expliziten Token-Request-Handler. `/api/auth/csrf` liefert einen Token; der Browser sendet den Cookie `XSRF-TOKEN` über den Header `X-XSRF-TOKEN` bei POST/PUT. Nach der Anmeldung wird der Token erneut abgerufen. Dieser lesbare **CSRF-Cookie ist nicht der geheime Session-Cookie**.

Eine restriktive Content Security Policy erlaubt Skripte und Ressourcen grundsätzlich nur aus eigener Herkunft, verhindert Einbettung in fremde Frames und blockiert Objektinhalte. Dynamische Texte werden in der Oberfläche über die Escape-Funktion behandelt. Zusätzlich sind DOM-basierte Sicherheitstests und regelmäßige Abhängigkeitsprüfungen erforderlich.

## Rollen und Mandanten

Alle `/api/**`-Routen außer Login und CSRF-Vorbereitung erfordern Anmeldung. `@PreAuthorize` kontrolliert die Rolle. `CurrentUser` bestimmt `organization_id` aus dem authentifizierten Konto; Services verwenden mandantenspezifische Datenbankabfragen und prüfen referenzierte Standorte, Anlagen sowie Mitarbeiter auf denselben Mandanten.

Ein Manager von Unternehmen A darf auch mit gültiger Anmeldung die Anlage von Unternehmen B nicht lesen. Dazu reicht eine zufällig erratene UUID nicht aus. Techniker dürfen Aufträge nur im Rahmen ihrer Zuweisung beginnen und abschließen. PDF-Endpunkte laden ihre Daten ebenfalls über abgesicherte Services.

**Tiefenabsicherung:** Die FK-Spalten der Kindtabellen referenzieren derzeit primär IDs und erzwingen die Mandantengleichheit nicht automatisch auf Datenbankebene. Composite Foreign Keys oder PostgreSQL Row-Level-Security wären eine zusätzliche Schutzschicht. Sie sind **noch nicht implementiert**.

## Gefahren und Gegenmaßnahmen

| Bedrohung | Vorhandene Maßnahme | Weiterer Bedarf |
|---|---|---|
| Unberechtigter API-Aufruf | Session und Rollen | Login-Limits, MFA/SSO, Alarmierung |
| Zugriff auf fremde Datensätze | Mandantengefilterte Queries | Security-Property-Tests, Composite FKs/RLS |
| CSRF | Cookie/Token-Header, Sessionwechsel | Proxy- und Browserprüfung |
| XSS | HTML-Escaping, CSP, keine externen JS-CDNs | Browser-DOM-Test und Dependency-Scan |
| Doppelte Wartungsaufträge | Unique Constraint und pessimistische Sperre | Mehrinstanzen-Test, Lock-Monitoring |
| SQL-Injection | Parametrisierte JPA-Abfragen | Spätere native Queries gesondert prüfen |
| Offenlegung von Geheimnissen | `.env` in `.gitignore`, Umgebungsvariablen | Secret-Manager, Rotation, Scans |
| Unbeabsichtigter DB-Zugang | Privates Docker-Netz, kein öffentlicher Port | Firewall, Zugriffskontrolle, Patchmanagement |
| Datenverlust | PostgreSQL-Volume | Offsite-Backup und Restore-Übungen |
| Audit-Manipulation | Anwendungsseitig fortlaufende Ereignisse | Unveränderbares externes Journal/WORM |
| Datenschutzverletzung | Rollen, Mandant, keine Hashes in Teamantworten | Löschkonzept, Retention, DSGVO-Prüfung |
| Überlastung | Validierung, begrenzter Endpointumfang | Ratenbegrenzung, Paginierung, Ressourcenlimits |

## Datenschutz und deutsche Anforderungen

Namen, E-Mail-Adressen, Mitarbeiterzuweisungen und Tätigkeitsaufzeichnungen können personenbezogene Daten sein. Ein realer Betreiber muss Rollen als Verantwortlicher/Auftragsverarbeiter, Rechtsgrundlage, Auskunftsrechte, Datenminimierung, Aufbewahrung, Löschung, Drittstaatentransfers, Zugriffsberechtigungen und ggf. Mitbestimmung prüfen. EU-Hosting allein stellt keine DSGVO-Konformität sicher. Ein internes Audit-Protokoll ist **kein rechtssicherer Prüfnachweis mit qualifizierter Signatur**.

Quellen: [DSGVO](https://eur-lex.europa.eu/eli/reg/2016/679/oj), [BetrSichV](https://www.gesetze-im-internet.de/betrsichv_2015/), [BSI](https://www.bsi.bund.de/), [OWASP ASVS](https://owasp.org/www-project-application-security-verification-standard/).

## Pflichtaufgaben vor produktivem Betrieb

1. Beispieldaten deaktivieren und feste Benutzer vollständig entfernen.
2. Sichere individuelle Administrator-Zugangsdaten und Secret-Manager einsetzen.
3. HTTPS, Secure-Cookies, Firewall und private Datenbankzugriffe konsequent absichern.
4. Datenschutzgerechte Protokollierung, Monitoring und Alarmierung bereitstellen.
5. MFA/SSO, Rate Limiting und geregelten Konto-Lebenszyklus ergänzen.
6. DSGVO-konforme Aufbewahrung, Export und Löschung sowie anlagenbezogene Prüfvorschriften umsetzen.
7. Verschlüsselte externe Backups regelmäßig auf Wiederherstellbarkeit prüfen.
8. Schwachstellenscans, Penetrationstest und OWASP-ASVS-Review durchführen.
9. Paginierung, Mandantenkontingente, Lasttests und Scheduler-Überwachung ergänzen.
10. Sicherheitsvorfälle und Wiederanlauf in organisatorischen Runbooks dokumentieren.

Sicherheitslücken bitte nicht öffentlich inklusive personenbezogener Daten oder Passwörter melden. Siehe `SECURITY.md` im Repository-Verzeichnis.
