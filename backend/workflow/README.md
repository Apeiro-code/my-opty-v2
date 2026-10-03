# Workflow & Communication

Task board, monthly reports and dealer communication. Owner: Karunarathna KHMMM.

## Tables owned here

| Table | What it holds |
|---|---|
| `todo_task` | Kanban tasks, optionally linked to an order |
| `monthly_report` | Sales and stock figures per month |
| `dealer` | Frame and lens dealer contacts |
| `dealer_email` | Composed and auto-generated dealer emails |

All DDL is written by Flyway, from `workflow/src/main/resources/db/migration`, and nothing else may create or alter them.
The migration version band is **V200-V299** — take the next free number inside it, never one
from another module's band, because four people writing `V3__` on the same afternoon is a
duplicate-version failure that fails the whole build.

## Endpoints owned here

Planned: `/api/tasks`, `/api/reports/monthly`, `/api/dashboard`,
`/api/dealers`, `/api/dealer-emails`.

Nothing is implemented yet. This module compiles and its tables exist; the endpoints land
with the features.

## Depends on

`contracts`, `shared`.

It must not depend on another business module. If you need something from `order`, put an
interface in `backend/contracts/order/` and let the reactor enforce it — an `import
com.myopty.order.model.*` does not compile here, which is the point.
