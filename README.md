# Quarkus Application

Production-ready Quarkus microservice built with Jakarta EE, MicroProfile, Panache ORM, JWT Security, Caching, and native GraalVM support.

---

## Tech Stack

| Layer | Technology |
|---|---|
| Framework | Quarkus 3.8 |
| Language | Java 21 |
| REST | JAX-RS (Quarkus REST / Reactive Routes) |
| ORM | Hibernate ORM with Panache |
| Database | PostgreSQL (H2 for dev/test) |
| Migrations | Flyway |
| Security | MicroProfile JWT (SmallRye) |
| Mapping | MapStruct |
| Validation | Hibernate Validator / Jakarta Bean Validation |
| Caching | Quarkus Cache (Caffeine) |
| Health | MicroProfile Health |
| Metrics | Micrometer + Prometheus |
| Tracing | OpenTelemetry |
| API Docs | SmallRye OpenAPI / Swagger UI |
| Logging | JBoss Logging + JSON console |
| Testing | JUnit 5 + REST-Assured + Mockito |
| Build | Maven 3.9 |
| Container | Docker (JVM + Native) |

---

## Project Structure

```
src/
├── main/
│   ├── java/com/example/
│   │   ├── config/          # Type-safe config (AppConfig)
│   │   ├── dto/             # Request/Response records (Java 21)
│   │   ├── entity/          # JPA / Panache entities
│   │   ├── exception/       # Custom exceptions + global mapper
│   │   ├── health/          # MicroProfile Health checks
│   │   ├── mapper/          # MapStruct mappers
│   │   ├── repository/      # Panache repositories
│   │   ├── resource/        # JAX-RS REST endpoints
│   │   └── service/         # Business logic layer
│   ├── resources/
│   │   ├── application.properties
│   │   └── db/migration/    # Flyway SQL scripts
│   └── docker/
│       ├── Dockerfile.jvm
│       └── Dockerfile.native
└── test/
    └── java/com/example/
        ├── resource/        # Integration tests (REST-Assured)
        └── service/         # Unit tests (Mockito)
```

---

## Getting Started

### Prerequisites

- Java 21+
- Maven 3.9+
- Docker & Docker Compose
- (Optional) GraalVM 21 CE for native builds

### Run in Dev Mode

```bash
./mvnw quarkus:dev
```

Quarkus Dev Mode features:
- Hot reload on source changes
- Dev UI at http://localhost:8080/q/dev-ui
- Swagger UI at http://localhost:8080/swagger-ui
- Health at http://localhost:8080/health

### Run with Docker Compose (full stack)

```bash
docker-compose up -d
```

This starts PostgreSQL, the app, Prometheus, and Grafana.

---

## Building

### JVM Build

```bash
./mvnw clean package -DskipTests
java -jar target/quarkus-app-runner.jar
```

### Native Build (GraalVM)

```bash
./mvnw clean package -Pnative -DskipTests
./target/quarkus-app-runner
```

### Docker — JVM

```bash
docker build -f src/main/docker/Dockerfile.jvm -t quarkus-app:jvm .
docker run -p 8080:8080 quarkus-app:jvm
```

### Docker — Native

```bash
docker build -f src/main/docker/Dockerfile.native -t quarkus-app:native .
docker run -p 8080:8080 quarkus-app:native
```

---

## API Endpoints

### Users (`/api/v1/users`)

| Method | Path | Role | Description |
|---|---|---|---|
| GET | `/` | ADMIN, MODERATOR | List all users (paginated) |
| GET | `/search?q=` | ADMIN, MODERATOR | Search users |
| GET | `/{id}` | Any auth | Get user by ID |
| POST | `/` | ADMIN | Create user |
| PUT | `/{id}` | ADMIN, USER | Update user |
| PATCH | `/{id}/deactivate` | ADMIN | Deactivate user |
| DELETE | `/{id}` | ADMIN | Delete user |

### Products (`/api/v1/products`)

