# PDF operational reports and bilingual exports

[English](../../en/modules/09-reporting.md) · [Deutsch](../../de/modules/09-reporting.md) · [Index](../INDEX.md)

## Business purpose

Generate printable inspection and work-order evidence from the current stored records, without uploading documents to a separate document service.

## Actors

Facility manager, technician, owner stakeholder and read-only reporting viewers.

## Data and implementation

`PdfReportService` reads tenant-scoped `InspectionService`, `WorkOrderService`, `CatalogService` and users; Apache PDFBox returns byte arrays.

## HTTP contract

`GET /api/inspections/{id}/report?lang=en|de`; `GET /api/work-orders/{id}/report?lang=en|de`.

## Typical workflow

User opens inspection or order details → selects report → server checks resource scope and assembles fields → PDFBox renders A4 document → browser downloads `application/pdf` as an attachment.

## Rules, permissions and edge cases

Language defaults to EN; only `lang=de` switches to German. Content-Disposition forces download. Reports are **operational summaries**, not signed legal certificates. PDFBox Standard14 fonts have limited Unicode coverage; certain unsupported characters are replaced, so this version is unsuitable for definitive legal archiving of arbitrary multilingual text.

## Automated tests

`downloadableReport` validates status, content type and `%PDF-` signature; manual visual QA recommended for long strings and German umlauts.

## Boundaries and future extensions

Replace Standard14 fonts with properly licensed embedded Unicode fonts before production. Add page numbers, signatures, tamper-evident archiving, attachments and report version histories. Public preview uses browser print, not server PDF.

**Related:** [API](../API.md) · [Architecture](../ARCHITECTURE.md) · [Testing](../TESTING.md) · [Roadmap](../ROADMAP.md).
