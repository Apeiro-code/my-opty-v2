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
of them will provide, but no interfaces are written yet — they arrive with the features that
need them.