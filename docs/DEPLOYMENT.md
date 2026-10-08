# Deployment

Where MyOpty runs, what it talks to, and how the local preview becomes a live site.
Everything here is read from the environment contract in
[`.env.example`](../.env.example); the sections below say which host and domain to
use, what to register, where the credentials go, and how the configuration fails
when they are missing.

**Verification note, per [docs/README.md](README.md):** the steps that were actually
run are marked *(verified)*. The PayHere registration walkthrough was written from
official PayHere documentation on 2026-10-06 and has **not** been executed by the
author — the account signup requires a human with an email address. The hosting,
domain and rollout sections are a **plan**: no VPS has been provisioned and no
domain registered yet, so every command there is *(not run)*. Treat PayHere's own
docs as the source of truth if anything has moved.

---

## Hosting provider — a small VPS running Docker Compose

**The recommendation: one small VPS (Ubuntu 24.04 LTS) with Docker Engine and the
Compose plugin.** Hetzner CX22 or an equivalent DigitalOcean/Vultr droplet —
roughly €4–7/month for 2 vCPU / 4 GB, which comfortably fits the JVM, Next.js,
MySQL and nginx. *(not run — pick the provider when the project goes live.)*

Why a VPS and not a PaaS (Render/Railway/Fly.io):

- **The deployment unit already exists.** `backend/compose.prod.yaml` builds the API
  and web images and puts them behind one nginx proxy; a VPS runs that file
  unchanged, while a PaaS would mean re-expressing a stateful stack (MySQL volume,
  loopback port bindings, `env_file`) as proprietary services.
- **One command deploys**, the same shape as the local preview:
  `docker compose -f compose.yaml -f compose.prod.yaml up -d --build`.
- **Cost and control.** A student project pays for a VM, not per-service markup,
  and a raw VM is where a `notify_url`, custom port or firewall rule needs to land.

### Server shape *(not run)*

1. Provision the VM, log in over SSH with a key (password auth disabled), create a
   non-root user with sudo.
