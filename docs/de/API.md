# REST-API-Handbuch (v1)

[English](../en/API.md) · [Übersicht](INDEX.md)

**Basispfad:** `/api` (gleicher Ursprung). **Format:** JSON über HTTPS; zwei Endpunkte liefern `application/pdf`. **Anmeldung:** HTTP-Session mit CSRF-Token. **Kennungen:** UUID. **Datum:** ISO `YYYY-MM-DD`. **Zeitpunkte:** UTC. Technische Enum-Werte bleiben unabhängig von der Anzeigesprache.

> Das Dokument beschreibt vorhandene Controller, keine geplante Swagger-/OpenAPI-Oberfläche. Eine OpenAPI-Spezifikation ist für den nächsten Ausbauschritt vorgesehen.

## 1. Anmeldung und CSRF

| Methode | Pfad | Berechtigung | Antwort |
|---|---|---|---|
| GET | `/api/auth/csrf` | Öffentlich | `{ "token": "..." }` und Cookie `XSRF-TOKEN` |
| POST | `/api/auth/login` | Öffentlich + CSRF | Authentifizierte Session und Benutzerangaben |
| GET | `/api/auth/me` | Angemeldet | `id`, `name`, `email`, `role`, `organization` |
| POST | `/api/auth/logout` | Angemeldet + CSRF | `{ "success": true }` |

Browser-Ablauf: Zunächst `/api/auth/csrf` abrufen, dann den Cookie-Wert `XSRF-TOKEN` im Header `X-XSRF-TOKEN` bei POST/PUT senden, über `/api/auth/login` anmelden, anschließend CSRF erneut abrufen und die Session-Cookies bei späteren API-Aufrufen mitsenden. Es wird ein `CookieCsrfTokenRepository` mit explizitem Request-Handler eingesetzt.

```bash
# Nur lokale Umgebung. Cookies werden in der Datei gesichert.
curl -s -c cookies.txt http://localhost:8080/api/auth/csrf
TOKEN='token-aus-der-antwort'
curl -b cookies.txt -c cookies.txt -X POST http://localhost:8080/api/auth/login \
 -H 'Content-Type: application/json' -H "X-XSRF-TOKEN: $TOKEN" \
 -d '{"email":"demo@facilityflow.local","password":"DemoPass123!"}'
```

Nach der Anmeldung vor weiteren Schreibzugriffen CSRF neu abrufen. Produktive Systeme müssen HTTPS verwenden; Testzugänge gehören nicht in öffentliche Installationen.

## 2. Berechtigungsmatrix

| Funktion | ADMIN | MANAGER | TECHNICIAN | VIEWER |
|---|:---:|:---:|:---:|:---:|
| Eigene Mandantendaten lesen | ✓ | ✓ | ✓ | ✓ |
| Standorte, Anlagen, Wartungspläne, Prüfvorlagen ändern | ✓ | ✓ | — | — |
| Aufträge erstellen, zuweisen, freigeben, stornieren | ✓ | ✓ | — | — |
| Zugewiesenen Auftrag beginnen und abschließen | ✓ | ✓ | ✓* | — |
| Prüfung durchführen | ✓ | ✓ | ✓ | — |
| Mangel manuell schließen | ✓ | ✓ | — | — |
| Audit-Protokoll lesen | ✓ | ✓ | — | — |
| Benutzer anlegen | ✓ | — | — | — |

`*` Techniker dürfen nur den ihnen zugewiesenen Auftrag bearbeiten. Manager und Administratoren können alle Aufträge ihres Mandanten bearbeiten.

## 3. Endpoint-Übersicht

Die folgenden Pfade gelten relativ zu `/api`.

