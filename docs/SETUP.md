# Local Development Setup

Follow this once per machine. Afterwards, `./scripts/check-env.sh` is the fastest
answer to "is my machine set up right?" and to "why does this work for you and not
for me?".

The goal is that four people on four machines run the project with the same
versions, the same configuration keys, and the same local services. That is what
"works on my machine" costs to get wrong, so the versions are pinned in the
repository rather than left to each person's memory.

---

## What is pinned, and where

| Tool | Version | Pinned in | How |
|------|---------|-----------|-----|
| Java | 25 (Temurin 25.0.4) | `.sdkmanrc` | SDKMAN switches on `cd` |
| Node.js | 24 (LTS) | `.node-version` | fnm switches on `cd` |
| npm | ships with Node 24 | — | Don't upgrade it separately |
| Maven | 3.9.16 | `backend/mvnw` | Wrapper. Never install Maven |
| Docker + Compose | v2+ | — | Must be running |

Java 26 also works, but nothing in the project is tested against it — every
build and test run happens on 25, so a build that only fails on your machine is
worth reporting rather than working around.

---

## 1. Java

```bash
# SDKMAN, if you do not have it
curl -s "https://get.sdkman.io" | bash
source "$HOME/.sdkman/bin/sdkman-init.sh"

sdk install java 25.0.4-tem
sdk default java 25.0.4-tem
```

Then enable automatic switching so the right version is active the moment you
`cd` into the repository:

```bash
sdk config set sdkman_auto_env true   # in ~/.sdkman/etc/config
```

With that on, `.sdkmanrc` is picked up automatically. Without it you will
silently build with whatever `sdk default` happens to be.

Verify: `java -version` should print `25.x`. A JRE is not enough — you need
`javac`, which is what compiles the project.

---

## 2. Node.js

Do not use `apt install nodejs` on Ubuntu/Mint. It currently ships Node 18,
which Next.js will not run on.

```bash
curl -fsSL https://fnm.vercel.app/install | bash
source ~/.bashrc

fnm install 24
fnm default 24
```

fnm writes a small block into `~/.bashrc`. **Check where it landed.** By default
the installer appends it to the very end of the file, which is *after* this line
near the top:

```bash
# If not running interactively, don't do anything
case $- in
    *i*) ;;
      *) return;;
esac
```

Anything that runs without a TTY — a shell script, a Makefile recipe, some IDE
task runners — stops reading `.bashrc` at that `return` and never sees Node on
`PATH`. `bash -c 'node -v'` is enough to check:

```bash
bash -c 'node -v'    # must print v24.x, not "command not found"
```

If it prints nothing, move the `# fnm` block in `~/.bashrc` to **above** that
guard. It is safe there: the block only adds a directory to `PATH` and asks fnm
to set up a shell hook.

### One more PATH gap

Even with the block above the guard, a shell that never reads `.bashrc` at all
still has no Node. `~/.local/bin` is on the default `PATH` and is writable
without `sudo`, so point it at the version fnm installed:

```bash
FNM_BIN="$HOME/.local/share/fnm/node-versions/v24.21.0/installation/bin"
for b in node npm npx; do ln -sfn "$FNM_BIN/$b" "$HOME/.local/bin/$b"; done
```

These are symlinks *into* the fnm installation, not a second Node.
`check-env.sh` resolves them before reporting where Node came from, so it will
tell you if they are stale after a version bump. Re-run the three lines above
with the new version in the path if it does.

Verify: `node -v` and `npm -v` in a fresh terminal.

---

## 3. Docker

The database, the prescription object store, and the mail catcher all run in
containers. Install Docker Desktop, or Docker Engine plus the Compose v2 plugin
on Linux. Check the plugin specifically — the old standalone `docker-compose`
script is not enough:

```bash
docker compose version     # must print a version
docker info                # must succeed, not time out
```

---

## 4. Get the code and configure it

```bash
git clone https://github.com/Apeiro-code/my-opty-v2.git
cd my-opty-v2

cp .env.example .env
chmod 600 .env
```

`.env` is git-ignored and must stay that way. `.env.example` is committed and is
the contract: every variable the application reads, with the default that works
on a fresh clone. **If you need a variable that is not in it, add it** — a value
that only exists in your `.env` is a value the other three people do not have.

Now fill in the three secrets. Generate your own; never copy a teammate's, and
never commit one:

