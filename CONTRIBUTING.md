# Contributing to MyOpty

This is the detailed companion to the development workflow summary in [README.md](README.md). The README
says *what* the rules are; this file says *how to satisfy them*. Where the two disagree, this file wins.

- [Who owns what](#who-owns-what)
- [Getting set up](#getting-set-up)
- [Branch naming](#branch-naming)
- [Commit messages](#commit-messages)
- [Opening a pull request](#opening-a-pull-request)
- [Review rules](#review-rules)
- [Where your code goes](#where-your-code-goes)
- [Cross-module rules](#cross-module-rules)
- [Database migrations](#database-migrations)
- [Before you request review](#before-you-request-review)

---

## Who owns what

Each student owns their module end to end — controller, service, repository, migration, and the matching
frontend feature folder. You are the only person who can merge a change into your module.

| Module | Backend | Frontend | Owner | Student ID |
|---|---|---|---|---|
| Catalog & Inventory | `backend/catalog` | `frontend/features/catalog` | Amarasekara K.N. | IT25103193 |
| Order & Prescription | `backend/order` | `frontend/features/order` | Welikuburawatta M.D.L | IT25300262 |
| Workflow & Communication | `backend/workflow` | `frontend/features/workflow` | Karunarathna KHMMM | IT25101670 |
| Payment & Billing | `backend/billing` | `frontend/features/billing` | Kankanamge P M G | IT24300359 |
| Shared Infrastructure | `backend/shared`, `backend/contracts` | `frontend/features/shared` | Team-wide, by consensus | — |

**Shared code needs team consensus.** `shared` and `contracts` are not owned by one person. Open an issue,
get an explicit "yes" from at least two members in the PR thread, and say who approved it. Code in these
modules that nobody agreed to is the most expensive kind of code in the project.

---

## Getting set up

You need Java 26, Docker & Docker Compose, and Node.js. Maven comes from the wrapper — do not install
Maven, and do not commit a `mvn` binary.

```bash
git clone <repo-url>
cd my-opty-v2

# Backend: MySQL on :3306, then the API on :8080
cd backend
docker compose up -d
./mvnw spring-boot:run

# Frontend: Next.js on :3000, talks to the API on :8080
cd ../frontend
npm install
npm run dev
```

Flyway runs migrations automatically on startup. Swagger UI is at `http://localhost:8080/swagger-ui.html`.

**Configuration comes from environment variables, never from committed files.** Copy `.env.example` to
`.env` and fill it in. `.env` is git-ignored; a credential that reaches a commit is a credential that has
to be rotated, so treat it as a bug, not an inconvenience.

---

## Branch naming

```
<type>/<module>-<short-description>
```

| Type | Use for |
|---|---|
| `feat` | A new endpoint, page, or table |
| `fix` | A bug fix |
| `docs` | Documentation only |
| `refactor` | Behaviour-preserving restructuring |
| `test` | Tests added or fixed |
| `chore` | Build, config, dependency bumps |

Examples:

```
feat/catalog-add-frame-crud
fix/order-prescription-validation
docs/workflow-api-spec
chore/shared-bump-spring-boot
```

Use `chore/shared-*` or `feat/shared-*` for anything touching `shared` or `contracts`. Naming a branch
after your module makes the ownership question obvious in the PR list instead of something a reviewer has
to infer.

**Branch lifecycle:** create from `main` → work and commit → open a PR → review → CI green →
squash-merge → **delete the branch.**

- Never commit directly to `main`. If you find yourself needing to, something upstream is broken —
  fix that in its own PR.
- Keep branches short-lived. If a branch is more than a few days old or has drifted far from `main`,
  rebase it on `main` rather than accumulating conflicts.
- One concern per branch. A branch that both fixes a bug and adds a feature takes twice as long to
  review and is twice as likely to be merged broken.

---

## Commit messages

Conventional Commits:

```
<type>[optional scope]: <description>

[optional body]
```

Types: `feat`, `fix`, `docs`, `style`, `refactor`, `perf`, `test`, `chore`.

```
feat(order): add estimated receive date on approval
fix(catalog): decrement stock only when the order is approved
docs(billing): document refund states
```

The subject line is an instruction in the imperative mood, under ~72 characters, with no trailing period.
The body, if you write one, explains *why* — the diff already shows *what* changed. Write a body for any
commit a reader would otherwise have to reverse-engineer the reasoning behind.

We squash-merge, so individual commit messages inside your branch are for your own benefit and for anyone
bisecting mid-review. The PR title becomes the commit message on `main`, so write the PR title carefully.

---

## Opening a pull request

Fill in `.github/pull_request_template.md`. Specifically:

1. **Title** follows the commit convention: `feat(order): ...`.
2. **Module** — say which module you own.
3. **Screenshots or a request/response sample** for anything user-facing or endpoint-shaped.
4. **Migration** — if you added a `.sql` file, name it and say whether it is safe to run against a
   database that already has data.
5. **What you deliberately did not do** — scope you cut, or a follow-up you are handing to someone else.

Keep the description written for someone reviewing at 11pm. Explain the intent, not the mechanics.

---

## Review rules

A PR merges when **all** of the following hold:

- **≥1 approval** from a team member who is not the author.
- **The module owner's approval**, if the PR touches that module. This is non-negotiable even if you are
  the module owner — a self-approval is not a review. Owners: see [the table above](#who-owns-what).
- **CI is green** — backend build + tests, frontend lint + build.
- **No unresolved review comments.**
- **Squash-merged** with a conventional-commit title, then the branch is deleted.

Reviewers: read for correctness and for whether it fits the module, not for style preferences that no
linter enforces. If something is a matter of taste, say so and approve. If it is wrong or dangerous, say
exactly what breaks and block.

The module-owner rule is enforced by `.github/CODEOWNERS`. GitHub will not let you merge your own PR
against your own code path — that is the point of it, not an obstacle to route around.

---

## Where your code goes

**Backend** (`backend/<module>/src/main/java/com/myopty/<module>/`):

| Package | Holds |
|---|---|
| `controller` | `@RestController` classes. Parse input, call a service, map the response. No business rules. |
| `service` | `@Service` classes. The business rules live here. This is where your logic goes. |
| `repository` | Spring Data JDBC repositories. |
| `model` | Entities and enums. |
| `dto` | Request and response records. Never expose an entity to a controller. |
| `exception` | Module-local exceptions. |
| `config` | Module-local Spring configuration. |

`backend/shared/src/main/java/com/myopty/shared/` is organised **by feature** instead, because auth, users,
Q&A, and config are four different things rather than four layers of one: `auth/`, `user/`, `question/`,
`config/`.

`backend/contracts/` holds cross-module interfaces and events only. No Spring, no entities, no
repositories. Anything that another module needs from you goes here.

**Frontend** (`frontend/`):

- `app/` — routing and thin server components only. A page that contains a data-fetching function and
  four blocks of JSX belongs in `features/` instead.
- `app/(customer)/` and `app/(client)/` — route groups. The parentheses are not part of the URL; they
  exist so customers and the shop owner get separate layouts without duplicating one.
- `app/api/` — route handlers. These are the BFF layer that attaches the auth cookie and calls :8080.
- `features/<module>/` — your module's components, hooks, API client (`api.ts`), and validation schemas
  (`schemas.ts`). **This is your ownership boundary.**
- `components/ui/` — shared presentational primitives (button, table, modal, badge). Add here only when
  two or more features genuinely need it.
- `lib/api/` — the fetch wrapper. It attaches the auth header and unwraps the
  `{ success, data, meta }` envelope. Call this; do not call `fetch` directly from a feature.
- `lib/auth/` — session and role-guard helpers.
- `types/` — mirrors `backend/contracts`.

---

## Cross-module rules

These are the rules that keep four people from tangling their work. They are enforced by the Maven
reactor, not by trust, as long as you do not route around them.

1. **Modules never depend on each other.** No module's `pom.xml` may list another business module as a
   dependency. `order` reads stock through `contracts.catalog.StockQuery`; it does not depend on
   `catalog`. This is what makes "no circular dependencies" true instead of aspirational.
2. **Never import another module's entity.** No `import com.myopty.catalog.model.Frame` outside
   `catalog`. It won't compile — that is intentional. Entities are private to their module.
3. **Talk through `contracts`.** Put the interface in `backend/contracts/<provider>/`, implement it in the
   provider, inject it in the consumer. The interface returns DTOs or primitives, never entities.
4. **Use events for things that shouldn't block.** `OrderPlacedEvent`, `StockChangedEvent`,
   `PaymentCompletedEvent` live in `backend/contracts/event/`. A consumer must not assume the publisher
   knows it exists.
5. **Never reach into another module's database tables** with hand-written SQL, even for a read.
6. **`shared` entities are the exception.** `AppUser` and shared enums may be used across modules.

If you need something from another module that `contracts` doesn't expose, the answer is to add it to
`contracts` in a separate PR — not to add a Maven dependency.

---

## Database migrations

Each module owns its tables and writes its own Flyway migrations.

```
backend/<module>/src/main/resources/db/migration/V<version>__<module>_<description>.sql
```

Example: `backend/catalog/src/main/resources/db/migration/V2__catalog_create_frame_table.sql`

**Version numbers are allocated per module. Do not pick your own band.**

| Module | Version band |
|---|---|
| `shared` | V1 |
| `catalog` | V2 – V99 |
| `order` | V100 – V199 |
| `workflow` | V200 – V299 |
| `billing` | V300 – V399 |

Four people writing `V3__something` on the same afternoon is the predictable failure here, and Flyway
fails the whole build on a duplicate version. Take the next free number inside your band. Never edit an
already-merged migration — add a new one; the database has already run the old file.

Rules:

- One logical change per migration. Creating a table plus its indexes is fine; creating five unrelated
  tables is not.
- Migrations must run against a database that already has data, not only against a fresh one. Adding a
  `NOT NULL` column with no default needs a backfill step in the same file.
- Never write a destructive migration (`DROP`, `TRUNCATE`) without saying so explicitly in the PR. It
  runs on every environment.

---

## Before you request review

Run these yourself first. It is faster than waiting for CI to tell you the same thing.

```bash
# Backend — builds every module, runs all tests
cd backend && ./mvnw verify

# One module, one test
./mvnw test -pl catalog -Dtest='*Catalog*'

# Frontend
cd ../frontend && npm run lint && npm run build
```

Then confirm, before you push:

- [ ] Branch is named `<type>/<module>-<description>` and branched from `main`.
- [ ] Nothing of mine is outside my module, except via `contracts`.
- [ ] No `.env`, no credentials, no API keys in the diff.
- [ ] No commented-out code left behind.
- [ ] New endpoints are documented in the module section of `README.md`.
- [ ] Tests cover the new behaviour, and the existing suite still passes.
- [ ] Anything you deliberately left out is written in the PR description.
