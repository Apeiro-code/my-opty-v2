# myopty-app

The runnable application. The only module with a `main` method, and the only one that produces
an executable jar.

It owns **no domain**. There are no entities, no repositories and no endpoints here. It exists
to assemble the six library modules into one process and to carry the configuration that
applies to the application as a whole rather than to any one feature:

| File | Why it is here |
|---|---|
| `MyOptyApplication.java` | `@SpringBootApplication` with `scanBasePackages = "com.myopty"`, because this class sits beside the module packages rather than above them |
| `application.yml` | Datasource, Flyway, Swagger, Actuator, CORS origins, log level |
| `SchemaMigrationTest.java` | Runs the real migrations against a throwaway MySQL 8 container |

## Running it

```bash
cd backend
docker compose up -d          # MySQL on :3307, Mailpit on :8025
./mvnw spring-boot:run        # API on :8080, Swagger UI at /swagger-ui.html
```

`./mvnw spring-boot:run` works from this directory because the parent's POM sets
`spring-boot-maven-plugin` to `skip` and this module turns it back on. Without that, the goal
would try to start the reactor itself and fail with "Unable to find a suitable main class".

## Where the configuration comes from

`application.yml` reads `${DB_URL}`, `${DB_USERNAME}` and the rest from the environment, and
picks them up through:

```yaml
spring:
  config:
    import: "optional:file:.env[.properties],optional:file:../.env[.properties]"
```

The root `.env` is `KEY=VALUE`, which is also valid properties syntax, so the `[.properties]`
hint lets Spring read it directly — no dotenv dependency. Both paths are `optional`, so a
build with no `.env` (CI) starts rather than failing on a missing placeholder. There is also a
`backend/.env` symlink to the root file, which is what makes Docker Compose read the same
values; see `docs/SETUP.md`.