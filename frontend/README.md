# Frontend

Next.js 16 App Router, React 19, TypeScript, Tailwind CSS v4. Runs on <http://localhost:3000>.

```bash
npm install
npm run dev      # dev server with Turbopack
npm run lint     # eslint
npm run build    # production build + type check
```

## What exists

Only enough to make the shape of the app visible and to prove the toolchain works. There are no
features, no data fetching in any component, and the only route that renders is `/`.

| Path | What it is |
|---|---|
| `app/layout.tsx` | Root HTML/body, metadata, `globals.css` |
| `app/(customer)/page.tsx` | Public storefront landing page — the one working route |
| `app/(customer)/layout.tsx` | Storefront header and nav |
| `app/(client)/layout.tsx` | Shop-owner nav |
| `lib/api/client.ts` | The single `fetch` wrapper |
| `types/api.ts` | `ApiResult` / `ApiError` / `ApiMeta` |

Everything else — `app/api/`, `features/`, `components/ui/`, `lib/auth/`, and the `(customer)` and
`(client)` subdirectories — is an empty directory holding a `.gitkeep`. The shape is agreed; the code is
not written.

## Three rules this skeleton exists to enforce

**1. Route groups are for layout, not for URLs.** `(customer)` and `(client)` are invisible to the URL.
They do not namespace anything, which is deliberate — a customer's URL should not read like a dashboard.
But it means two groups cannot both own the same path.

**2. `lib/api/client.ts` is the only place `fetch` is called.** It holds the base URL, the CORS
credentials and the envelope unwrapping in one place. A component that calls `fetch` itself silently opts
out of all three, and then the same base-URL bug gets fixed four times.

**3. Server components by default.** `'use client'` is a decision, not a default. It moves your code into
the browser bundle, so it is only worth paying for when the component genuinely needs state, an effect or
an event handler.

## Routes

The two route groups are **not** namespaced in the URL — that is deliberate, because a customer's
address should read like a shopfront rather than like a dashboard. A customer sees `/orders`; the shop
owner sees `/shop/orders`. The `/shop` segment is what separates them.

| URL | Directory | Audience |
|---|---|---|
| `/` | `app/(customer)/page.tsx` | Anyone |
| `/frames` | `app/(customer)/frames/` | Anyone |
| `/lenses` | `app/(customer)/lenses/` | Anyone |
| `/questions` | `app/(customer)/questions/` | Anyone |
| `/prescriptions` | `app/(customer)/prescriptions/` | Customer |
| `/orders` | `app/(customer)/orders/` | Customer — their own orders |
| `/account` | `app/(customer)/account/` | Customer |
| `/shop/orders` | `app/(client)/shop/orders/` | Shop owner — the approval queue |
| `/shop/inventory` | `app/(client)/shop/inventory/` | Shop owner |
| `/shop/tasks` | `app/(client)/shop/tasks/` | Shop owner |
| `/shop/billing` | `app/(client)/shop/billing/` | Shop owner |
| `/api/*` | `app/api/` | BFF route handlers |

Only `/` has a page. Every other directory holds a `.gitkeep` and nothing else.

### Why `/shop` and not `/admin`, and why on every route

`/admin` would be wrong — this is one shop's owner, not a platform administrator. `/client` would be
worse: it sits confusingly next to "customer" in the same product. `/shop` says what the area is.

The prefix goes on **every** owner route, not just the ones that would otherwise collide. Two reasons:

- **One rule.** Prefixing only `/orders` would leave `/shop/orders` sitting next to `/inventory`, and
  the next person to add a colliding route has to make the same judgement call again.
- **One matcher.** When authentication lands, protecting the owner area is a single
  `matcher: ["/shop/:path*"]` in middleware. Without the prefix it is a hand-maintained list of paths
  that is silently incomplete the first time someone adds a route.

This also means `/shop/*` is greppable: one glob finds every owner-facing page, which is what you want
when auditing "what can a logged-out visitor reach".

### The collision this prevents

Both groups originally held an `orders/` directory, so both resolved to `/orders`. With neither
containing a page it built fine, which is exactly what made it worth fixing early. The moment two pages
existed, Next.js failed:

```
Error: You cannot have two parallel pages that resolve to the same path.
Please check /(client)/orders and /(customer).
```

That error is the reason to care: it appears at build time, but only *after* someone has written two
pages and possibly linked to them. Changing URL structure later is a breaking change. Verified by
building all twelve routes together — they generate cleanly.

One trap when writing a layout in a group: `LayoutProps` only accepts `"/"`, so a layout in
`app/(client)/` is still `LayoutProps<"/">`. Writing `LayoutProps<"/shop">` fails type checking with
`Type '"/shop"' does not satisfy the constraint '"/"'`, which is confusing because it reads like the
segment is wrong. It is not — the type is not parameterised by route.

## Known: 5 high `npm audit` findings — do not "fix" these

`npm audit` reports 5 high vulnerabilities. All five are the same advisory, reached through one chain:

```
eslint-config-next -> @next/eslint-plugin-next -> fast-glob -> micromatch -> braces
```

`braces` has a stack-exhaustion DoS (GHSA-vfj7-8cjw-p6xm) via deeply nested glob patterns. What that
means in practice:

- **It is dev-only.** `npm ls braces --omit=dev` is empty. None of the chain is in the production
  dependency tree, and none of it appears in `.next/static` — it never reaches a browser. It is build-time
  lint tooling.
- **There is no patched version to upgrade to.** The installed `braces@3.0.3` is the newest release
  published. `npm audit` offers a fix only by installing `eslint-config-next@14.2.35` — downgrading the
  lint config from the Next 16 major to the Next 14 one. That breaks Next.js 16 linting in exchange for
  removing a vulnerability from code that never runs in production.

So: **do not run `npm audit fix --force` here.** It will downgrade `eslint-config-next` and silently
degrade lint coverage for every rule that changed between Next 14 and 16. If a future `braces` release
fixes this, it will arrive through the normal `npm update` of `eslint-config-next` — no override needed.

Re-evaluate if this chain ever appears in `dependencies` rather than `devDependencies`, because then it
is reachable from the build and the calculus changes.

## Environment

`.env` is a symlink to the repository root, because Next.js only reads `.env` from its own directory (or
one level up in some configurations — the symlink removes the ambiguity). Without it, `NEXT_PUBLIC_API_BASE_URL`
is undefined and the bundle is built pointing nowhere, which fails silently.

```
NEXT_PUBLIC_API_BASE_URL=http://localhost:8080
```

`NEXT_PUBLIC_` variables are inlined into the browser bundle at build time. That is why this one is public:
it is not a secret, and it cannot be changed without a rebuild. Never put anything sensitive behind that
prefix.

The backend's CORS config lists `http://localhost:3000` in `CORS_ALLOWED_ORIGINS`. If you change the dev
port, add the new origin there too — the browser blocks the response before your code sees it, and the
error names CORS rather than the thing you changed.
