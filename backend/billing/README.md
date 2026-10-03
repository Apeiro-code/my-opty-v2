# Payment & Billing

Payments, invoices and billing reports. Owner: Kankanamge P M G.

## Tables owned here

| Table | What it holds |
|---|---|
| `payment_method` | Accepted methods and their provider config |
| `payment` | A payment attempt against an order |
| `invoice` | What was charged, stored not recomputed |
| `billing_report` | Monthly revenue, refunds, net |

All DDL is written by Flyway, from `billing/src/main/resources/db/migration`, and nothing else may create or alter them.
The migration version band is **V300-V399** — take the next free number inside it, never one
from another module's band, because four people writing `V3__` on the same afternoon is a
duplicate-version failure that fails the whole build.

## Endpoints owned here

Planned: `/api/payment-methods`, `/api/payments`, `/api/invoices`,
`/api/billing/reports`.

Nothing is implemented yet. This module compiles and its tables exist; the endpoints land
with the features.

## Depends on

`contracts`, `shared`.

It must not depend on another business module. If you need something from `order`, put an
interface in `backend/contracts/order/` and let the reactor enforce it — an `import
com.myopty.order.model.*` does not compile here, which is the point.
