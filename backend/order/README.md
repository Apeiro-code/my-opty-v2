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

Implemented: `POST /api/prescriptions` — the customer submission story: optical values
(per eye) as a JSON `prescription` part and an optional `document` part, both
`multipart/form-data`. The customer id comes from the session.

Still planned: `GET /api/prescriptions/{id}`, `GET /api/prescriptions/{id}/document`,
`GET /api/prescriptions?status=`, `PUT /api/prescriptions/{id}/verify` and `/reject`,
`/api/orders`, `/api/notifications`, `/api/discounts`, `/api/stock/updates`.

Documents are written through `service/ObjectStore`; the only implementation is
filesystem-backed (`FileSystemObjectStore`, rooted at `myopty.object-storage.root`).
MinIO is still to come and replaces that one bean. The per-eye optical columns arrived
in `V106__order_prescription_per_eye.sql`; V100's shared `cyl`/`axis`/`add_power` were
dropped by it, having never held a row.

## Depends on

`contracts`, `shared` (the session principal `AppUser` and the `ApiError` envelope).

It must not depend on another business module. If you need something from `catalog`, put an
interface in `backend/contracts/catalog/` and let the reactor enforce it — an `import
com.myopty.catalog.model.*` does not compile here, which is the point.