```bash
./scripts/check-env.sh --secrets
```

| Variable | Command |
|----------|---------|
| `DB_PASSWORD` | `openssl rand -hex 16` |
| `DB_ROOT_PASSWORD` | `openssl rand -hex 16` |
| `MINIO_SECRET_KEY` | `openssl rand -hex 24` |

There is no session secret to generate: login hands back a server-side session
cookie (section 6), so an empty authentication block in `.env` is correct.

These stay **blank on purpose**, because the local services need no credentials:

| Variable | Why blank |
|----------|-----------|
| `MAIL_USERNAME`, `MAIL_PASSWORD` | Mailpit accepts anything |
| `PAYMENT_GATEWAY_MERCHANT_ID`, `PAYMENT_GATEWAY_SECRET_KEY` | `PAYMENT_GATEWAY_PROVIDER=fake` needs none |

Registering a PayHere sandbox account or configuring a real SMTP provider for staging is
documented in [DEPLOYMENT.md](DEPLOYMENT.md). The moment you set
`PAYMENT_GATEWAY_PROVIDER` to something other than `fake`, the preflight and the
application both require those credentials, each naming the variable to fix.

### Two symlinks

The backend and the frontend each read `.env` from their own directory, but the file you just
created is at the repository root. Two symlinks fix that, and both are git-ignored:

```bash
ln -sfn ../.env backend/.env
ln -sfn ../.env frontend/.env
```

Without them:

- `docker compose` finds no `.env` beside `compose.yaml` and starts MySQL with an empty root
  password, or refuses to start at all.
- Next.js does not see `NEXT_PUBLIC_API_BASE_URL`, so the browser bundle is built pointing
  nowhere. It fails silently, which is worse.

Run them again after any `git clone` or a branch switch that removes them.

---

## 5. Check your machine

```bash
./scripts/check-env.sh
```

It exits non-zero if anything blocks a build, and prints the command that fixes
each problem. Expected result:

```
Java
  ok    java 25.0.4 (major 25 as required)
        vendor: Eclipse Adoptium

Node
  ok    node v24.21.0 (major 24 as required)
        npm  11.19.0
  ok    .node-version pins Node 24 (fnm reads it on cd)

Docker
  ok    docker 29.8.2
  ok    docker daemon is reachable
  ok    docker compose 5.6.0

Maven
  ok    backend/mvnw is executable
  ok    Maven comes from the wrapper; there is deliberately no committed mvn binary

Environment file
  ok    .env exists
  ok    .env defines every key in .env.example
  ok    every required secret has a value
  ok    .env is mode 600

Secret hygiene
  ok    .env is not tracked by git
  ok    .env.example is tracked, which is how the contract reaches everyone

Environment is ready.
```

The patch-level versions of Docker and Compose will differ from the sample above — those lines only
assert that a recent enough version is present. The `ok` on the `java`/`node` **major** version is the
part that matters, because a mismatched major fails the build in ways that look like source errors.

A `warn` is worth reading but does not stop you. A `FAIL` means the next error
you hit will be confusing and unrelated to the real cause.

---

## 6. Run it

```bash
# MySQL on :3307, Mailpit UI on :8025, then the API on :8080
cd backend
docker compose up -d
./mvnw spring-boot:run

# Next.js on :3000
cd ../frontend
npm install
npm run dev
```

Flyway applies migrations on startup, so a fresh database builds itself: 24 migrations create
22 tables, and a second start reports the schema is already up to date. Two of those migrations
(V7 and V105) also insert sample categories, frames, lenses and discounts, so browsing and
inventory features have realistic rows to work against — look rows up by model, name or slug
rather than by id, because seed ids are not fixed. Swagger UI is at
<http://localhost:8080/swagger-ui.html> and Mailpit at <http://localhost:8025>.

Migration `V1_3` seeds the only two accounts that exist, one per role:

| Role | Email | Password |
|---|---|---|
| `CUSTOMER` (a shopper) | `customer@myopty.local` | `customer123` |
| `CLIENT` (staff: catalog, shop, approvals) | `client@myopty.local` | `client123` |

`POST /api/auth/login` with either pair returns the session cookie every other `/api/**`
call needs. They are development fixtures with published passwords — there is no
registration flow yet, so any environment beyond this laptop must replace them before a
stranger can reach it.

