# Order & Prescription

Prescriptions, progressive orders, discounts and stock intake. Owner: Welikuburawatta M.D.L.

## Tables owned here

| Table | What it holds |
|---|---|
| `prescription` | Optical values, document key, verification status |
| `progressive_order` | An order built against a prescription |
| `order_notification` | What the shop told a customer, stored as written |
| `discount` | Percentage, validity window, bulk targets |
| `stock_update` | New stock received from a dealer |

All DDL is written by Flyway, from `order/src/main/resources/db/migration`, and nothing else may create or alter them.
The migration version band is **V100-V199** — take the next free number inside it, never one
from another module's band, because four people writing `V3__` on the same afternoon is a
duplicate-version failure that fails the whole build.

## Endpoints owned here

Planned: `/api/prescriptions`, `/api/orders`, `/api/notifications`,
`/api/discounts`, `/api/stock/updates`.

Nothing is implemented yet. This module compiles and its tables exist; the endpoints land
with the features.

## Depends on

`contracts`, `shared`.

It must not depend on another business module. If you need something from `catalog`, put an
interface in `backend/contracts/catalog/` and let the reactor enforce it — an `import
com.myopty.catalog.model.*` does not compile here, which is the point.
