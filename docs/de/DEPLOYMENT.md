# Bereitstellungs- und Betriebsanleitung

[English](../en/DEPLOYMENT.md) · [Übersicht](INDEX.md)

## Zwei bewusst getrennte Betriebsarten

1. **Öffentliche Portfolio-Demo:** GitHub Pages liefert nur HTML/CSS/JavaScript. Bei `?preview=1` werden synthetische Datensätze im Browser angelegt. Weder Java noch PostgreSQL noch echte Mandantenanmeldung sind beteiligt. **Ideal für Recruiter.**
2. **Vollständige Anwendung:** Spring Boot mit PostgreSQL, betrieben über Docker Compose und Caddy auf einem VPS oder über eine Containerplattform mit dauerhaftem Datenbankdienst. Hier gelten reale Authentifizierung, Rechte und Persistenz.

Unter `*.github.io` schaltet sich der Vorschauadapter automatisch ein. Die Repository-Links setzen den Namen `facilityflow` voraus; bei anderem Namen die README-URLs anpassen.

## Variante A: GitHub Pages als Recruiter-Vorschau

1. GitHub-Repository `ahadPorkar/facilityflow` anlegen und den gesamten Quellcode auf `main` hochladen.
2. Unter **Settings → Pages → Build and deployment** als Quelle **GitHub Actions** auswählen.
3. Workflow **Publish interactive showcase** automatisch durch Push oder manuell ausführen.
4. Nach erfolgreichem Lauf `https://ahadporkar.github.io/facilityflow/` öffnen.
5. Sprache EN/DE wechseln, Gebäude und Anlagen prüfen, Aufträge bearbeiten, Prüfungen mit Mängeln simulieren.

`pages.yml` veröffentlicht den Ordner `src/main/resources/static`. Alle statischen Ressourcen verwenden relative Pfade, damit die Vorschau unter einem GitHub-Projektunterordner funktioniert. Beispieldaten liegen im lokalen Browserspeicher und können über **Reset** zurückgesetzt werden. Keine API-Schlüssel oder echten Kundendaten in die statischen Dateien aufnehmen.

**Grenze:** GitHub Pages führt keine Java-Anwendung, keine Datenbank, keine Hintergrundjobs und keine echten gemeinsamen Benutzersessions aus.

## Variante B: Docker Compose auf Rechner oder VPS

### Lokale Entwicklung

```bash
cp .env.example .env
# Starkes DB_PASSWORD eintragen.
docker compose up --build -d
docker compose ps
docker compose logs -f app
curl http://localhost:8080/actuator/health
```

PostgreSQL wird ausschließlich im privaten Compose-Netz bereitgestellt; die Anwendung lauscht auf dem Host standardmäßig nur auf `127.0.0.1:8080`. `.env.example` aktiviert Beispieldaten **nur für die lokale Entwicklungsumgebung**.

### Öffentlicher VPS mit Caddy/HTTPS

Voraussetzungen: Linux-VPS mit Docker Compose v2, korrekt auf den Server gerichteter A/AAAA-DNS-Eintrag, offene Ports TCP 80/443, ausreichend Arbeitsspeicher und ein externes Backup-Ziel.

1. Betriebssystem aktualisieren; Deployment mit nichtprivilegiertem Nutzer und abgesichertem SSH organisieren.
2. Repository klonen und `.env` mit geheimen Werten erzeugen:

```env
DB_PASSWORD=LANGES_EINMALIGES_ZUFALLSPASSWORT
APP_DEMO_SEED=false
APP_BOOTSTRAP_EMAIL=admin@unternehmen.example
APP_BOOTSTRAP_PASSWORD=ANDERES_LANGES_STARKES_PASSWORT
APP_BOOTSTRAP_NAME=FacilityFlow Administrator
SESSION_COOKIE_SECURE=true
APP_DOMAIN=facilityflow.ihredomain.de
APP_PORT=8080
```

3. DNS- und Firewall-Konfiguration überprüfen.
4. Starten:

```bash
docker compose --profile production up --build -d
docker compose ps
curl -I https://facilityflow.ihredomain.de/
curl https://facilityflow.ihredomain.de/actuator/health
```

Caddy verwendet `deploy/Caddyfile` und übernimmt für eine öffentlich erreichbare Domain die automatisierte TLS-Zertifikatsverwaltung. Von außen sind nur Caddy-Ports 80/443 notwendig. PostgreSQL darf **nicht** über Port 5432 veröffentlicht werden. Der Host-Port 8080 ist in der mitgelieferten Compose-Datei ausschließlich an localhost gebunden; bei öffentlichem Betrieb kann die Portfreigabe auch entfernt und durch `expose: ["8080"]` ersetzt werden.

**Wichtig:** Hinter HTTPS unbedingt `SESSION_COOKIE_SECURE=true`. Mit `APP_BOOTSTRAP_EMAIL` und `APP_BOOTSTRAP_PASSWORD` kann beim Start einer leeren Datenbank ein Administrator erzeugt werden. Geheimnisse in einen Secret-Manager oder geschützte Umgebungsvariablen legen, nicht ins Repository oder CI-Protokoll. Beispielzugang niemals öffentlich verwenden.

