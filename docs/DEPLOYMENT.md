# Deployment guide

How to run Warehouse App on a Linux server with a public domain, HTTPS,
backups and monitoring. Everything runs in Docker: PostgreSQL, the
application, Caddy (HTTPS reverse proxy with automatic Let's Encrypt
certificates) and the backup service.

```
Internet ──443/80──▶ Caddy (proxy) ──▶ app:8080 ──▶ db:5432
                                                  ▲
                         backup (nightly pg_dump) ┘──▶ ./backups ──▶ off-site storage (optional)
```

## 1. What you need

| Item | Notes |
|---|---|
| Server | Any Linux VPS with 2 GB RAM, 1–2 vCPU, 20 GB disk (Hetzner, DigitalOcean, OVH, a company server…). Ubuntu 24.04 LTS is assumed below. |
| Domain | A domain or subdomain, e.g. `sklad.example.com`. |
| SMTP service | For password recovery e-mails: Brevo, Mailgun, Amazon SES, Postmark or your own mail host. You need host, port, user, password and an allowed sender address. |
| Off-site storage (recommended) | Any S3-compatible bucket (Backblaze B2, AWS S3, Wasabi, Cloudflare R2…) for backup copies outside the server. |

## 2. Prepare the server

1. **DNS:** create an `A` record (and `AAAA` for IPv6) for your domain pointing
   to the server's IP address. Wait until `ping sklad.example.com` shows it.
2. **Install Docker** (official repository, includes Compose):

   ```bash
   curl -fsSL https://get.docker.com | sudo sh
   sudo usermod -aG docker $USER     # log out and back in afterwards
   ```

3. **Firewall:** allow only SSH and web traffic.

   ```bash
   sudo ufw allow OpenSSH
   sudo ufw allow 80/tcp
   sudo ufw allow 443
   sudo ufw enable
   ```

## 3. Install the application

```bash
git clone https://github.com/Jarru01/warehouse-management-app.git
cd warehouse-management-app
cp .env.prod.example .env
chmod 600 .env
nano .env
```

Fill in every value marked **REQUIRED** in `.env`:

- `DOMAIN` — your domain, without `https://`.
- `DB_PASSWORD` — a long random password, e.g. from `openssl rand -base64 32`.
- `WAREHOUSE_RIADITEL_HESLO` — the director's temporary password for the first login.
- `MAIL_HOST`, `MAIL_PORT`, `MAIL_USERNAME`, `MAIL_PASSWORD`, `MAIL_FROM` — from your SMTP provider.
- Backup settings, and the `RCLONE_CONFIG_OFFSITE_*` lines for off-site copies (see section 6).

Start everything:

```bash
docker compose -f compose.yaml -f compose.prod.yaml --profile app up -d --build
docker compose -f compose.yaml -f compose.prod.yaml --profile app ps
```

The first start builds the images (a few minutes) and Caddy requests the
certificate. When `app` is `healthy`, open `https://your-domain`.

Tip: to avoid typing both files every time, put this into the shell profile:

```bash
alias wh='docker compose -f compose.yaml -f compose.prod.yaml --profile app'
```

The rest of this guide uses `wh` for that command.

## 4. First login checklist

1. Log in as director (ID `123`) with `WAREHOUSE_RIADITEL_HESLO`; you are asked
   to set a new password immediately.
2. `WAREHOUSE_RIADITEL_HESLO` can stay in `.env`: it is applied only while the
   director has no password, so it cannot reset the new one.
3. Register a test customer with your e-mail and try **Zabudol si heslo?** to
   confirm e-mails arrive.
4. Run a backup now and check it (section 6).
5. Add the monitors from section 7.

## 5. Updating

```bash
git pull
wh up -d --build
```

Flyway applies new database migrations automatically on start. Take a manual
backup before larger updates (`wh run --rm backup zaloha`).

Useful commands:

| Task | Command |
|---|---|
| Status | `wh ps` |
| Application logs | `wh logs -f app` |
| Proxy logs | `wh logs -f proxy` |
| Restart the application | `wh restart app` |
| Stop everything (data kept) | `wh down` |

Container logs rotate automatically (5 × 10 MB per container).

