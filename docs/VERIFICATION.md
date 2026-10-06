# Verification report

Verified locally on 26 September 2026 with Java 21.0.7, Node.js 22.14.0, and PostgreSQL 17.10.

## Completed checks

- Compiled all 25 Java application/test source files with Java 21 and the resolved Maven dependencies.
- **11 backend tests passed, zero failures/errors, against a disposable PostgreSQL database.** Coverage includes authentication, BCrypt, invalid JWTs, CRUD, ownership, validation/errors, analytics, history filtering/pagination, dependent deletion, alert lifecycle, older unresolved-alert counts, and UTF-8 password limits.
- HTTP probe tests exercise real local HTTP requests, refused connections, delayed headers, and headers followed by a stalled body. The whole-response deadline cancels the outstanding request.
- The earlier eight-test suite also passed against H2 in PostgreSQL compatibility mode.
- **Four frontend tests passed** for unknown/zero metrics, status badges, pagination boundaries, and error/retry behavior.
- The final frontend production build passed after source organization and chart/error-display refinements.
- Spring Boot started with PostgreSQL and created the application schema.
- Browser walkthrough created a real local demo account and monitor. The scheduler recorded HTTP 200 → HTTP 503 → HTTP 200, with a single failure incident and automatic recovery resolution. History filters returned the corresponding real 503/DOWN rows.

## Environment limitations

The Windows sandbox rejects Java's canonical-path lookup inside Documents. Standard Maven compilation failed on that filesystem operation, rather than a Java source diagnostic. Compilation was therefore verified with the JDK compiler through a temporary in-memory JAR file manager, followed by the normal Maven Surefire test runner. That helper stays outside the deliverable; the project retains its standard Maven configuration for normal local and Docker builds. A complete `mvn package` run was not verified in this sandbox.

Docker Desktop's daemon was unavailable, so container startup was not executed. Dockerfiles and Compose configuration are included. The GitHub Actions workflow is provided but has not been run on GitHub.

The Vite build reports a large bundle advisory because the dashboard includes Recharts; it does not prevent the build. An npm audit during setup reported two moderate development-tool advisories related to Vitest's mock server; no Vitest UI/server is started by this project. Review/update development dependencies before exposing development tooling to a network.

## Screenshots

Screenshots in `screenshots/` show the actual running system and real demo checks, not seeded monitoring data. The local demo service is supplied in `scripts/demo-target.mjs` so results can be reproduced.

## Reproduce

Run `mvn test` in `backend`, then `npm ci`, `npm test`, and `npm run build` in `frontend`. See the README for environment variables and PostgreSQL setup. For database-specific tests, point Maven at a **disposable** PostgreSQL database using the documented system properties. Tests reset that database.
