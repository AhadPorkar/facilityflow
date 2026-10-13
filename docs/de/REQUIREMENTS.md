# Fachliche und technische Anforderungen (PRD) — FacilityFlow

[English](../en/REQUIREMENTS.md) · [Übersicht](INDEX.md)

**Version:** 1.0 Portfolio-Implementierung · **Zielmarkt:** Technisches Facility Management in Deutschland · **Standardsprache:** Englisch · **Weitere Sprache:** Deutsch

## 1. Ausgangslage und Nutzergruppen

In der technischen Gebäudebewirtschaftung müssen Anlagen, Wartungstermine, Arbeitseinsätze, Sicherheitsprüfungen, Mängel und Freigaben koordiniert werden. Nicht nur die Dateneingabe verursacht Aufwand: Auch Übergaben, fehlende Nachweise, Nachfragen und verspätete Maßnahmen erhöhen die Prozesskosten.

| Rolle / Persona | Ziel | Herausforderung | Unterstützung |
|---|---|---|---|
| Facility-Manager | Gebäudebetrieb steuern und Leistungen freigeben | Fehlende standortübergreifende Transparenz | Dashboard, Wartungspläne, Freigabe, Mängel |
| Wartungsdisponent | Einsätze planen und verteilen | Manuelle Terminverfolgung | Fälligkeitsjob, Zuweisung, Status |
| Techniker | Einsätze und Prüfungen dokumentieren | Papierformulare und fehlende Historie | Responsive Formulare, Prüflisten, Abschluss |
| Qualitäts-/Sicherheitsverantwortlicher | Kontrollen nachweisen | Dokumente in verschiedenen Systemen | Vorlagen, Prüfergebnisse, Mängel, PDF |
| Leitung / Beobachter | Betriebszustand beurteilen | Keine konsolidierte Übersicht | Lesender Dashboard-Zugriff |

## 2. Ziele und Abgrenzung

**Ziele:** Durchgängiger digitaler Ablauf von Wartung bis Freigabe; wirksame Rollen- und Mandantentrennung; Vermeidung doppelter Wiederholungsaufträge; strukturierte Prüfprotokolle; zweisprachige PDF-Berichte; leicht zugängliche Demonstration für Recruiter.

**Nicht Bestandteil von Version 1:** Buchhaltung, HR-Disposition, BIM/CAFM-Vollintegration, öffentliche Genehmigungsverfahren, rechtssichere qualifizierte elektronische Signatur, IoT-Sensorik, Marktplatz für Dienstleister, zertifizierte Prüfverfahren und Offline-Synchronisation.

**Beispielhafte Kennzahlen für einen Piloten** (Zielgrößen, keine gemessenen Erfolge): fristgerecht erzeugte Wartungen, Dauer bis zur Zuweisung, Freigabezeit, offene kritische Mängel, Vollständigkeit von Prüflisten, Minutenersparnis bei der Berichterstellung.

## 3. Funktionale Anforderungen und Akzeptanzkriterien

| ID | Anforderung | Akzeptanzkriterium |
|---|---|---|
| FR-01 | Sichere Anmeldung | Korrekte Zugangsdaten erzeugen Session; falsche Daten führen zu 401 |
| FR-02 | Berechtigungen | VIEWER darf nicht schreiben; ADMIN verwaltet Benutzer; MANAGER darf freigeben |
| FR-03 | Mandantentrennung | Fremde Standort-/Anlagen-/Benutzer-IDs können nicht ausgelesen oder referenziert werden |
| FR-04 | Standorte | Manager können Gebäude mit mandanteneindeutiger Standortkennung verwalten |
| FR-05 | Anlagen | Jede Anlage gehört zu einem Standort desselben Mandanten |
| FR-06 | Wartungspläne | Intervalle von 1 bis 3650 Tagen und nächste Fälligkeit konfigurierbar |
| FR-07 | Fälligkeitsverarbeitung | Pro Plan und Termin entsteht höchstens ein Auftrag; nächster Termin wird fortgeschrieben |
| FR-08 | Auftragsstatus | Ungültige Übergänge und unberechtigte Durchführung werden abgewiesen |
| FR-09 | Freigabe | Ausschließlich Manager/Administratoren können abgeschlossene Aufträge freigeben |
| FR-10 | Prüfvorlagen | 1 bis 30 Prüfpunkte; optionale Beschränkung auf Anlagentyp |
| FR-11 | Prüfdurchführung | Jeder Prüfpunkt muss genau einmal beantwortet werden |
| FR-12 | Korrekturworkflow | Nicht bestandene Punkte erzeugen Mängel und einen verknüpften Korrekturauftrag |
| FR-13 | Mangelabschluss | Manuelle Bestätigung oder Freigabe des Korrekturauftrags schließt offene Mängel |
| FR-14 | PDF-Bericht | Prüf- und Auftragsbericht auf Englisch oder Deutsch herunterladbar |
| FR-15 | Audit | Ereignisse enthalten Mandant, Benutzer, Zeit und Aktion |
| FR-16 | Kennzahlen | Standort-/Anlagen-/Auftrags-/Mangelzahlen sind mandantenbezogen |
| FR-17 | Sprache | Englisch ist Standard, Deutsch per Umschalter verfügbar |
| FR-18 | Vorschau | Öffentliche synthetische Daten sind im Browser ohne Anmeldung nutzbar |

