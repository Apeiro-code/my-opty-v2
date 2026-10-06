# Builds the runnable API image for the staging preview (backend/compose.prod.yaml).
#
# Two stages: the full JDK builds the 7-module reactor with the Maven wrapper (so the
# image carries whatever Maven version the wrapper pins), the slim JRE runs the one
# repackaged jar. Tests are skipped here — CI runs ./mvnw verify; an image build that
# re-runs the Testcontainers suite would need Docker-in-Docker for no benefit.

FROM eclipse-temurin:25-jdk AS build
WORKDIR /workspace

# The wrapper downloads Maven on first use, and every dependency lands in ~/.m2 —
# both are cached by BuildKit, so only a dependency change pays the full download.
RUN --mount=type=cache,target=/root/.m2 \
    printf 'maven-wrapper: prepared\n'

COPY . .
RUN --mount=type=cache,target=/root/.m2 \
    ./mvnw -B -DskipTests package

FROM eclipse-temurin:25-jre
WORKDIR /app

# The application never runs as root: it authenticates to MySQL with its own
# credentials and has no business owning anything in the container.
RUN useradd --system --uid 10001 --create-home myopty

# myopty-app is the only runnable module; spring-boot-maven-plugin repackages it as
# an executable jar with every dependency nested inside.
COPY --from=build --chown=myopty:myopty /workspace/myopty-app/target/myopty-app-*.jar app.jar

USER myopty
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
