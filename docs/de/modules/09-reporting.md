# PDF-Betriebsberichte und zweisprachiger Export

[English](../../en/modules/09-reporting.md) · [Deutsch](../../de/modules/09-reporting.md) · [Übersicht](../INDEX.md)

## Fachlicher Zweck

Prüf- und Arbeitsauftragsberichte werden direkt aus den gespeicherten Vorgangsdaten erzeugt, ohne einen zusätzlichen Dokumentendienst zu benötigen.

## Beteiligte Rollen

Facility-Manager, Techniker, Eigentümervertreter und lesende Berichtsnutzer.

## Fachliche Daten / Implementierung

`PdfReportService` verwendet mandantensichere Fachservices, Anlagen-/Standortdaten und Apache PDFBox.

## API-Vertrag

`GET /api/inspections/{id}/report?lang=en|de`; `GET /api/work-orders/{id}/report?lang=en|de`.

## Beispielablauf

Nutzer öffnet Prüfung oder Arbeitsauftrag → fordert Bericht an → Server kontrolliert den Mandanten → erstellt A4-PDF aus gespeicherten Feldern → Browser lädt `application/pdf` als Anhang herunter.

## Geschäftsregeln, Rechte und Grenzfälle

Sprache ist standardmäßig EN; `lang=de` schaltet auf deutsche Berichtsbeschriftung. Berichte sind **operative Zusammenfassungen**, keine rechtsverbindlichen unterschriebenen Prüfnachweise. Die PDFBox-Standard14-Schriften besitzen begrenzte Unicode-Abdeckung; bestimmte Zeichen werden ersetzt. Für eine verlässliche rechtliche Archivierung beliebiger Texte reicht dies nicht aus.

## Automatisierte Tests

`downloadableReport` prüft HTTP-Status, PDF-MIME-Typ und `%PDF-`-Signatur. Für lange Texte und deutsche Umlaute wird zusätzlich eine visuelle Prüfung empfohlen.

## Bewusste Grenzen und Ausbau

Vor echtem Einsatz müssen lizenzkonforme eingebettete Unicode-Schriften, Seitenzahlen, Unterschriften, Integritätsschutz, Anhänge und Berichtsrevisionen ergänzt werden. Die öffentliche Vorschau verwendet Browser-Druck statt Server-PDF.

**Querverweise:** [API](../API.md) · [Architektur](../ARCHITECTURE.md) · [Testkonzept](../TESTING.md) · [Roadmap](../ROADMAP.md).
