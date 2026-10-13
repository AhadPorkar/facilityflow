# Security Policy / Sicherheitsrichtlinie

## Supported versions / Unterstützte Versionen

FacilityFlow is a portfolio/reference application. Only the most recent main branch is maintained on a best-effort basis. There is no commercial SLA or guarantee of security support.

FacilityFlow ist eine Portfolio-/Referenzanwendung. Die aktuelle Hauptversion wird nach bestem Ermessen gepflegt; es besteht keine verbindliche Sicherheits-SLA.

## Report privately / Vertraulich melden

Do **not** disclose exploit details, credentials, database dumps or customer records in public GitHub issues. Contact the maintainer through the contact information on [ahadporkar.com](https://ahadporkar.com/). Include affected version, reproduction steps and expected impact without attaching real personal data. Allow time for private triage before public disclosure.

Bitte **keine** Exploit-Details, Kennwörter, Datenbankexporte oder Kundendaten in öffentlichen GitHub-Issues veröffentlichen. Kontakt über [ahadporkar.com](https://ahadporkar.com/). Betroffene Version, reproduzierbare Schritte und Auswirkung beschreiben – ohne personenbezogene Daten.

## Deployment notice / Betriebshinweis

Public preview mode is synthetic and unauthenticated; real mode must run behind HTTPS with `APP_DEMO_SEED=false`, secure unique credentials, private PostgreSQL and a reviewed backup/incident process. Read [Security (EN)](docs/en/SECURITY.md) / [Sicherheit (DE)](docs/de/SECURITY.md).
