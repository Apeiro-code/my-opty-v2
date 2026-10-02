# Docs

Documentation that grows with the system. The root [README.md](../README.md) holds the full project
plan, epics, user stories and diagrams.

| Document | Purpose |
|---|---|
| [SETUP.md](SETUP.md) | Local development environment: pinned tool versions, install steps, preflight check |
| `DEPLOYMENT.md` | Hosting provider, domain, staging → live rollout (planned, Epic 0 story 15) |
| API specs | OpenAPI reference per module; the running instance is at `/swagger-ui.html` |

## Conventions

- One file per concern; link it from this table when added.
- Commands are verified before they are documented. If it was not run, say so.
- The root README stays the single source of truth for epics and diagrams.
