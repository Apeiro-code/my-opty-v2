#!/usr/bin/env bash
#
# Preflight check for the MyOpty local development environment.
#
# Run this before blaming the code:
#
#     ./scripts/check-env.sh
#
# Exits 0 when the machine can build and run the project, 1 otherwise. Warnings
# do not fail the run; they are the things that work today and will surprise you
# later.
#
# The pinned versions live in the repository (.node-version, .sdkmanrc, and this
# script) rather than in this file's logic, so changing them is a reviewable
# commit instead of a thing everyone has to be told about.

set -uo pipefail

REPO_ROOT="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$REPO_ROOT" || exit 1

# `--secrets` prints the commands that fill in the blank secrets and exits. It is
# referenced from the failure messages below, so it has to exist.
if [ "${1:-}" = "--secrets" ]; then
  cat <<'SECRETS'
Fill in the blank secrets in .env. Each line replaces the empty value for that
key with a freshly generated one. They are printed, not written: read them into
your .env deliberately rather than piping a secret through a shell history.
SECRETS
  printf '\n  openssl rand -hex 16      # DB_PASSWORD\n'
  printf '  openssl rand -hex 16      # DB_ROOT_PASSWORD\n'
  printf '  openssl rand -hex 24      # MINIO_SECRET_KEY\n'
  printf '\nThen re-run: ./scripts/check-env.sh\n'
  exit 0
fi

# Versions the project is built and run against.
REQUIRED_JAVA_MAJOR=25
REQUIRED_NODE_MAJOR="$(tr -d '[:space:]' < .node-version 2>/dev/null || echo 24)"
REQUIRED_JAVA_BUILD="$(grep -E '^java=' .sdkmanrc 2>/dev/null | cut -d= -f2 || echo 25.0.4-tem)"

# Keys that must carry a real value in a local .env. See the comment above the
# loop that reads it for why this is a list rather than "whatever is blank".
REQUIRED_SECRET_KEYS="DB_PASSWORD DB_ROOT_PASSWORD MINIO_SECRET_KEY"

FAILURES=0
WARNINGS=0

if [ -t 1 ]; then
  RED=$'\033[31m'; GREEN=$'\033[32m'; YELLOW=$'\033[33m'; BOLD=$'\033[1m'; DIM=$'\033[2m'; RESET=$'\033[0m'
else
  RED=''; GREEN=''; YELLOW=''; BOLD=''; DIM=''; RESET=''
fi

section() { printf '\n%s%s%s\n' "$BOLD" "$1" "$RESET"; }
pass()    { printf '  %sok%s    %s\n' "$GREEN" "$RESET" "$1"; }
warn()    { printf '  %swarn%s  %s\n' "$YELLOW" "$RESET" "$1"; WARNINGS=$((WARNINGS + 1)); }
fail()    { printf '  %sFAIL%s  %s\n' "$RED" "$RESET" "$1"; FAILURES=$((FAILURES + 1)); }
detail()  { printf '        %s%s%s\n' "$DIM" "$1" "$RESET"; }
hint()    { printf '        %s-> %s%s\n' "$DIM" "$1" "$RESET"; }


# --- Java -------------------------------------------------------------------
section "Java"

if ! command -v java >/dev/null 2>&1; then
  fail "java is not on PATH"
  hint "sdk install java $REQUIRED_JAVA_BUILD"
elif ! command -v javac >/dev/null 2>&1; then
  fail "javac is not on PATH (a JRE is installed, but the project needs a JDK)"
  hint "sdk install java $REQUIRED_JAVA_BUILD"