## 4. Qualitäts- und Sicherheitsanforderungen

**Sicherheit:** BCrypt-Kennwortspeicherung (Kostenfaktor 12), HTTP-only-Session-Cookie, SameSite Lax, Secure-Cookie unter HTTPS, CSRF-Absicherung von Schreiboperationen, rollenbasierte Autorisierung, Mandantenfilter, keine eingecheckten Secrets und keine öffentlich freigegebene PostgreSQL-Portweiterleitung.

**Zuverlässigkeit:** Geschäftslogik transaktional; PostgreSQL-Fremdschlüssel und Eindeutigkeitsbedingungen; Schutz gegen doppelte Wartungsaufträge durch `(maintenance_plan_id, scheduled_for)`; pessimistisches Sperren bei der Fälligkeitsermittlung. Eine hochverfügbare, verteilte Scheduler-Lösung ist nicht Teil von v1.

**Leistung:** Indizes für Mandant, Status und Fälligkeit. Listen werden in v1 noch ohne serverseitige Paginierung geliefert und wurden nicht für große Datenbestände lastgetestet.

**Barrierefreiheit:** Responsives Layout und tastaturbedienbare Tabellenzeilen sind vorhanden. Eine vollständige WCAG-2.2-AA-Prüfung steht aus.

**Internationalisierung:** Anzeige und Berichte unterstützen Englisch/Deutsch. Technische Statuscodes bleiben für die API sprachneutral. Die derzeitigen PDF-Standardschriften unterstützen nicht sämtliche internationalen Zeichen; Details im Berichtsmodul.

## 5. Fachliche Ablaufbeispiele

### UC-1 Regelmäßige Wartung

Manager erfasst Gebäude und Lüftungsanlage → legt 90-Tage-Plan an → täglicher UTC-Job erzeugt fälligen Arbeitsauftrag → Techniker übernimmt und dokumentiert Ausführung → Manager gibt Auftrag frei → PDF-Bericht und Aktivitätsverlauf sind verfügbar.

**Ausnahmen:** Inaktive Pläne werden übersprungen; ohne Mitarbeiterzuweisung bleibt ein Auftrag OPEN; ungültige Statuswechsel werden abgewiesen; ein erneuter Lauf erzeugt keinen Doppelauftrag. Verpasste Intervalle werden in v1 zu einem Nachholauftrag zusammengefasst.

### UC-2 Prüfung mit festgestelltem Mangel

Manager erstellt Prüfliste → Techniker wählt Anlage und passende Vorlage → alle Punkte werden beantwortet → fehlerhafte Punkte erzeugen Mängel und einen Korrekturauftrag → nach Durchführung und Freigabe werden die verknüpften Mängel geschlossen. Der ursprüngliche negative Prüfbericht bleibt als historischer Befund erhalten.

**Ausnahmen:** Falscher Anlagentyp, fehlende oder doppelte Antworten, deaktivierte Vorlagen und fremde Mandantenreferenzen werden zurückgewiesen.

### UC-3 Mandantenschutz

Ein Nutzer von Mandant A ruft eine fremde Standort-ID auf und erhält 404. Auch beim Erstellen eines Auftrags mit fremdem Standort wird 404 zurückgegeben. Die API erlaubt keine Angabe von `organizationId` in den Request-Feldern.

## 6. Umsetzung und weiterer Ausbau

- **P0 umgesetzt:** FR-01 bis FR-18 nach den dokumentierten Einschränkungen einschließlich Frontend-Demo, Compose-Konfiguration und Tests.
- **P1 vor produktivem Einsatz:** Backup-/Restore-Übungen, Monitoring, Paginierung, Lasttests, Sicherheitsprüfung, SSO/MFA bei erhöhtem Schutzbedarf, moderne PDF-Schriften, Dateiablage mit Virenprüfung.
- **P2:** SLA-Regeln, Benachrichtigungen, Lieferantenportal, QR-Tags, konfigurierbare BPMN-Freigaben, Offline-App, IoT-Impulse und Schnittstellen zu CAFM/ERP-Systemen.

## 7. Rechtlicher und fachlicher Kontext

- [Betriebssicherheitsverordnung (BetrSichV)](https://www.gesetze-im-internet.de/betrsichv_2015/)
- [DGUV](https://www.dguv.de/)
- [BSI](https://www.bsi.bund.de/)
- [DSGVO](https://eur-lex.europa.eu/eli/reg/2016/679/oj)

Die Vorschriften begründen fachliche Anforderungen, ersetzen aber keine anlagenbezogene Rechtsprüfung. FacilityFlow behauptet **keine Prüf- oder Compliance-Zertifizierung**.
