# GEMINI.md - Project Context

This file provides architectural context and development guidelines for the `quarkus-app` project.

---

## Project Overview

- **Purpose**: Production-ready Quarkus microservice with a focus on high-performance, container-native deployments.
- **Framework**: Quarkus 3.32.2 (Jakarta EE & MicroProfile)
- **Language**: Java 21
- **Key Stack Components**:
    - **REST**: Quarkus REST (JAX-RS) with Jackson and Qute (for templates).
    - **Persistence**: Hibernate ORM with Panache (Repository pattern).
    - **Database**: PostgreSQL (Flyway for migrations).
    - **Security**: MicroProfile JWT (SmallRye) with RSA key verification.
    - **Validation**: Jakarta Bean Validation (Hibernate Validator).
    - **Mapping**: MapStruct for DTO ↔ Entity conversion.
    - **Caching**: Quarkus Cache (Caffeine).
    - **Observability**: Micrometer (Prometheus), OpenTelemetry (Tracing), MicroProfile Health.

---

## Project Structure

- `src/main/java/com/example/`:
    - `config/`: Type-safe configuration classes (using `@ConfigMapping`).
    - `dto/`: Request and Response data transfer objects (implemented as **Java 21 records**).
    - `entity/`: JPA entities extending `PanacheEntity`.
    - `exception/`: Custom exceptions and global `ExceptionMapper` for structured JSON errors.
    - `health/`: Custom liveness and readiness health checks.
    - `mapper/`: MapStruct interfaces for entity/DTO mapping.
    - `repository/`: Data access layer using `PanacheRepository`.
    - `resource/`: REST endpoints (JAX-RS resources).
    - `service/`: Business logic layer (application-scoped beans).
- `src/main/resources/`:
    - `application.properties`: Main configuration.
    - `db/migration/`: Flyway SQL migration scripts.
    - `META-INF/resources/`: Publicly accessible resources, including the `publicKey.pem` for JWT validation.
    - `templates/`: Qute HTML templates for server-side rendering.

---

## Building and Running

### Development Mode (with Hot Reload)
```bash
./mvnw quarkus:dev
```
- Dev UI: `http://localhost:8080/q/dev-ui`
- Swagger UI: `http://localhost:8080/q/swagger-ui`

### Production Build (JVM)
```bash
./mvnw clean package
java -jar target/quarkus-app-runner.jar
```

### Production Build (Native GraalVM)
```bash
./mvnw clean package -Pnative
./target/quarkus-app-runner
```

### Testing
```bash
./mvnw test        # Unit and integration tests
./mvnw verify      # Runs Failsafe integration tests
```

---

## Development Conventions

1.  **Dependency Injection**: Prefer **constructor injection**. Use Lombok's `@RequiredArgsConstructor(onConstructor_ = @Inject)` to reduce boilerplate in services and resources.
2.  **DTOs**: Always use **Java 21 records** for DTOs. Keep them immutable.
3.  **Data Access**: Use the **Repository Pattern** (`PanacheRepository`) rather than the Active Record pattern for better testability and separation of concerns.
4.  **Error Handling**: Do not catch exceptions in Resources. Let them bubble up to the `GlobalExceptionMapper` for consistent JSON error responses (status, error, message, details, timestamp).
5.  **Security**: Use `@RolesAllowed({"ADMIN", "USER", ...})` on Resource methods or classes to enforce JWT-based RBAC.
6.  **Caching**: Use `@CacheResult` for expensive read operations and `@CacheInvalidate` for update/delete operations.
7.  **Soft Deletion**: Use the `status` field (e.g., `ACTIVE`, `INACTIVE`) in entities instead of physical deletion where data retention is required.
8.  **Testing**:
    - **Unit Tests**: Use Mockito for service-level testing.
    - **Integration Tests**: Use **REST-Assured** with `@QuarkusTest` to test REST endpoints against a running application.
    - **AssertJ**: Use AssertJ for fluent and readable assertions.

9. Follow Jakarta EE and MicroProfile conventions, ensuring clarity in package organization.
10. Use the best practices in terms of design patterns whenever applicable.
11. Adhere to SOLID principles to ensure high cohesion and low coupling in your Quarkus applications.
11. Use Quarkus annotations (e.g., @ApplicationScoped, @Inject, @ConfigProperty) effectively.
12. Implement build-time optimizations using Quarkus extensions and best practices.
13. Integrate MicroProfile APIs (e.g., Config, Health, Metrics) for enterprise-grade applications.
14. Use Mutiny where event-driven or reactive patterns are needed (e.g., messaging, streams).
15. Use @CacheResult, @CacheInvalidate (MicroProfile or Quarkus caching extensions) for caching.
16. Microservices architecture, leveraging Quarkus for fast startup and minimal memory usage.


---

## JWT Configuration Note

The application requires a public key for JWT verification.
- **Local Dev**: Use the provided `src/main/resources/META-INF/resources/publicKey.pem`.
- **Production**: The public key location and issuer are configured via:
    - `mp.jwt.verify.publickey.location`
    - `mp.jwt.verify.issuer`