Note **:3307**, not 3306. A native MySQL often already holds 3306 on developer
machines, and a port clash there stops the stack for a reason that looks
unrelated to ports.

To start from an empty database — which is the same thing as undoing a schema change you are
not sure about — drop the volume and bring it back:

```bash
cd backend && docker compose down -v && docker compose up -d
```

---

## 7. Staging preview

To see the site the way a reviewer would — one origin, no dev servers running —
the compose overlay builds the API and the web app into images and puts them
behind an nginx proxy:

```bash
cd backend
docker compose -f compose.yaml -f compose.prod.yaml up -d --build
```

Then open <http://localhost:8088>. The proxy routes `/api`, `/actuator`,
`/swagger` and `/v3/api-docs` to the API and everything else to Next.js, so the
preview needs neither CORS nor `NEXT_PUBLIC_API_BASE_URL` (the web image is built
with it empty on purpose). The port comes from `PREVIEW_PORT` in `.env`.

Notes:

- First build compiles the Maven reactor and the Next.js bundle; both are cached,
  so later builds only redo what changed.
- The preview is bound to `127.0.0.1` only. The API has login and roles now, but its
  only accounts are the seeded demo ones (see section 6) — do not publish it on a
  routable address.
- It reuses the same MySQL container as the dev workflow, so your migrations and
  seed data are already there. To stop the preview but keep the data:
  `docker compose -f compose.yaml -f compose.prod.yaml down` (add `-v` only if
  you want to drop the database too).
- Plain `docker compose up -d` (dev) prints a warning about "orphan containers"
  while the preview is running. That is Compose noticing services it was not
  given this time; it changes nothing. The command above stops them properly.

---

## Troubleshooting

| Symptom | Cause | Fix |
|---------|-------|-----|
| `node: command not found` in a script but fine in your terminal | fnm block sits below the non-interactive guard | Move the `# fnm` block above it in `~/.bashrc` |
| `bash -c 'node -v'` fails | Shell never reads `.bashrc` | Symlink node/npm/npx into `~/.local/bin` (section 2) |
| `cannot find symbol` on every class | JRE, not JDK | `sdk install java 25.0.4-tem` |
| Flyway cannot connect | Container not up, or port 3306 assumed | `docker compose ps`; confirm `DB_URL` says 3307 |
| `docker compose` says a variable is unset | `backend/.env` symlink missing | `ln -sfn ../.env backend/.env` (section 4) |
| Frontend calls nothing / bundle points at `localhost:3000` for the API | `frontend/.env` symlink missing | `ln -sfn ../.env frontend/.env` (section 4) |
| `./mvnw spring-boot:run` says "Unable to find a suitable main class" | Run from the wrong directory | `cd backend` first; only `myopty-app` is runnable |
| Mail arrives nowhere | Looking at your inbox | Mailpit is local: <http://localhost:8025> |
| Preview site shows "502" or "can't connect" | API still starting, or image stale | Wait for `/actuator/health`, or re-run the overlay command with `--build` |
| `docker compose` build fails on `mvnw: permission denied` | Wrapper lost its exec bit | `chmod +x backend/mvnw` |
| `401` on every API call | Not logged in, or the session expired | `POST /api/auth/login` with a demo account (section 6); logout ends the session on purpose |
| `403` on a shop route | Logged in as `CUSTOMER` | Shop routes need the `CLIENT` account (section 6) |
| A teammate's value is not in your `.env` | They added a key and did not say so | `cp .env.example .env`, re-apply your secrets |
| Line endings churn the whole file | Windows editor writing CRLF | `.gitattributes` normalises to LF; check the editor is not fighting it |

MinIO is not in `compose.yaml` yet. It holds prescription uploads, which is the
prescription story rather than the skeleton, and the image is not pullable on every
machine — a service that cannot start would fail `docker compose up -d` for a reason
unrelated to the code. It arrives with that story; the `MINIO_*` keys in `.env` are
already there waiting for it.

---

## Windows and WSL

Only the Linux path above has been verified. On Windows, `fnm` installs with
`winget install Schniz.fnm` or `choco install fnm`, SDKMAN does not exist, so
install Temurin 25 directly and set `JAVA_HOME`. WSL is the smoother option: it
runs these instructions unchanged.

On any platform, `.gitattributes` forces LF in the repository so Windows and
Linux contributors do not produce whole-file diffs over a one-line change.
