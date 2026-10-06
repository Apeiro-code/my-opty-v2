# External Service Accounts

What the application talks to outside this repository. Everything here is read from the
environment contract in [`.env.example`](../.env.example); the runbook below says what to
register, where the credentials go, and how the configuration fails when they are missing.

> **Scope:** Epic 0 story 15 (*document the deployment plan*) will add hosting provider,
> domain, and the staging → live rollout. This file covers only the two service accounts
> that exist today: the payment gateway and outbound email.

**Verification note, per [docs/README.md](README.md):** the steps below that were actually
run are marked *(verified)*. The PayHere registration walkthrough was written from official
PayHere documentation on 2026-10-06 and has **not** been executed by the author — the
account signup requires a human with an email address. Treat PayHere's own docs as the
source of truth if anything has moved.

---

## Payment gateway — PayHere

The default is `PAYMENT_GATEWAY_PROVIDER=fake`: an in-process stand-in that needs no
account, and the mode every developer is in until they register a sandbox. Registering the
sandbox is what this section is for.

### Register the sandbox account *(not run — human task)*

1. Sign up at **<https://sandbox.payhere.lk/merchant/sign-up>** — the sandbox is a
   separate deployment of the PayHere platform, so a sandbox account never converts to a
   live one (and vice versa). No real payments happen there; it is simulated.
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
- When checkout lands (Epic 4): PayHere's `notify_url` must be a **publicly reachable**
  URL — `localhost` cannot receive PayHere's server-to-server payment notification. That
  is a staging-hosting concern and arrives with story 15.

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
