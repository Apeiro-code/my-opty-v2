# contracts

Cross-module interfaces, request and response records, and events. Shared by everyone, owned
by the whole team.

## The rule

**No Spring, no entities, no repositories, no dependencies at all.** This is the only module
two business modules are allowed to import, so anything placed here becomes a contract the
whole team has to live with. That is why `contracts` has an empty `<dependencies>` block: the
moment it needs a library to compile, the boundary it exists to protect has been crossed.

Concretely:

- Put the *interface* here and the implementation in the module that owns the data.
- Return records, DTOs or primitives. Never an entity — `com.myopty.catalog.model.Frame` is
  not visible outside `catalog` anyway.
- Interfaces return what the consumer needs, not the provider's whole shape. A `StockQuery`
  with an `isInStock(long id)` method survives a rewrite of the catalog tables. One with a
  `Frame getById(long id)` method does not, and has quietly made the two modules one module.

The feature modules `catalog`, `order`, `workflow` and `billing` are deliberately absent from
this module's dependencies. `contracts/<provider>/` directories exist for the interfaces each
of them will provide.

Written so far:

- `catalog/StockManager` — `reserve(StockReservation)` / `release(StockReservation)`. `order`
  calls it when a progressive order is placed and again when one is rejected, so stock moves
  without `order` reading catalog's tables. The implementation (`catalog`'s `StockService`)
  decrements/restores the shelf and writes a `stock_entry` audit row; `reserve` throws
  `InsufficientStockException` when the shelf cannot cover the order, which `order` maps to a
  `409 OUT_OF_STOCK`. `StockReservation` is a record of ids and a quantity.

- `order/DiscountQuery` — `bestDiscountFor(ItemKind, long)`, returning an
  `Optional<AppliedDiscount>` of the percentage and description. `catalog` calls it when it
  prices an item for the storefront, so a customer sees the discounted figure beside the
  original without catalog reading the `discount` table. The implementation is `order`'s
  `DiscountQueryImpl`, which applies the "item-specific beats sitewide, then the larger
  percentage" rule and returns empty for full price. `AppliedDiscount` carries no price — the
  consumer holds the price and does the rounding.