### Datenbanksicherung

Ein manuelles Beispiel für lokal geschützte Exporte:

```bash
docker compose exec -T db pg_dump -U facilityflow -Fc facilityflow > facilityflow.dump
```

Backups müssen verschlüsselt, außerhalb des Servers aufbewahrt und regelmäßig durch eine **isolierte Wiederherstellung** geprüft werden. FacilityFlow enthält keine fertige automatische Datensicherung und kein Restore-Runbook für Produktivdaten.

### Updates und Rollback

Vor jedem Release Tests und Flyway-Migrationen überprüfen, Sicherung erzeugen und ein festes Git-Tag verwenden:

```bash
git fetch --tags
git checkout v1.0.0
docker compose --profile production up --build -d
```

Eine alte JAR-Version bedeutet nicht automatisch ein rückgängig gemachtes Datenbankschema. Datenbank-Migrationen benötigen einen eigenen Vorwärtskorrektur- oder Wiederherstellungsplan. Momentan existiert nur `V1__init.sql`.

## Variante C: Railway / Container-PaaS

Auf einer Plattform wie Railway wird das Dockerfile mit einem separaten PostgreSQL-Dienst verwendet:

1. Projekt anlegen und GitHub-Repository verbinden (Dockerfile-Build).
2. Persistenten PostgreSQL-Dienst erzeugen.
3. JDBC-Variablen setzen: `SPRING_DATASOURCE_URL=jdbc:postgresql://<interner-host>:5432/<datenbank>`, Benutzer und Kennwort.
4. `APP_DEMO_SEED=false`, Administrator-Zugang und `SESSION_COOKIE_SECURE=true` setzen.
5. `PORT` verwenden, Plattform-HTTPS aktivieren, `/actuator/health` überprüfen.
6. Kostenlimits, Backups, Monitoring, Domain und Proxy-/Cookie-Verhalten testen.

Ein Anbieter-URL-Format muss gegebenenfalls zuerst in eine JDBC-URL umgewandelt werden. Container-Dateisysteme können flüchtig sein; Daten gehören in den dauerhaften Datenbankdienst. Tarife und Limits vor der Entscheidung aktuell prüfen. Dokumentation: https://docs.railway.com/guides/dockerfiles und https://docs.railway.com/guides/postgresql.

## Relevante Umgebungsvariablen

| Name | Standard | Bedeutung |
|---|---|---|
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://localhost:5432/facilityflow` | PostgreSQL-JDBC-URL |
| `SPRING_DATASOURCE_USERNAME` | `facilityflow` | Datenbankbenutzer |
| `SPRING_DATASOURCE_PASSWORD` | leer | Datenbankkennwort |
| `PORT` | `8080` | HTTP-Port im Container |
| `APP_DEMO_SEED` | `false` | Lokale Beispieldaten/Standardzugang erzeugen |
| `APP_BOOTSTRAP_EMAIL` | leer | Erstes Administratorkonto |
| `APP_BOOTSTRAP_PASSWORD` | leer | Geheimnis für Erstadministrator |
| `APP_BOOTSTRAP_NAME` | `FacilityFlow Admin` | Anzeigename |
| `APP_MAINTENANCE_CRON` | `0 0 3 * * *` | Täglicher Wartungsjob, UTC |
| `SESSION_COOKIE_SECURE` | `false` | Bei HTTPS **true** |
| `APP_DOMAIN` | `facilityflow.example.com` | Domain für Caddy |
| `APP_PORT` | `8080` | Lokal gebundener Compose-Port |

## CI/CD und öffentliche Sicherheit

- `.github/workflows/ci.yml`: Maven/Java 21 mit PostgreSQL-Testcontainers, plus Node.js-Tests.
- `.github/workflows/pages.yml`: GitHub Pages für synthetische statische Demo; nur dieser Workflow benötigt Pages-Schreibrechte.
- Die reale Anwendung wird **nicht automatisch** durch GitHub Actions in ein Produktivsystem eingespielt. Dafür wären geschützte Provider-Secrets und ein separater Freigabeprozess nötig.

**Go-live-Checkliste:** keine Demo-Zugangsdaten, sichere Session-Cookies, TLS und DNS, nichtöffentliche Datenbank, Restore-Übungen, gesicherte Secrets, Sicherheitsreview, Monitoring inkl. Fehleralarm des Wartungsjobs, DSGVO-Bewertung und Rollenverantwortung für qualifizierte Prüfungen.

## Quellen zur Betriebsumgebung

- GitHub Pages: https://docs.github.com/en/pages
- Docker Compose: https://docs.docker.com/compose/
- Caddy HTTPS: https://caddyserver.com/docs/automatic-https
- Railway: https://docs.railway.com/guides/dockerfiles
- PostgreSQL Railway: https://docs.railway.com/guides/postgresql
- Spring Boot Deployment: https://docs.spring.io/spring-boot/reference/deployment/index.html
