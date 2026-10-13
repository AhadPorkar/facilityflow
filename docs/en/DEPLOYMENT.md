# Deployment Playbook

[Deutsch](../de/DEPLOYMENT.md) · [Index](INDEX.md)

## Choosing a deployment

FacilityFlow has two fundamentally different modes:

1. **Public portfolio demo** — GitHub Pages hosts static HTML/CSS/JS only. With `?preview=1`, the browser creates a synthetic organization and local records. No backend, tenant security claims, Java runtime, or real PostgreSQL access are involved. **Best for recruiters.**
2. **Full application** — Spring Boot + PostgreSQL, deployed through Docker Compose + Caddy on a VPS, or via a container platform with managed PostgreSQL. Requires real credentials, TLS and responsible operations. **Best for technical evaluation.**

A `github.io` hostname enables preview mode automatically, even without `?preview=1`. The Pages URL in the README assumes you publish under the repository name `facilityflow`; update the link if the repo name differs.

## Option A — GitHub Pages, free static showcase

1. Create the GitHub repository `ahadPorkar/facilityflow` and push the entire extracted project.
2. In **Settings → Pages → Build and deployment**, select **GitHub Actions** as source.
3. Push to `main`, or manually run **Publish interactive showcase** in the Actions tab.
4. After a successful run, open `https://ahadporkar.github.io/facilityflow/` (or `?preview=1`).
5. Inspect the EN/DE switch, mobile navigation, create/assign/complete/approve, inspection and defect features.

`.github/workflows/pages.yml` uploads `src/main/resources/static/`. Relative resource paths are used deliberately so the demo also works from a project subpath. Its user data is synthetic and stored in browser local storage. The **Reset** control restores demonstration records. Never put API keys, live passwords or customer data in the static assets.

**GitHub Pages limitations:** cannot execute Java, PostgreSQL, scheduled tasks, private sessions, or real multi-user collaboration; persistence is local to one browser profile. Changes are demonstrative only.

## Option B — Docker Compose on your laptop or VPS

### Local development

```bash
cp .env.example .env
# Set DB_PASSWORD to a long random value.
docker compose up --build -d
docker compose ps
docker compose logs -f app
curl http://localhost:8080/actuator/health
```

The database is available **only inside the Docker network**. The application is bound to `127.0.0.1:8080` on the host by default. Demo seed is enabled in `.env.example` only to simplify local evaluation.

### Internet-facing VPS with Caddy and HTTPS

Prerequisites: Linux host with Docker Compose v2; domain A/AAAA records pointing at server; inbound TCP ports 80/443 permitted; sufficient memory for Java and PostgreSQL; a backup location outside the host.

1. Install updates and Docker. Create a non-root deploy account and restrict SSH access.
2. Clone the repository and create a private `.env` file with the following pattern:

```env
DB_PASSWORD=PUT_A_STRONG_RANDOM_PASSWORD_HERE
APP_DEMO_SEED=false
APP_BOOTSTRAP_EMAIL=admin@your-company.example
APP_BOOTSTRAP_PASSWORD=PUT_A_SEPARATE_STRONG_RANDOM_PASSWORD_HERE
APP_BOOTSTRAP_NAME=FacilityFlow Administrator
SESSION_COOKIE_SECURE=true
APP_DOMAIN=facilityflow.yourdomain.tld
APP_PORT=8080
```

3. Point `APP_DOMAIN` at the server and verify DNS and firewall before startup.
4. Start with:

```bash
docker compose --profile production up --build -d
docker compose ps
curl -I https://facilityflow.yourdomain.tld/
curl https://facilityflow.yourdomain.tld/actuator/health
```

Caddy uses `deploy/Caddyfile` and automatically manages HTTPS certificates for an eligible public domain. It is the *only* service published to the internet on 80/443. PostgreSQL remains on the internal Compose network. Do not directly expose port 5432.

**Important:** `SESSION_COOKIE_SECURE=true` is mandatory on HTTPS. The example's HTTP access port is bound to localhost only; for a publicly accessible environment, restrict it via host firewall or remove the app's `ports:` mapping and use `expose: ["8080"]` in the Compose file, allowing access only through Caddy.

**Bootstrapping:** on an empty database, a private first admin is created from `APP_BOOTSTRAP_EMAIL` and `APP_BOOTSTRAP_PASSWORD`. Keep secrets in a secure vault, not in Git or CI logs. Rotate bootstrap credentials after initial provisioning. If no demo or bootstrap configuration is provided, users will need a safe out-of-band creation procedure; do not expose a public registration route.

### Backups and restore rehearsal

For a local illustration:

