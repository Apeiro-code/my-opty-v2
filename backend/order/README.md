# Order & Prescription

Prescriptions, progressive orders, discounts and stock intake. Owner: Welikuburawatta M.D.L.

## Tables owned here

| Table | What it holds |
|---|---|
| `prescription` | Optical values, document key, verification status |
| `progressive_order` | An order built against a prescription, linked to a frame and lens under a selected order type |
| `order_notification` | What the shop told a customer, stored as written |
| `discount` | Percentage, validity window, bulk targets |
| `stock_update` | New stock received from a dealer |

All DDL is written by Flyway, from `order/src/main/resources/db/migration`, and nothing else may create or alter them.
The migration version band is **V100-V199** — take the next free number inside it, never one
from another module's band, because four people writing `V3__` on the same afternoon is a
duplicate-version failure that fails the whole build.

## Endpoints owned here

Implemented:
- `POST /api/prescriptions` — the customer submission story: optical values (per eye)
  as a JSON `prescription` part and an optional `document` part, both
  `multipart/form-data`.
- `GET /api/prescriptions` — the caller's own prescriptions, newest first, so the
  order form can offer one to link.
- `POST /api/orders` — the customer links one of their own prescriptions to a lens
  (and, optionally, a frame) under a selected order type. The order type is a JSON
  enum field; the customer id comes from the session and the prescription is loaded
  with that id in the query. A non-owner's prescription answers 404, a `REJECTED`
  one 409, and an unknown frame or lens 400.
- `GET /api/orders` — the caller's own orders, newest first, for the tracking page.
- `GET /api/orders/{id}` — one order, for its owner only.
- `GET /api/notifications` — the caller's notifications, newest first; an optional
  `orderId` narrows them. The customer comes from the session, never a query
  parameter.

The shop owner's review, approval and production endpoints (role-gated, see below):
- `GET /api/shop/prescriptions?status=` — the review queue, oldest first, capped at
  100; `status` defaults to `PENDING_REVIEW`.
- `PUT /api/shop/prescriptions/{id}/verify` — confirm a pending prescription.
- `PUT /api/shop/prescriptions/{id}/reject` — reject it, body `{ "reason": "..." }`.
  This is what "flag missing details" means: the reason is stored and travels back
  to the customer. Reviewing is one-way — only a `PENDING_REVIEW` row may be decided.
- `GET /api/shop/orders?status=` — the approval queue, oldest first, capped at 100;
  `status` defaults to `PENDING`.
- `GET /api/shop/orders/active` — the production line: orders whose status is
  `APPROVED`, `PROCESSING` or `READY`, oldest first.
- `PUT /api/shop/orders/{id}/approve` — send a pending order to production. Refused
  (409) until the linked prescription is `VERIFIED`, and stamps `receive_date` from
  the lab lead time configured for the order type.
- `PUT /api/shop/orders/{id}/reject` — stop it, body `{ "reason": "..." }`.
- `PUT /api/shop/orders/{id}/processing|ready|dispatched` — move an approved order
  forward. Forward-only and skippable; a backwards or repeated move is 409
  (`INVALID_STATE`); `DISPATCHED` and `REJECTED` are terminal.
- `PUT /api/shop/orders/{id}/receive-date` — correct or withdraw the estimate, body
  `{ "receiveDate": "YYYY-MM-DD" }` (or `null`). Does not notify: it is a correction,
  not a status change.
- `GET /api/shop/orders/{id}/notifications` — one order's notification history,
  newest first.

Every status change (approve, reject, processing, ready, dispatched) records a row in
`order_notification` and emails the customer through a `NotificationSender` backed by
`JavaMailSender`. The status change commits before the send, so a failed send marks
the row `FAILED` without rolling back the move; `myopty.notifications.fail-on-error`
(from `MAIL_FAIL_ON_ERROR`) controls whether that failure is also surfaced as `502
NOTIFICATION_FAILED`. The estimated date is `today + myopty.lab.lead-days.<orderType>`
(3/5/7 days for single vision / bifocal / progressive by default).

Customer endpoints take the customer id from the session; no endpoint takes it from the
body or a query parameter. `order_type` arrived in `V107__order_add_order_type.sql`;
values mirror `lens.type` (`SINGLE_VISION`, `BIFOCAL`, `PROGRESSIVE`).

The shop endpoints are mounted under `/api/shop/**` rather than at the originally
planned `/api/prescriptions/{id}/verify` and `/api/orders/{id}/approve`, because the
shared filter chain already restricts `/api/shop/**` to `ROLE_CLIENT` — so the role
rule lives in one place (`SecurityConfig`) instead of being lettered onto each path.
The customer's own `/api/prescriptions` and `/api/orders/{id}` are untouched.

Still planned: `GET /api/prescriptions/{id}`, `GET /api/prescriptions/{id}/document`,
`GET /api/prescriptions?status=`, `GET /api/orders?prescriptionId=`, `/api/discounts`
and `/api/stock/updates`.

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
