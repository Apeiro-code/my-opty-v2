#!/usr/bin/env bash
#
# The coding standard, in one command:
#
#     ./scripts/lint.sh
#
# Runs every linter the repository has and exits non-zero if any of them fails:
#
#   - backend   ./mvnw spotless:check   (Palantir format; fix with spotless:apply)
#   - frontend  npm run lint            (ESLint, flat config)
#   - frontend  npm run format:check    (Prettier; fix with npm run format)
#
# `./mvnw verify` also runs spotless:check, so a dirty Java file fails the build
# even if you never run this script. This exists so you can find out *before*
# the build, from the repository root, in one command.

set -uo pipefail

REPO_ROOT="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$REPO_ROOT" || exit 1

if [ -t 1 ]; then
  RED=$'\033[31m'; GREEN=$'\033[32m'; BOLD=$'\033[1m'; DIM=$'\033[2m'; RESET=$'\033[0m'
else
  RED=''; GREEN=''; BOLD=''; DIM=''; RESET=''
fi

pass() { printf '  %sok%s    %s\n' "$GREEN" "$RESET" "$1"; }
fail() { printf '  %sFAIL%s  %s\n' "$RED" "$RESET" "$1"; FAILURES=$((FAILURES + 1)); }

FAILURES=0

printf '%s%s%s\n' "$BOLD" "Lint: backend (Spotless)" "$RESET"
if (cd backend && ./mvnw -q spotless:check); then
  pass "spotless:check"
else
  fail "spotless:check — fix with: cd backend && ./mvnw spotless:apply"
fi

printf '\n%s%s%s\n' "$BOLD" "Lint: frontend (ESLint + Prettier)" "$RESET"
if [ ! -d frontend/node_modules ]; then
  fail "frontend/node_modules is missing — run: cd frontend && npm install"
else
  if (cd frontend && npm run --silent lint); then
    pass "eslint"
  else
    fail "eslint"
  fi
  if (cd frontend && npm run --silent format:check); then
    pass "prettier --check"
  else
    fail "prettier --check — fix with: cd frontend && npm run format"
  fi
fi

printf '\n'
if [ "$FAILURES" -eq 0 ]; then
  printf '%sAll linters passed.%s\n' "$GREEN" "$RESET"
  exit 0
fi
printf '%s%d linter(s) failed.%s The message above says which, and how to fix it.\n' "$RED" "$FAILURES" "$RESET"
exit 1