## 6. Backups

The `backup` container makes a compressed `pg_dump` every night at
`BACKUP_TIME` (UTC) into `BACKUP_DIR` (default `./backups` in the project
folder):

| Folder | Content | Kept (default) |
|---|---|---|
| `backups/daily` | every backup | 7 newest (`BACKUP_KEEP_DAILY`) |
| `backups/weekly` | copy of Sunday's backup | 4 newest (`BACKUP_KEEP_WEEKLY`) |
| `backups/monthly` | copy of the backup from the 1st of the month | 6 newest (`BACKUP_KEEP_MONTHLY`) |

Every backup is checked with `pg_restore --list` before it is kept; a broken
dump is discarded and logged as an error.

| Task | Command |
|---|---|
| Back up now | `wh run --rm backup zaloha` |
| List backups | `wh run --rm backup zoznam` |
| Backup log | `wh logs backup` |

### Off-site copies

Backups on the same server are lost together with the server. Set
`BACKUP_OFFSITE` and the `RCLONE_CONFIG_OFFSITE_*` values in `.env` (an example
for Backblaze B2 is in `.env.prod.example`; any rclone remote works). After each
backup the whole `backups` folder is copied there (`rclone copy`, files are
never deleted remotely). Set a lifecycle rule on the bucket, for example delete
files older than 180 days, to limit storage costs. Use an access key that can
only write to that one bucket.

### Restore

1. Stop the application so nothing writes during the restore:

   ```bash
   wh stop app
   ```

2. Restore a backup (path relative to `backups/`, see `zoznam`):

   ```bash
   wh run --rm backup obnov daily/sklad-20261117-023000.dump
   ```

   The restore runs in one transaction: either the whole database is replaced
   by the backup, or nothing changes.

3. Start the application again:

   ```bash
   wh start app
   ```

To check a backup without touching the live data, restore it into a separate
database: `wh run --rm -e OBNOVA_DB=sklad_test backup obnov daily/…dump`, look
at it with `wh exec db psql -U warehouse -d sklad_test`, and remove it with
`wh exec db dropdb -U warehouse sklad_test`. Do this test restore regularly,
for example once a month.

## 7. Monitoring

The application needs no changes; connect an external monitoring service to
the endpoints below. Free options: **UptimeRobot**, **Better Stack**,
**healthchecks.io**, or a self-hosted **Uptime Kuma** on another machine.

| What | How to set it up | Alert when |
|---|---|---|
| Application and database | HTTP(S) monitor on `https://your-domain/actuator/health/readiness`, every 1–5 minutes, expect status `200` and keyword `UP` | down for 2 checks in a row |
| Application process | (optional) `https://your-domain/actuator/health/liveness` | down |
| HTTPS certificate | Most uptime services check certificate expiry on the HTTPS monitor; enable "notify X days before expiry" | expires in < 14 days (Caddy renews after 60 days, so this means renewal failed) |
| Backups | Create a heartbeat/push check (healthchecks.io "check" or Uptime Kuma "Push" monitor) with period 1 day and grace 2 hours; put its URL into `BACKUP_HEARTBEAT_URL` | no successful backup for over a day |
| Disk space | VPS provider's monitoring or a node agent | > 80 % used |

What the endpoints mean:

- `/actuator/health/readiness` — `UP` when the application can serve requests,
  including the database connection. Answers `503` with `OUT_OF_SERVICE` or
  `DOWN` otherwise.
- `/actuator/health/liveness` — the application process is running. Docker uses
  it to mark the container `unhealthy`.
- `/actuator/health` — overall state; details are hidden on purpose.

Security events (failed and blocked logins, password changes) are in the audit
log on the director's dashboard and in `wh logs app` (logger `AUDIT`).

## 8. Security notes

- Only ports 22, 80 and 443 are open; the database and the application port are
  reachable only inside Docker.
- Keep the server updated: `sudo apt update && sudo apt upgrade` regularly, or
  enable `unattended-upgrades`.
- Rebuild the images now and then (`wh build --pull && wh up -d`) to pick up
  security fixes of the base images.
- `.env` contains every secret; keep it out of Git and readable only by you.