| Method | Path | Role | Description |
|---|---|---|---|
| GET | `/` | Any auth | List products (paginated) |
| GET | `/search?q=` | Any auth | Search products |
| GET | `/category/{cat}` | Any auth | Filter by category |
| GET | `/price-range?min=&max=` | Any auth | Filter by price range |
| GET | `/low-stock?threshold=` | ADMIN, MODERATOR | Low stock alert |
| GET | `/{id}` | Any auth | Get product by ID |
| POST | `/` | ADMIN, MODERATOR | Create product |
| PUT | `/{id}` | ADMIN, MODERATOR | Update product |
| DELETE | `/{id}` | ADMIN | Delete product |

### System

| Path | Description |
|---|---|
| `/health/live` | Liveness probe |
| `/health/ready` | Readiness probe |
| `/metrics` | Prometheus metrics |
| `/openapi` | OpenAPI spec |
| `/swagger-ui` | Swagger UI (dev only in prod) |

---

## Configuration

### JWT Security & Key Generation

This application uses **MicroProfile JWT (SmallRye)** for security. JWT tokens must be signed with a private key, and the application verifies them using the corresponding public key.

#### Public Key Location

The public key must be placed at:
```
src/main/resources/META-INF/resources/publicKey.pem
```

The application loads it via the configuration property:
```properties
mp.jwt.verify.publickey.location=classpath:META-INF/resources/publicKey.pem
```

#### Generating RSA Keypair

For development and production, generate a secure RSA keypair:

```bash
# Generate 2048-bit private key
openssl genrsa -out private_key.pem 2048

# Extract public key from private key
openssl rsa -in private_key.pem -pubout -out public_key.pem
```

#### Setup Instructions

1. **Generate keypair** (see above)
2. **Copy public key** to `src/main/resources/META-INF/resources/publicKey.pem`
3. **Keep private key secure** (use it in your authentication/token-generation service)
4. **For production**, manage keys via:
   - AWS KMS / Secrets Manager
   - HashiCorp Vault
   - Environment variables or mounted secrets

#### Sample Keys (Development Only)

A sample public key is provided in the repository for local development. **Do not use this in production.**

To generate your own development keys:
```bash
# Quick generation
openssl genrsa 2048 | tee private_key.pem | openssl rsa -pubout > src/main/resources/META-INF/resources/publicKey.pem

# Verify
openssl rsa -in private_key.pem -check
openssl rsa -pubin -in src/main/resources/META-INF/resources/publicKey.pem -check
```

#### JWT Configuration

Key properties in `application.properties`:

```properties
# JWT Public Key Location
mp.jwt.verify.publickey.location=classpath:META-INF/resources/publicKey.pem

# Token Issuer (must match token's iss claim)
mp.jwt.verify.issuer=${JWT_ISSUER:https://example.com/issuer}

# Enable JWT authentication
quarkus.smallrye-jwt.enabled=true
```

Set `JWT_ISSUER` environment variable to match your token-generation service issuer claim.

#### Example JWT Token Generation (with private key)

```bash
# Install jwt-cli (optional)
npm install -g jwt-cli

# Or use jwt.io to sign tokens manually
```

Token should contain:
```json
{
  "iss": "https://example.com/issuer",
  "sub": "user123",
  "aud": "api",
  "exp": 1234567890,
  "iat": 1234567800
}
```

---

### Other Configuration

Key properties in `application.properties`:

```properties
# Database
quarkus.datasource.jdbc.url=${DB_URL:jdbc:postgresql://localhost:5432/appdb}
quarkus.datasource.username=${DB_USERNAME:app_user}
quarkus.datasource.password=${DB_PASSWORD:app_password}

# JWT
mp.jwt.verify.issuer=${JWT_ISSUER:https://example.com/issuer}

# Pagination
app.pagination.default-page-size=20
app.pagination.max-page-size=100
```

Profiles: `dev`, `test`, `prod` — controlled by `QUARKUS_PROFILE` env var.

---

## Testing

```bash
# Unit + Integration tests
./mvnw test

# Integration tests only
./mvnw verify

# Native integration tests
./mvnw verify -Pnative
```

---

## Observability

- **Health**: `/health`, `/health/live`, `/health/ready`
- **Metrics**: `/metrics` (Prometheus format)
- **Tracing**: OpenTelemetry → configurable OTLP endpoint
- **Grafana**: http://localhost:3000 (admin/admin)
- **Prometheus**: http://localhost:9090
