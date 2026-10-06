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
`V1`, then `V1_1`, `V1_2`, `V1_3`. They sort as 1, 1.1, 1.2, 1.3: inside the documented band, and
one logical change per file, which is what the migration rules ask for. `V1_3` seeds the two
demo accounts (see the root `docs/SETUP.md`).

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
| `config/JdbcAuditingConfig` | Fills `created_at` / `updated_at`. Those columns are `NOT NULL` in every migration, so an entity without the auditing annotations fails on insert instead of writing a zero timestamp. |
| `user/AppUser` | The `app_user` row as Spring Security's `UserDetails`: the `role` column becomes `ROLE_CUSTOMER` or `ROLE_CLIENT`, and `isEnabled()` is `status = ACTIVE`, so a disabled account cannot log in even with the right password. |
| `user/AppUserRepository` | The one query login needs: find by email. Read-only, because authentication must not write users — that is registration's job, and registration does not exist yet. |
| `auth/AppUserDetailsService` | Hands Spring Security the `AppUser` for the email a login request carries. |
| `auth/SecurityConfig` | The whole filter chain: deny-by-default on `/api/**`, `ROLE_CLIENT` on `/api/shop/**`, the two auth endpoints and `/actuator/health` public. CSRF is off with the reason documented in the class; CORS reads `CORS_ALLOWED_ORIGINS`; 401 and 403 are JSON bodies in the API's error envelope. |
| `auth/AuthController` | `POST /api/auth/login`, `GET /api/auth/me`, `POST /api/auth/logout`. Login discards any pre-authentication session before establishing identity (fixation), and logout invalidates the session outright. |
| `auth/dto/*` | The request and response shapes, as records with the validation that keeps malformed input out of the controller. |

Registration, password reset and a login screen in the frontend are later stories — until
they land, the only accounts are the two `V1_3` fixtures, and "logged in" means the session
cookie they receive.