2. Install Docker Engine + the `compose` plugin (docker.com's apt repository).
3. `ufw allow 22/tcp && ufw allow 80/tcp && ufw allow 443/tcp && ufw enable` —
   nothing else is reachable: MySQL (3307), Mailpit and the preview port all bind
   `127.0.0.1` already, which is why the firewall only opens the web ports.
4. Clone this repository to `/opt/myopty` and create the server's `.env`
   (see [Secrets on the server](#secrets-on-the-server) below).
5. Start the stack with the command above and check `http://<server-ip>` — it will
   not be the domain yet, and it will not be TLS yet.

### What the stack does not do by itself

`compose.prod.yaml` binds the preview proxy to **`127.0.0.1:8088`, loopback only**,
deliberately: the file's own comment says nothing here listens on a routable address
until staging has real accounts and TLS behind it. So the public entrypoint is an
edge proxy on the host — that is the next section, not a change to the compose file.

---

## Domain and TLS

### Register a domain *(not run)*

The team has no domain yet. Buy one from a registrar with simple DNS —
**Porkbun** or **Cloudflare Registrar**, ~€10–12/year for a `.com` or `.lk`
through an authorised reseller. Registration takes the project's name or the
shop's; the choice only needs to be made once, because the payment gateway
approves secrets **per domain** (see the PayHere section) and changing domains
means re-requesting them.

### DNS *(not run)*

| Record | Type | Value | Points at |
|---|---|---|---|
| `@` | A | the VPS's public IPv4 | the site |
| `www` | CNAME | the apex domain | the same site |

Propagation is minutes to a few hours. `dig +short yourdomain.example` confirms it.

### TLS — Caddy in front of the loopback preview *(not run)*

The recommendation is **Caddy on the VPS host** as the public 80/443 terminator,
because it obtains and renews Let's Encrypt certificates automatically and leaves
the already-working compose stack untouched:

```
# /etc/caddy/Caddyfile — Caddy listens on 80/443, the preview stays on loopback
yourdomain.example {
    reverse_proxy 127.0.0.1:8088
}
```

`apt install caddy`, paste that, `systemctl reload caddy` — HTTPS is live because
Caddy reads the A record and completes the ACME challenge itself. The alternative
is `certbot --nginx` inside the proxy container, but that means editing
`backend/nginx/preview.conf` and `compose.prod.yaml` (443 listener, certificate
mounts) — more moving parts in files that currently work.

HTTPS is not optional for staging either: PayHere's server-to-server payment
notification must reach a **public HTTPS `notify_url`** (see the rollout
checklist), and a session cookie over plain HTTP is a credential in transit.

---

## Secrets on the server

- **Generate fresh values on the server.** The VPS `.env` is not any developer's
  `.env`: run `openssl rand -hex 16` / `-hex 24` there for `DB_PASSWORD`,
  `DB_ROOT_PASSWORD` and `MINIO_SECRET_KEY` (the same commands
  `./scripts/check-env.sh --secrets` prints). Copying a laptop's `.env` up ships
  that laptop's history with it.
- **Transport:** edit it in place over SSH (`ssh root@host 'vi /opt/myopty/.env'`)
  or `scp` it up — then `chmod 600 /opt/myopty/.env`, owner = the deploy user.
  Never commit it, never paste it into a chat or ticket.
- **Live payment credentials live only on the server.** Developers keep sandbox
  values; a live merchant secret on a developer machine is one careless `git add`
  away from a rotated integration (`.env.example` says the same at the top).
- **If a credential is ever committed: rotate it first, tell the team second.**
  Deleting the commit does not unpublish history — `gitleaks` fails the CI run
  precisely so this is caught while it is still cheap.
- **CI/CD today is not a deploy.** Going live is a manual `git pull` + Compose
  restart on the box. If an automated deploy is added later, its credentials go in
  GitHub Actions secrets or a deploy key — still not in the repository.

---

## Staging → live rollout

### Stage 1 — staging on the VPS, everything sandboxed *(not run)*

1. Server up, domain resolving, Caddy serving `https://<domain>` — the compose
   stack is unchanged from the local preview: `docker compose -f compose.yaml -f
   compose.prod.yaml up -d --build`.
2. Payment: register the **staging domain** as a Domain/App in the PayHere
   sandbox portal and put the sandbox `PAYMENT_GATEWAY_MERCHANT_ID` /
   `PAYMENT_GATEWAY_SECRET_KEY` in the server's `.env` with
   `PAYMENT_GATEWAY_SANDBOX=true` (registration runbook in the PayHere section
   below). The secret is per domain — staging and live are separate approvals.
3. Email: point `MAIL_*` at the real SMTP provider per the SMTP section below,
   with `MAIL_FAIL_ON_ERROR=true` and `LOG_LEVEL=warn`. Mailpit still starts with
   the stack but nothing points at it once `MAIL_HOST` changes.
4. Verify *(not run)*: `/actuator/health` is `UP`; log in with a seeded account;
   complete a sandbox checkout; send one real email and find it in the provider's
   activity log (the Mailpit UI at :8025 only exists locally).
5. Backups before anything else *(not run)* — a nightly systemd timer or cron:

   ```bash
   docker compose -f compose.yaml -f compose.prod.yaml exec -T mysql \
     sh -c 'exec mysqldump -uroot -p"$MYSQL_ROOT_PASSWORD" myopty' \
     | gzip > "/var/backups/myopty-$(date +%F).sql.gz"
   ```

   Keep the dump **and** a copy of the server `.env` in a password manager:
   a restore needs `DB_PASSWORD` to match the dump, and a lost `.env` means
   lost payment configuration.

### Stage 2 — live cutover *(end of project, not run)*

A sandbox account never converts to a live one — this is a new registration, in
order:

1. Register the **live** PayHere merchant account, add the production domain,
   get its live Merchant ID/Secret → replace only those two values in the
   server's `.env`.
2. Set `PAYMENT_GATEWAY_SANDBOX=false`. The application logs a loud warning on
   every startup while this is false-adjacent, by design — the flip is deliberate,
   followed by an immediate restart and one real low-amount payment **and its
   refund** through the flow (the refund path exists in the fake gateway and must
   exist in the real one before customers use it).
3. Confirm `PAYMENT_GATEWAY_PROVIDER=payhere`, `MAIL_*` = production provider,
   `LOG_LEVEL=error` (per the `.env.example` comment on that variable).
4. Smoke-test the live site end to end *(not run)*: browse → register → log in →
   order → pay → receive the confirmation email; then check `/actuator/health`
   from outside the LAN.
5. **Rollback** = restore the staged `.env` from backup, restart. The old file
   still carries sandbox credentials, so the site returns to staging behaviour
   without re-registering anything.

Two standing decisions for whoever runs the cutover: whether `/swagger-ui.html`
should stay publicly reachable in production (it is exposed by the proxy
deliberately for staging), and whether the demo accounts seeded by migration
V1_3 should exist in the production database at all.

---

## Payment gateway — PayHere

The default is `PAYMENT_GATEWAY_PROVIDER=fake`: an in-process stand-in that needs no
account, and the mode every developer is in until they register a sandbox. Registering
the sandbox is what this section is for.

### Register the sandbox account *(not run — human task)*

1. Sign up at **<https://sandbox.payhere.lk/merchant/sign-up>** — the sandbox is a
   separate deployment of the PayHere platform, so a sandbox account never converts to
   a live one (and vice versa). No real payments happen there; it is simulated.
2. Verify the email address the account was registered with.
3. In the merchant portal: **Side Menu → Integrations** → copy the **Merchant ID**.
4. Still under *Integrations*: **Add Domain/App** → enter the top-level domain (use
   `localhost` while developing) → **Request to Allow**. Wait for the approval, then copy
   the **Merchant Secret** shown next to the domain.
   - The secret is **per domain/app**, not per account: integrating on a new domain means
     requesting a new secret.
5. Put the values in your `.env` (never in `.env.example`, never in git):

   ```
   PAYMENT_GATEWAY_PROVIDER=payhere
   PAYMENT_GATEWAY_MERCHANT_ID=<from step 3>
   PAYMENT_GATEWAY_SECRET_KEY=<from step 4>
   PAYMENT_GATEWAY_SANDBOX=true
   ```

   `PAYMENT_GATEWAY_API_VERSION` is not used by PayHere — leave it blank.

6. Confirm the configuration **(verified — both checks exist and run)**:

   ```bash
   ./scripts/check-env.sh        # fails, naming whichever credential is blank
   cd backend && ./mvnw spring-boot:run   # fails at startup, naming the same variable
   ```

   The application refuses to start on an unknown provider or missing credentials rather
   than discovering the problem at checkout (`PaymentGatewayProperties` in
   `backend/billing/`).

### Configuration reference

| Variable | Meaning |
|---|---|
| `PAYMENT_GATEWAY_PROVIDER` | `fake` (default, no account) or `payhere` |
| `PAYMENT_GATEWAY_MERCHANT_ID` | PayHere Merchant ID, *Integrations* menu |
| `PAYMENT_GATEWAY_SECRET_KEY` | Merchant Secret, per domain/app |
| `PAYMENT_GATEWAY_API_VERSION` | Unused by PayHere; blank |
| `PAYMENT_GATEWAY_SANDBOX` | `true` = sandbox endpoints (no money). `false` triggers a loud startup warning and must be a deliberate choice |

### Safety rules

- **Sandbox only on developer machines.** Live credentials never belong in a developer's
  `.env`; if one is ever committed, treat it as leaked and rotate it (`.env.example` says
  the same at the top).
- `PAYMENT_GATEWAY_SANDBOX=false` is warned about at every startup, so a half-finished
  edit cannot quietly point the app at live mode.
- When checkout lands (Epic 4): PayHere's `notify_url` must be a **publicly reachable
  HTTPS** URL — `localhost` cannot receive PayHere's server-to-server payment
  notification. That is what the [Domain and TLS](#domain-and-tls) section above
  exists for.

### For whoever implements the payment flow (Epic 4)

Sourced from the [PayHere Authorize API docs](https://support.payhere.lk/api-&-mobile-sdk/authorize-api)
on 2026-10-06 *(not run)*:

- Sandbox payment endpoint: `https://sandbox.payhere.lk/pay/authorize` (live:
  `https://www.payhere.lk/pay/authorize`).
- Request hash, computed **server-side only** (client-side computation would expose the
  secret):

  ```
  hash = to_upper_case(md5(merchant_id + order_id + amount + currency + to_upper_case(md5(merchant_secret))))
  ```

- Payment notifications must be verified before acting on them:

  ```
  md5sig = to_upper_case(md5(merchant_id + order_id + payhere_amount + payhere_currency + status_code + to_upper_case(md5(merchant_secret))))
  ```

- Test cards (sandbox): Visa `4916217501611292`, MasterCard `5307732125531191`,
  AMEX `346781005510225`; any name/CVV/expiry. Decline scenarios have their own cards —
  see <https://support.payhere.lk/sandbox-and-testing>.

---

## Outbound email — SMTP

### Locally: Mailpit *(verified)*

Nothing to register. `docker compose up -d` starts Mailpit with the database: SMTP on
`localhost:1025`, every message it receives at **<http://localhost:8025>**. The `.env`
defaults (`MAIL_HOST=localhost`, `MAIL_PORT=1025`, auth and STARTTLS off) point at it, and
nothing leaves the machine.

The path is proven by an automated test, not by hope: `MailSendingTest` boots the real
context against a throwaway Mailpit container, sends through the auto-configured
`JavaMailSender`, and asserts the message arrived via Mailpit's API
*(verified — part of `./mvnw verify`)*.

To watch it live: keep <http://localhost:8025> open and send anything once the Q&A or
dealer-email features land.

### Staging and production: a real SMTP provider *(not run — needs an account)*

1. Create an account with any SMTP provider (e.g. Brevo's free tier) and generate
   **SMTP credentials** — these are often different from the account login.
2. Fill in `.env`:

   ```
   MAIL_HOST=<provider's SMTP host>
   MAIL_PORT=587
   MAIL_USERNAME=<SMTP user>
   MAIL_PASSWORD=<SMTP password>
   MAIL_FROM_ADDRESS=no-reply@yourdomain.tld
   MAIL_SMTP_AUTH=true
   MAIL_STARTTLS=true
   MAIL_FAIL_ON_ERROR=true
   ```

   - `MAIL_SMTP_AUTH` / `MAIL_STARTTLS` default to `false` because Mailpit needs neither;
     JavaMail does not infer authentication from a non-blank username, so both are
     explicit.
   - Use a domain you control in `MAIL_FROM_ADDRESS` — providers reject or spam-folder
     senders without SPF/DKIM alignment.
   - `MAIL_FAIL_ON_ERROR=true` in staging: a customer who is never told their order is
     ready finds out at the counter (see the comment on that variable in
     `.env.example`).
3. `./scripts/check-env.sh` warns when `MAIL_HOST` is not local but `MAIL_USERNAME` is
   blank *(verified)*.
4. Verify by sending a real notification and checking the provider's activity log —
   the Mailpit UI at :8025 only exists locally.

---

## Related

- [`.env.example`](../.env.example) — the full contract, every variable with its default.
- [SETUP.md](SETUP.md) — local environment, secrets to generate, preflight check.
- [../README.md](../README.md) — the staging-preview command and the branch/CI workflow
  the deployment builds on.