else
  java_version="$(java -version 2>&1 | awk -F'"' '/version/ {print $2; exit}')"
  java_major="${java_version%%.*}"

  if [ "$java_major" = "$REQUIRED_JAVA_MAJOR" ]; then
    pass "java $java_version (major $REQUIRED_JAVA_MAJOR as required)"
  elif [ "$java_major" -lt "$REQUIRED_JAVA_MAJOR" ] 2>/dev/null; then
    fail "java $java_version is older than the required major $REQUIRED_JAVA_MAJOR"
    hint "sdk install java $REQUIRED_JAVA_BUILD && sdk default java $REQUIRED_JAVA_BUILD"
  else
    fail "java $java_version is newer than the pinned major $REQUIRED_JAVA_MAJOR"
    hint "The build targets release $REQUIRED_JAVA_MAJOR. A newer JDK usually works, but CI"
    hint "uses $REQUIRED_JAVA_MAJOR, so a failure that only you see is worth reporting."
  fi

  java_vendor="$(java -XshowSettings:properties -version 2>&1 | sed -n 's/^ *java.vendor = //p' | head -1)"
  [ -n "$java_vendor" ] && detail "vendor: $java_vendor"

  case "$java_vendor" in
    *Eclipse\ Adoptium*|*Temurin*) ;;
    *) warn "not a Temurin/Adoptium build ($java_vendor); CI uses Temurin, so differences are possible" ;;
  esac
fi


# --- Node -------------------------------------------------------------------
section "Node"

if ! command -v node >/dev/null 2>&1; then
  fail "node is not on PATH"
  hint "curl -fsSL https://fnm.vercel.app/install | bash"
  hint "fnm install && fnm default 24"
elif ! command -v npm >/dev/null 2>&1; then
  fail "npm is not on PATH even though node is"
  hint "fnm install 24 && fnm default 24"
else
  node_version="$(node -v 2>/dev/null)"
  node_major="${node_version#v}"
  node_major="${node_major%%.*}"

  if [ "$node_major" = "$REQUIRED_NODE_MAJOR" ]; then
    pass "node $node_version (major $REQUIRED_NODE_MAJOR as required)"
  elif [ "$node_major" -lt "$REQUIRED_NODE_MAJOR" ] 2>/dev/null; then
    fail "node $node_version is older than the required major $REQUIRED_NODE_MAJOR"
    hint "Next.js needs a newer Node than this."
    hint "fnm install $REQUIRED_NODE_MAJOR && fnm default $REQUIRED_NODE_MAJOR"
  else
    warn "node $node_version is newer than the pinned major $REQUIRED_NODE_MAJOR"
    hint "Newer majors have broken Next.js before. Use the pinned major if you hit odd errors."
  fi

  detail "npm  $(npm -v 2>/dev/null)"

  # fnm switches versions by rewriting PATH. If node came from somewhere else
  # entirely, `fnm use` will silently not affect the terminal you are in.
  # Resolve first: the documented setup symlinks node into ~/.local/bin so that
  # shells which never read ~/.bashrc still find it, and that symlink is not a
  # second Node -- it points into the fnm installation.
  node_path="$(command -v node)"
  node_real="$(readlink -f "$node_path" 2>/dev/null || echo "$node_path")"
  case "$node_real" in
    *fnm*) : ;;
    *) warn "node resolves to $node_real, which is outside fnm"
       hint "fnm will not switch this one. Remove any other Node from PATH, or use that Node." ;;
  esac
  [ "$node_path" != "$node_real" ] && detail "via $node_path"
fi

if [ -f .node-version ]; then
  pass ".node-version pins Node $REQUIRED_NODE_MAJOR (fnm reads it on cd)"
else
  fail ".node-version is missing, so Node is not pinned for the team"
fi


# --- Docker -----------------------------------------------------------------
section "Docker"

if ! command -v docker >/dev/null 2>&1; then
  fail "docker is not on PATH"
  hint "The database and object store run in containers; without Docker there is nowhere to store data."
else
  pass "docker $(docker --version 2>/dev/null | awk '{print $3}' | tr -d ',' )"

  if docker info >/dev/null 2>&1; then
    pass "docker daemon is reachable"
  else
    fail "docker is installed but the daemon is not responding"
    hint "Start Docker Desktop, or on Linux: sudo systemctl start docker"
  fi

  if docker compose version >/dev/null 2>&1; then
    pass "docker compose $(docker compose version --short 2>/dev/null)"
  else
    fail "the 'docker compose' plugin is missing (the old 'docker-compose' script will not do)"
    hint "Install the Compose v2 plugin"
  fi
fi


# --- Maven ------------------------------------------------------------------
# Only checked once the wrapper exists; it is part of the backend skeleton.
section "Maven"