| HTTP | Pfad | Ergebnis / Zweck | Schreibberechtigung |
|---|---|---|---|
| GET | `/sites`, `/sites/{id}` | Standorte auflisten, Detail | Alle angemeldeten |
| POST/PUT | `/sites`, `/sites/{id}` | Standort anlegen / ändern | ADMIN/MANAGER |
| GET | `/assets`, `/assets/{id}` | Anlagenliste und Detail | Alle |
| POST/PUT | `/assets`, `/assets/{id}` | Anlage anlegen / ändern | ADMIN/MANAGER |
| GET | `/maintenance-plans`, `/{id}` | Wartungsplanliste und Detail | Alle |
| POST/PUT | `/maintenance-plans`, `/{id}` | Wartungsplan anlegen / ändern | ADMIN/MANAGER |
| POST | `/maintenance-plans/generate-due` | Fällige Aufträge erzeugen (`created`) | ADMIN/MANAGER |
| GET | `/work-orders`, `/{id}` | Arbeitsaufträge lesen | Alle |
| POST | `/work-orders` | Auftrag erstellen | ADMIN/MANAGER |
| POST | `/work-orders/{id}/assign` | Mitarbeiter zuweisen | ADMIN/MANAGER |
| POST | `/work-orders/{id}/start` | Bearbeitung beginnen | ADMIN/MANAGER/zugewiesener TECHNICIAN |
| POST | `/work-orders/{id}/complete` | Mit Ergebnis abschließen | ADMIN/MANAGER/zugewiesener TECHNICIAN |
| POST | `/work-orders/{id}/approve` | Freigeben, verknüpfte Mängel schließen | ADMIN/MANAGER |
| POST | `/work-orders/{id}/cancel` | Offenen/zugewiesenen Auftrag stornieren | ADMIN/MANAGER |
| GET | `/inspection-templates`, `/{id}` | Prüflisten; Detail als `{template,items}` | Alle |
| POST | `/inspection-templates` | Prüfliste veröffentlichen | ADMIN/MANAGER |
| GET | `/inspections`, `/{id}` | Prüfungen; Detail mit Ergebnissen/Mängeln | Alle |
| POST | `/inspections` | Neue Prüfung durchführen | ADMIN/MANAGER/TECHNICIAN |
| GET | `/defects` | Mängelliste | Alle |
| POST | `/defects/{id}/resolve` | Mangel manuell erledigen | ADMIN/MANAGER |
| GET | `/dashboard` | Kennzahlen und letzte Aktivitäten | Alle |
| GET | `/audit-events` | Letzte 100 Protokollereignisse | ADMIN/MANAGER |
| GET | `/inspections/{id}/report?lang=de` | PDF-Prüfbericht | Alle |
| GET | `/work-orders/{id}/report?lang=en` | PDF-Arbeitsbericht | Alle |
| GET | `/team` | Mitarbeiterliste ohne Passwort-Hashes | Alle |
| POST | `/team` | Mitarbeiter anlegen | ADMIN |

Nicht aufgeführte Lösch-, Bulk- oder Exportendpunkte sind in v1 nicht vorhanden.

## 4. Repräsentative JSON-Aufrufe

### Standort: `POST /api/sites`

```json
{
  "code": "BER-001", "name": "Büro Berlin Mitte",
  "address": "Alexanderplatz 1", "city": "Berlin", "postalCode": "10178",
  "contactName": "Haustechnik", "contactEmail": "technik@example.invalid"
}
```

### Anlage: `POST /api/assets`

`siteId` muss auf einen Standort des aktuellen Mandanten verweisen:

```json
{
  "siteId": "00000000-0000-0000-0000-000000000001",
  "code": "HVAC-01", "name": "Lüftungsgerät Ost",
  "type": "HVAC", "location": "Technikraum Ebene 4",
  "manufacturer": "Beispielhersteller", "status": "ACTIVE",
  "installedOn": "2023-06-15"
}
```

Anlagentypen: `HVAC`, `ELECTRICAL`, `FIRE_SAFETY`, `PLUMBING`, `ELEVATOR`, `OTHER`. Anlagenstatus: `ACTIVE`, `OUT_OF_SERVICE`, `RETIRED`.

### Wartungsplan: `POST /api/maintenance-plans`

