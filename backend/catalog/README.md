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
`GET`/`POST /api/shop/lenses` (list and add), `PUT /api/shop/lenses/{id}` (edit),
and `GET /api/shop/categories` (the filing categories a frame or lens may be
assigned to). They sit under `/api/shop/**`, so the filter chain's `ROLE_CLIENT`
rule is the only place the role is checked, and they never touch the storefront's
public read surface.

Also **built:** `GET /api/shop/inventory/report` — the live availability report.
It reads every active frame and lens and bands each as `IN_STOCK`, `LOW_STOCK`
(`0 < stock_qty <= low_stock_threshold`) or `OUT_OF_STOCK` (`stock_qty = 0`),
returning the three counts and the per-item rows. It is computed on read and does
not write the `availability_report` snapshot table, which belongs to the separate
export story.

This module provides the `contracts.catalog.StockManager` seam. `StockService`
implements it: `reserve` decrements a frame/lens with a conditional update
(`... WHERE id = ? AND stock_qty >= ?`, so a concurrent order cannot oversell),
throws `InsufficientStockException` when the shelf cannot cover the order, and
writes a `SALE` row to `stock_entry`; `release` restores the units and writes a
`RETURN` row. `order` calls these on order placement and rejection, so stock moves
without either module reading the other's tables.

A frame or lens carries an optional `categoryId`. When one is supplied, the
service checks the category exists and that its `item_type` accepts the record
(`FRAME`, `LENS`, or `BOTH`); filing under the wrong product is a 400
`INVALID_CATEGORY`. The categories themselves are the seeded rows from V7 — this
story files records into existing categories, it does not create them.

Nothing else is implemented yet. This module compiles and its tables exist; the
remaining endpoints land with the features.

## Depends on

`contracts`, `shared`.

It must not depend on another business module. If you need something from `order`, put an
interface in `backend/contracts/order/` and let the reactor enforce it — an `import
com.myopty.order.model.*` does not compile here, which is the point.