```bash
# Replace BACKUP_FILE with a private, access-controlled destination.
docker compose exec -T db pg_dump -U facilityflow -Fc facilityflow > facilityflow.dump
# Restore must be rehearsed on an isolated database; do NOT overwrite live data casually.
```

Automate encrypted off-host backups, monitor success, enforce retention, and periodically restore into a separate environment. This repository provides **no automated backup/restore system**.

### Updating and rollback

Test schema migrations and CI before each deployment. Take and verify a backup, build a pinned release tag, then redeploy:

```bash
git fetch --tags
git checkout v1.0.0
docker compose --profile production up --build -d
```

**Warning:** reverting application code is not equivalent to reverting a Flyway schema migration. Database migrations need an explicit forward repair or restore plan. This repository includes only the initial `V1__init.sql` migration.

## Option C — Railway/container PaaS

A platform such as Railway can run this `Dockerfile` with a separate PostgreSQL instance. The exact UI varies, but the general sequence is:

1. Create a project and connect GitHub repo (Dockerfile build).
2. Add managed PostgreSQL and provision dedicated credentials.
3. Configure `SPRING_DATASOURCE_URL=jdbc:postgresql://<internal-host>:5432/<db>`, username/password; do not assume an arbitrary provider URL is directly JDBC-compatible.
4. Configure `APP_DEMO_SEED=false`, secure bootstrap credentials and `SESSION_COOKIE_SECURE=true`.
5. Expose the application's `PORT` (Spring maps `${PORT:8080}`), enable platform HTTPS, check `/actuator/health`.
6. Set backup, monitoring, budget alerts and domain. Verify platform/proxy forwarding and cookie/session behavior.

**Caveat:** platform container filesystems can be ephemeral; PostgreSQL must use a managed/persistent service. Free tiers, limits and pricing may change; check provider documentation before selecting a plan. Railway instructions: https://docs.railway.com/guides/dockerfiles and https://docs.railway.com/guides/postgresql.

## Environment variable reference

| Variable | Default | Use |
|---|---|---|
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://localhost:5432/facilityflow` | PostgreSQL JDBC connection |
| `SPRING_DATASOURCE_USERNAME` | `facilityflow` | Database account |
| `SPRING_DATASOURCE_PASSWORD` | empty | Database secret |
| `PORT` | `8080` | HTTP listening port inside container |
| `APP_DEMO_SEED` | `false` | Seeds known demo data/user when empty — local only |
| `APP_BOOTSTRAP_EMAIL` | empty | Creates initial tenant admin if configured |
| `APP_BOOTSTRAP_PASSWORD` | empty | Admin credential; choose strong unique password |
| `APP_BOOTSTRAP_NAME` | `FacilityFlow Admin` | Admin display name |
| `APP_MAINTENANCE_CRON` | `0 0 3 * * *` | Maintenance scheduler cron, UTC |
| `SESSION_COOKIE_SECURE` | `false` | **Set true behind HTTPS** |
| `APP_DOMAIN` | `facilityflow.example.com` | Caddy automatic HTTPS hostname |
| `APP_PORT` | `8080` | Localhost binding in Compose |

## CI/CD

- `.github/workflows/ci.yml`: Java 21 Maven `verify` (real PostgreSQL via Testcontainers) and Node.js 22 no-dependency UI tests.
- `.github/workflows/pages.yml`: static Pages deployment on `main`; has permission for `pages: write` and `id-token: write` only in that job.
- This repository does **not** automatically deploy the full PostgreSQL-backed application from GitHub; that requires provider credentials and an explicit protected deployment workflow.

## Go-live safety checks

- No demo seed or known demo user credentials.
- Separate non-public database with dedicated least-privilege account, encrypted backups and restore drills.
- HTTPS/TLS, secure cookies, correctly configured DNS/reverse proxy, HTTP security headers.
- Secrets stored outside Git and restricted to runtime.
- Security review including authorization and CSRF end-to-end in deployed environment.
- Observability, uptime alarms and failure notification for scheduled maintenance jobs.
- Explicit GDPR/data retention assessment and qualified-personnel procedure where applicable.
- Test whether the provider restarts, upgrades and restores application data reliably.

## Authoritative references

- GitHub Pages: https://docs.github.com/en/pages
- GitHub Actions pages deployment: https://github.com/actions/deploy-pages
- Docker Compose: https://docs.docker.com/compose/
- Caddy automatic HTTPS: https://caddyserver.com/docs/automatic-https
- Railway Dockerfile deployment: https://docs.railway.com/guides/dockerfiles
- Railway PostgreSQL: https://docs.railway.com/guides/postgresql
- Spring Boot production deployment: https://docs.spring.io/spring-boot/reference/deployment/index.html