if [ -f backend/mvnw ]; then
  if [ -x backend/mvnw ]; then
    pass "backend/mvnw is executable"
  else
    fail "backend/mvnw is not executable"
    hint "chmod +x backend/mvnw"
  fi
  pass "Maven comes from the wrapper; there is deliberately no committed mvn binary"
else
  printf '  %sskip%s  backend/mvnw not added yet (Maven arrives with the backend skeleton)\n' "$DIM" "$RESET"
fi


# --- Environment file -------------------------------------------------------
section "Environment file"

if [ ! -f .env.example ]; then
  fail ".env.example is missing from the repository"
elif [ ! -f .env ]; then
  fail ".env does not exist"
  hint "cp .env.example .env"
  hint "Then fill in the blank secrets; check-env.sh --secrets prints the commands."
else
  pass ".env exists"

  # Every key in the example must exist in the local file, or the application
  # silently falls back to a default that is wrong on your machine only.
  missing_keys=""
  while IFS= read -r key; do
    [ -z "$key" ] && continue
    if ! grep -qE "^${key}=" .env; then
      missing_keys="$missing_keys $key"
    fi
  done < <(grep -oE '^[A-Z][A-Z0-9_]*=' .env.example | tr -d '=')

  if [ -n "$missing_keys" ]; then
    fail "these keys are in .env.example but not in .env:"
    for key in $missing_keys; do detail "$key"; done
    hint "cp .env.example .env again, then re-apply your own secrets."
  else
    pass ".env defines every key in .env.example"
  fi

  # Explicit list, not "every key whose example value is blank".
  #
  # Several keys are deliberately blank in .env.example and must stay blank on a
  # developer's machine: Mailpit accepts no credentials, and the fake payment
  # gateway has none. Treating blank-as-required would demand values for all of
  # them and train people to fill in junk to make a checker quiet.
  #
  # These three are the ones where a blank value is either a crash or a security
  # problem, so they are named here. Adding a new one is a one-word change.
  blank_secrets=""
  for key in $REQUIRED_SECRET_KEYS; do
    value="$(grep -m1 -E "^${key}=" .env | cut -d= -f2- || true)"
    if [ -z "$value" ]; then
      blank_secrets="$blank_secrets $key"
    fi
  done

  case "$blank_secrets" in
    "")
      pass "every required secret has a value"
      ;;
    *)
      fail "these secrets are still blank in .env:"
      for key in $blank_secrets; do detail "$key"; done
      hint "openssl rand -hex 16   # DB_PASSWORD, DB_ROOT_PASSWORD"
      hint "openssl rand -hex 24   # MINIO_SECRET_KEY"
      hint "./scripts/check-env.sh --secrets prints these on their own."
      hint "Each developer generates their own. Never copy them from a teammate."
      ;;
  esac

  # Provider-conditional requirements. `fake` needs no merchant account, so
  # demanding these unconditionally would fail every developer who has not
  # registered one. The moment a real provider is named the credentials stop
  # being optional — the application refuses to start without them, and the
  # preflight says so first. docs/DEPLOYMENT.md is the registration runbook.
  payment_provider="$(grep -m1 -E '^PAYMENT_GATEWAY_PROVIDER=' .env | cut -d= -f2- || true)"
  if [ -n "$payment_provider" ] && [ "$payment_provider" != "fake" ]; then
    missing_creds=""
    for key in PAYMENT_GATEWAY_MERCHANT_ID PAYMENT_GATEWAY_SECRET_KEY; do
      cred_value="$(grep -m1 -E "^${key}=" .env | cut -d= -f2- || true)"
      [ -z "$cred_value" ] && missing_creds="$missing_creds $key"
    done
    if [ -n "$missing_creds" ]; then
      fail "PAYMENT_GATEWAY_PROVIDER=$payment_provider but these are blank:"
      for key in $missing_creds; do detail "$key"; done
      hint "Copy the sandbox values per docs/DEPLOYMENT.md, or set PAYMENT_GATEWAY_PROVIDER=fake."
    else
      pass "PAYMENT_GATEWAY_PROVIDER=$payment_provider has its credentials"
    fi
  else
    pass "PAYMENT_GATEWAY_PROVIDER=fake needs no merchant account"
  fi

  # Mail follows the same shape but only warns: some relays on a private network
  # authenticate nothing, and a real send failure is loud where it happens.
  mail_host="$(grep -m1 -E '^MAIL_HOST=' .env | cut -d= -f2- || true)"
  if [ -z "$mail_host" ] || [ "$mail_host" = "localhost" ]; then
    pass "MAIL_HOST is local (Mailpit) and needs no credentials"
  else
    mail_user="$(grep -m1 -E '^MAIL_USERNAME=' .env | cut -d= -f2- || true)"
    if [ -z "$mail_user" ]; then
      warn "MAIL_HOST=$mail_host but MAIL_USERNAME is blank"
      hint "Staging SMTP providers need credentials — docs/DEPLOYMENT.md has the recipe."
    else
      pass "MAIL_HOST=$mail_host has a MAIL_USERNAME"
    fi
  fi

  # A .env copied weeks ago is the quiet cause of "works on my machine": the
  # example moved on and this file did not. Generated secrets always differ, so
  # they are excluded from the comparison.
  drifted=""
  while IFS= read -r key; do
    [ -z "$key" ] && continue
    case " $REQUIRED_SECRET_KEYS " in *" $key "*) continue ;; esac
    example_value="$(grep -m1 -E "^${key}=" .env.example | cut -d= -f2- || true)"
    local_value="$(grep -m1 -E "^${key}=" .env | cut -d= -f2- || true)"
    [ "$example_value" != "$local_value" ] && drifted="$drifted $key"
  done < <(grep -oE '^[A-Z][A-Z0-9_]*=' .env.example | tr -d '=')

  if [ -n "$drifted" ]; then
    warn ".env differs from .env.example on:"
    for key in $drifted; do detail "$key"; done
    hint "Expected if you have deliberately changed one. Otherwise your .env is older"
    hint "than the committed contract: diff it against .env.example."
  fi

  env_mode="$(stat -c '%a' .env 2>/dev/null || stat -f '%Lp' .env 2>/dev/null || echo '')"
  if [ -n "$env_mode" ] && [ "$env_mode" != "600" ]; then
    warn ".env is mode $env_mode, readable beyond your own account"
    hint "chmod 600 .env"
  else
    pass ".env is mode 600"
  fi
