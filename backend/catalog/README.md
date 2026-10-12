# Catalog & Inventory

Frames, lenses, categories and stock. Owner: Amarasekara K.N.

## Tables owned here

| Table | What it holds |
|---|---|
| `category` | Browsing categories, self-referencing for a hierarchy |
| `frame` | Frames, with price and current stock |
| `lens` | Lenses, with type and coating |
| `stock_entry` | Audit log of every stock movement |
| `availability_report` | Reserved for generated snapshots — unused, the report is computed live |

All DDL is written by Flyway, from `catalog/src/main/resources/db/migration`, and nothing else may create or alter them.
The migration version band is **V2-V99** — take the next free number inside it, never one
from another module's band, because four people writing `V3__` on the same afternoon is a
duplicate-version failure that fails the whole build.

## Endpoints owned here

The public storefront reads `/api/frames`, `/api/lenses`, `/api/categories`.
**Built:** `GET /api/frames` — the anonymous shop wall. It sits outside
`/api/shop/**` and is on the filter chain's public list, so it needs no session.
It returns only active frames, oldest first, and only through
`StorefrontFrameResponse`: `id`, `model`, `color`, `material`, `price`, `imageUrl`
and a friendly `inStock` flag. The exact `stockQty`, the `active` flag and the
category stay on the owner's `FrameResponse`, so the narrower public record is
what keeps an owner-only field from leaking onto the storefront later. The
`image_url` column is mapped on `Frame` for this read; the owner's create writes
null and an update keeps the stored value. `/api/lenses` and `/api/categories`
are not built yet — nothing serves the storefront's `/lenses` page.

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
not write the `availability_report` snapshot table.

Also **built:** the two stories that read that report. `GET
/api/shop/inventory/alerts` is the same items narrowed to the ones not `IN_STOCK`,
worst first, for the in-app banner. `GET
/api/shop/inventory/report/export?format=pdf|xlsx` renders the whole report as a
downloadable file — OpenPDF for the PDF, Apache POI for the workbook — and writes
nothing server-side. An unknown `format` is a 400 `INVALID_PARAMETER`; a render
failure is a 500 `EXPORT_FAILED`; `ExportFormatConverter` makes `format` accept
either case.

The low-stock alert also leaves the process: `StockService.reserve` asks
`LowStockAlerts` which of its movements crossed a threshold downward, and, once
the surrounding transaction commits, `EmailLowStockAlertSender` mails a summary to
the `myopty.alerts.recipient-email` address (`LOW_STOCK_ALERT_EMAIL`, blank
disables it; `LOW_STOCK_ALERTS_ENABLED` gates the whole path). The send is
deliberately off the request path — a mail outage must not fail a sale — so its
failures are logged and swallowed.

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

## Depends on

`contracts`, `shared`.

It must not depend on another business module. If you need something from `order`, put an
interface in `backend/contracts/order/` and let the reactor enforce it — an `import
com.myopty.order.model.*` does not compile here, which is the point.

OpenPDF and Apache POI are imported for the export (`PdfWriter`/`XSSFWorkbook` and their
related types); all usage is in `AvailabilityReportExportService`.