```json
{
  "assetId": "00000000-0000-0000-0000-000000000002",
  "title": "Vierteljährliche Lüftungswartung",
  "intervalDays": 90, "nextDueDate": "2026-11-01",
  "instructions": "Filter, Antriebe und Lager prüfen", "active": true
}
```

### Arbeitsauftrag: `POST /api/work-orders`

```json
{
  "siteId": "00000000-0000-0000-0000-000000000001",
  "assetId": "00000000-0000-0000-0000-000000000002",
  "title": "Lüftungsfilter tauschen",
  "description": "Filter wechseln und Zustand protokollieren",
  "priority": "HIGH", "dueDate": "2026-11-05"
}
```

Weiterer Ablauf: `/assign` mit `{"userId":"<UUID>"}` → `/start` (ohne Body) → `/complete` mit `{"resolution":"Filter gewechselt; Luftstrom gemessen"}` → `/approve` (ohne Body). Prioritäten: `LOW`, `MEDIUM`, `HIGH`, `CRITICAL`.

### Prüfvorlage: `POST /api/inspection-templates`

```json
{
  "name": "Lüftungsprüfung", "description": "Sicherheit und Betriebszustand",
  "assetType": "HVAC",
  "items": [
    { "label": "Elektrische Absicherung in Ordnung", "required": true },
    { "label": "Keine ungewöhnliche Vibration", "required": true }
  ]
}
```

Nach `GET /api/inspection-templates/{id}` müssen die gelieferten tatsächlichen Prüfpunkte per UUID vollständig beantwortet werden:

```json
{
  "assetId": "00000000-0000-0000-0000-000000000002",
  "templateId": "00000000-0000-0000-0000-000000000003",
  "notes": "Regelmäßige Anlagenkontrolle",
  "results": [
    { "itemId": "00000000-0000-0000-0000-000000000004", "passed": true },
    { "itemId": "00000000-0000-0000-0000-000000000005", "passed": false, "note": "Lager verursacht Vibration" }
  ]
}
```

Jeder Prüfpunkt muss genau einmal enthalten sein. Fehlerhafte Prüfungen erzeugen einen Korrekturauftrag und Mängel. Fehlende/doppelte Antworten oder fremde Mandantenreferenzen führen zu einer Ablehnung.

### Teammitglied: `POST /api/team`

```json
{
  "displayName": "Nina Technikerin",
  "email": "nina@example.invalid",
  "password": "EinStarkesErstKennwort!",
  "role": "TECHNICIAN"
}
```

## 5. Fehlerantworten

```json
{
  "code": "INVALID_WORKFLOW",
  "message": "Order must be assigned before start",
  "timestamp": "2026-10-13T10:00:00Z"
}
```

| HTTP | Ursache | Fehlercode |
|---|---|---|
| 400 | Ungültige Eingabe / Bean Validation | `VALIDATION_ERROR` |
| 401 | Nicht angemeldet oder falscher Login | `INVALID_CREDENTIALS` beim Login; Security-Entry-Point sonst |
| 403 | Falsche Rolle / nicht zugewiesener Techniker | `ACCESS_DENIED` |
| 404 | Nicht vorhanden oder fremder Mandant | `NOT_FOUND` |
| 409 | Doppelte E-Mail, Kennung oder Fremdschlüsselkonflikt | `DUPLICATE_EMAIL`, `DUPLICATE_OR_REFERENCE` |
| 422 | Fachlich unzulässiger Zustandswechsel | `INVALID_WORKFLOW` |

Frameworkinterne Fehler können das Spring-Problemformat verwenden; der Client sollte sich an den HTTP-Status und die dokumentierten erfolgreichen Datenstrukturen halten.

## 6. Ausbaubedarf

Die API ist derzeit unversioniert. Geplant sind `/api/v1`, stabile Response-DTOs, OpenAPI-Schema, Paginierung, Sortierung, Filter, Korrelations-IDs, Rate Limits und Deprecation-Regeln. Datei-Uploads, Signaturbilder und Massentransaktionen sind derzeit nicht implementiert.
