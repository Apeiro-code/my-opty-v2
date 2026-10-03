# Shared Infrastructure

Auth, users, the Q&A section and cross-cutting configuration. Owned by the whole team: a change
here needs team consensus, because every other module reads from it.

## Tables owned here

| Table | What it holds |
|---|---|
| `app_user` | Every login, customer and shop owner alike, separated by `role` |
| `client_profile` | Shop details: name, venue, contact number |
| `customer_profile` | Customer address and preferred contact |
| `question` | Customer questions, answers, and the public FAQ flag |

Migrations live in `shared/src/main/resources/db/migration`. The band is **V1**.

`shared` has four tables but one version in the band, so it uses Flyway's nested versions —
`V1`, then `V1_1`, `V1_2`. They sort as 1, 1.1, 1.2: inside the documented band, and one
logical change per file, which is what the migration rules ask for.

## Why one user table and not two

The EER diagram draws `CUSTOMER` and `CLIENT` as separate entities. They are one table with a
`role` column here, because authentication comes first: a login has to resolve to exactly one
identity before it can know which profile to load. Two tables means two places to look, two
places to forget a row, and a password reset that has to guess which table it is in.
`client_profile` and `customer_profile` carry what actually differs between the two kinds of
person, and each is keyed by `app_user.id`.

## What is here now

| Class | Why it exists |
|---|---|
| `config/CorsConfig` | Reads `CORS_ALLOWED_ORIGINS` so the Next.js dev server can call the API. Handing this to the security filter chain is part of adding Spring Security. |
| `config/JdbcAuditingConfig` | Fills `created_at` / `updated_at`. Those columns are `NOT NULL` in every migration, so an entity without the auditing annotations fails on insert instead of writing a zero timestamp. |

Auth itself — registration, login, JWT issuance, roles — is not implemented yet. It is the
next piece of Epic 0, and until it lands the API is open to anyone who can reach it.