fi


# --- Secret hygiene ---------------------------------------------------------
section "Secret hygiene"

if git rev-parse --git-dir >/dev/null 2>&1; then
  if git ls-files --error-unmatch .env >/dev/null 2>&1; then
    fail ".env is tracked by git"
    hint "git rm --cached .env"
    hint "If it has real values in history, rotate them. Removing it now does not unpublish the past."
  else
    pass ".env is not tracked by git"
  fi

  tracked_secrets="$(git ls-files 2>/dev/null | grep -E '(^|/)\.env($|\.)' | grep -v '\.example$' || true)"
  if [ -n "$tracked_secrets" ]; then
    fail "these env files are tracked and should not be:"
    for f in $tracked_secrets; do detail "$f"; done
  fi

  if git ls-files --error-unmatch .env.example >/dev/null 2>&1; then
    pass ".env.example is tracked, which is how the contract reaches everyone"
  else
    warn ".env.example is not tracked yet (new file, not staged)"
  fi
else
  printf '  %sskip%s  not a git repository\n' "$DIM" "$RESET"
fi


# --- Summary ----------------------------------------------------------------
printf '\n%s' "$BOLD"
if [ "$FAILURES" -gt 0 ]; then
  printf '%s%d problem(s) found.%s' "$RED" "$FAILURES" "$RESET"
elif [ "$WARNINGS" -gt 0 ]; then
  printf '%sEnvironment is usable, with %d warning(s).%s' "$YELLOW" "$WARNINGS" "$RESET"
else
  printf '%sEnvironment is ready.%s' "$GREEN" "$RESET"
fi
printf '%s\n\n' "$RESET"

[ "$FAILURES" -gt 0 ] && exit 1
exit 0
