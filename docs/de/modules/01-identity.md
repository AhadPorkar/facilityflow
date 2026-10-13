# Identität, Mandanten und Rollen

[English](../../en/modules/01-identity.md) · [Deutsch](../../de/modules/01-identity.md) · [Übersicht](../INDEX.md)

## Fachlicher Zweck

Die Anmeldung verknüpft jeden Mitarbeiter fest mit einem Mandanten. Eine rollenbasierte Rechteprüfung schützt die gemeinsame Gebäude- und Anlagenverwaltung.

## Beteiligte Rollen

ADMIN, MANAGER, TECHNICIAN und VIEWER; nur ADMIN darf Benutzer anlegen.

## Fachliche Daten / Implementierung

`organization`, `app_user`, `PrincipalUser`, `CurrentUser`, `SecurityConfig`, `TeamService`.

## API-Vertrag

`GET /api/auth/csrf`; `POST /api/auth/login`; `GET /api/auth/me`; `POST /api/auth/logout`; `GET /api/team`; `POST /api/team` (nur ADMIN).

## Beispielablauf

Browser ruft CSRF-Token ab → Anmeldung per E-Mail/Kennwort → BCrypt-Prüfung und neue Session-ID → aktuelle Organisation wird aus der Identität ermittelt → Rolle bestimmt schreibende Funktionen → Abmeldung löscht die Session.

## Geschäftsregeln, Rechte und Grenzfälle

E-Mail-Adressen sind global eindeutig; Erstkennwörter sind 12–72 Zeichen lang. VIEWER ist lesend. Ein Techniker bearbeitet nur ihm zugewiesene Arbeitsaufträge. Teamantworten enthalten keine Passwort-Hashes. Erstadministrator und lokales Demo-Konto sind keine öffentliche Registrierung.

## Automatisierte Tests

`authenticationIsRequired`, `loginWorks`, `viewerIsReadOnly`, `teamRoleBoundary`, `noCrossTenantSites` und Vorschautests.

## Bewusste Grenzen und Ausbau

SSO, MFA, Passwortreset, Anmeldelimits, Benutzerdeaktivierung und Mandanten-Self-Service sind noch nicht enthalten. Beispieldaten niemals öffentlich aktivieren.

**Querverweise:** [API](../API.md) · [Architektur](../ARCHITECTURE.md) · [Testkonzept](../TESTING.md) · [Roadmap](../ROADMAP.md).
