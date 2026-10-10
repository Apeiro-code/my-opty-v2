# Catalog & Inventory

Frames, lenses, categories and stock. Owner: Amarasekara K.N.

## Tables owned here

| Table | What it holds |
|---|---|
| `category` | Browsing categories, self-referencing for a hierarchy |
| `frame` | Frames, with price and current stock |
| `lens` | Lenses, with type and coating |
| `stock_entry` | Audit log of every stock movement |
| `availability_report` | Generated inventory snapshots |

All DDL is written by Flyway, from `catalog/src/main/resources/db/migration`, and nothing else may create or alter them.
The migration version band is **V2-V99** — take the next free number inside it, never one
from another module's band, because four people writing `V3__` on the same afternoon is a
duplicate-version failure that fails the whole build.

## Endpoints owned here

Planned: `/api/frames`, `/api/lenses`, `/api/categories`,
`/api/inventory/report`, `/api/inventory/alerts`, `/api/inventory/report/export`.

**Built:** the shop owner's frame and lens stories —
`GET /api/shop/frames` (inventory list), `POST /api/shop/frames` (add),
`PUT /api/shop/frames/{id}` (edit), `DELETE /api/shop/frames/{id}` (discontinue —
a soft hide that sets `is_active` false and keeps the row so old orders resolve),
and `GET`/`POST /api/shop/lenses` (list and add). They sit under `/api/shop/**`,
so the filter chain's `ROLE_CLIENT` rule is the only place the role is checked,
and they never touch the storefront's public read surface.

Nothing else is implemented yet. This module compiles and its tables exist; the
remaining endpoints land with the features.

## Depends on

`contracts`, `shared`.

It must not depend on another business module. If you need something from `order`, put an
interface in `backend/contracts/order/` and let the reactor enforce it — an `import
com.myopty.order.model.*` does not compile here, which is the point.
