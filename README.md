# PayFlow

B2B Payment Processing Platform — Event-Driven Microservices built with Java 21, Spring Boot, Apache Kafka, and PostgreSQL.

---

## Architecture

```
POST /payment
      │
      ▼
payment-service  →  validates request  →  saves payment (PENDING)  →  Outbox → Kafka
      │
      ▼  (payment-payment.requested.v1)
account-service  →  Inbox (idempotency)  →  validates accounts & balance  →  updates balances  →  Outbox → Kafka
      │
      ├──▶ (account-payment.accepted.v1)
      └──▶ (account-payment.rejected.v1)
                        │
                        ├──▶  notification-service  →  Inbox  →  saves notification record
                        └──▶  payment-service        →  Inbox  →  updates payment status (ACCEPTED / REJECTED)
```

### Services

| Service | Port | DB Port | Description |
|---|---|---|---|
| payment-service | 8081 | 5433 | REST API, Outbox Pattern, Inbox Pattern, JWT Auth |
| account-service | 8082 | 5434 | Async consumer, balance management, Outbox + Inbox |
| notification-service | 8083 | 5435 | Async consumer, Inbox, simulated notification log |

### Kafka Topics

| Topic | Producer | Consumers |
|---|---|---|
| `payment-payment.requested.v1` | payment-service | account-service |
| `account-payment.accepted.v1` | account-service | notification-service, payment-service |
| `account-payment.rejected.v1` | account-service | notification-service, payment-service |

---

## Tech Stack

- **Java 21** / Spring Boot 3
- **Apache Kafka** (KRaft mode — no Zookeeper)
- **PostgreSQL 15** — one isolated DB per service
- **Flyway** — versioned DB migrations
- **Hibernate / JPA** — UUID primary keys, dirty checking
- **Maven multi-module** — shared library across services
- **Docker Compose** — full local environment
- **GitHub Actions** — parallel CI (build-shared → 3 parallel test jobs)
- **Spring Security + JJWT 0.13.0** — JWT-based authentication on payment-service

---

## Patterns Implemented

- **Outbox Pattern** — payment-service and account-service write events to an outbox table atomically with the main transaction; a scheduler polls and publishes to Kafka (at-least-once delivery)
- **Inbox Pattern** — all Kafka consumers persist a `processed_event` record before processing; duplicate events are discarded via unique constraint violation
- **Idempotent consumers** — safe to re-deliver any event without side effects
- **Multi-currency balances** — each account has separate balance rows per currency (`UNIQUE(account_id, currency)`)

---

## Authentication

`payment-service` is protected by JWT-based authentication via Spring Security.

> ⚠️ **This is a basic implementation**, intentionally kept simple for a portfolio project. It covers the core JWT pattern but does not include advanced production features.

### What is implemented

- Stateless JWT authentication with JJWT 0.13.0 (HS256 signing)
- `POST /auth/login` returns a signed JWT token
- All `/payment/**` endpoints require a valid `Authorization: Bearer <token>` header
- `JwtAuthenticationFilter` intercepts every request and validates the token
- Secret managed via environment variable (`JWT_SECRET`)
- Token expiration configurable via `JWT_EXPIRATION_MS` (default: 1 hour)
- Single hardcoded user (`admin` / `password`) — sufficient for demo purposes

### What is NOT implemented

- User management with database (no `UserRepository`, no registration)
- Password hashing (BCrypt)
- Role-based access control (RBAC)
- Refresh tokens
- Token blacklisting / logout
- Per-service authentication (only `payment-service` is protected — `account-service` and `notification-service` are async and have no HTTP endpoints)

### How to obtain a token

```
POST http://localhost:8081/auth/login
Content-Type: application/json

{
  "username": "admin",
  "password": "password"
}
```

Use the returned token in all subsequent requests:

```
Authorization: Bearer <token>
```

---

## Getting Started

### Prerequisites

- Docker Desktop
- Java 21
- Maven

### 1. Start infrastructure

```bash
cd docker
docker compose up -d
```

Starts: Kafka (KRaft), Kafdrop, pgAdmin, and 3 PostgreSQL instances.

### 2. Create Kafka topics

```bash
docker exec kafka-payflow /opt/kafka/bin/kafka-topics.sh \
  --bootstrap-server localhost:9092 --create \
  --topic payment-payment.requested.v1 --partitions 1 --replication-factor 1

docker exec kafka-payflow /opt/kafka/bin/kafka-topics.sh \
  --bootstrap-server localhost:9092 --create \
  --topic account-payment.accepted.v1 --partitions 1 --replication-factor 1

docker exec kafka-payflow /opt/kafka/bin/kafka-topics.sh \
  --bootstrap-server localhost:9092 --create \
  --topic account-payment.rejected.v1 --partitions 1 --replication-factor 1
```

### 3. Start services

Start each service from IntelliJ or via Maven in separate terminals:

```bash
cd services/payment-service    && mvn spring-boot:run
cd services/account-service    && mvn spring-boot:run
cd services/notification-service && mvn spring-boot:run
```

---

## Seed Data — Account Setup

> ⚠️ **account-service does not expose a REST API.** Accounts and balances must be inserted directly into the database before running any payment.

### Connect to the account DB

| Field | Value |
|---|---|
| Host | `localhost` |
| Port | `5434` |
| Database / User / Password | `accountdb` |

Use pgAdmin at http://localhost:5050 (login: `admin@admin.com` / `admin`) or any SQL client.

### Insert accounts

```sql
-- Account A — sender (ACTIVE)
INSERT INTO account (account_id, email, account_status)
VALUES ('a1000000-0000-0000-0000-000000000001', 'alice@payflow.io', 'ACTIVE');

-- Account B — receiver (ACTIVE)
INSERT INTO account (account_id, email, account_status)
VALUES ('b2000000-0000-0000-0000-000000000002', 'bob@payflow.io', 'ACTIVE');

-- Account C — for rejection testing (DISABLED)
INSERT INTO account (account_id, email, account_status)
VALUES ('c3000000-0000-0000-0000-000000000003', 'charlie@payflow.io', 'DISABLED');
```

Valid values for `account_status`: `ACTIVE`, `DISABLED`, `BLOCKED`

### Insert balances

> ⚠️ Both sender **and** receiver must have a balance row for the currency used in the payment. If either is missing, the payment is rejected with `CURRENCY_NOT_SUPPORTED`.

```sql
-- Alice: 10.000 EUR and 5.000 USD
INSERT INTO account_balance (balance_id, account_id, currency, balance)
VALUES
  (gen_random_uuid(), 'a1000000-0000-0000-0000-000000000001', 'EUR', 10000.0000),
  (gen_random_uuid(), 'a1000000-0000-0000-0000-000000000001', 'USD', 5000.0000);

-- Bob: 0 EUR and 0 USD
INSERT INTO account_balance (balance_id, account_id, currency, balance)
VALUES
  (gen_random_uuid(), 'b2000000-0000-0000-0000-000000000002', 'EUR', 0.0000),
  (gen_random_uuid(), 'b2000000-0000-0000-0000-000000000002', 'USD', 0.0000);
```

---

## REST API — payment-service

Base URL: `http://localhost:8081`

> ⚠️ All endpoints require `Authorization: Bearer <token>`. See the Authentication section to obtain a token.

### Create a payment

```
POST /payment
Content-Type: application/json
Authorization: Bearer <token>
```

**Happy path:**
```json
{
  "senderAccountId": "a1000000-0000-0000-0000-000000000001",
  "receiverAccountId": "b2000000-0000-0000-0000-000000000002",
  "amount": 250.00,
  "currency": "EUR",
  "reasonPayment": "Invoice #1042"
}
```

**Rejection — insufficient funds:**
```json
{
  "senderAccountId": "a1000000-0000-0000-0000-000000000001",
  "receiverAccountId": "b2000000-0000-0000-0000-000000000002",
  "amount": 99999.00,
  "currency": "EUR"
}
```

**Rejection — currency not supported (no NOK balance rows):**
```json
{
  "senderAccountId": "a1000000-0000-0000-0000-000000000001",
  "receiverAccountId": "b2000000-0000-0000-0000-000000000002",
  "amount": 100.00,
  "currency": "NOK"
}
```

**Rejection — account not active:**
```json
{
  "senderAccountId": "c3000000-0000-0000-0000-000000000003",
  "receiverAccountId": "b2000000-0000-0000-0000-000000000002",
  "amount": 100.00,
  "currency": "EUR"
}
```

### Get payment by ID

```
GET /payment/{paymentId}
Authorization: Bearer <token>
```

### Get all payments

```
GET /payment/all
Authorization: Bearer <token>
```

---

### Payment statuses

| Status | Meaning |
|---|---|
| `PENDING` | Created, awaiting processing by account-service |
| `ACCEPTED` | Balances updated successfully |
| `REJECTED` | Payment failed |

### Rejection causes

| Cause | Trigger |
|---|---|
| `USER_NOT_FOUND` | Sender or receiver account ID does not exist |
| `USER_NOT_ACTIVE` | Account status is not `ACTIVE` |
| `CURRENCY_NOT_SUPPORTED` | No balance row for the requested currency |
| `NOT_ENOUGH_FUNDS` | Sender balance is less than requested amount |
| `GENERAL_REJECTION` | Unexpected error |

### Supported currencies

`USD` `EUR` `GBP` `CHF` `CAD` `JPY` `AUD` `RON` `CZK` `DKK` `NOK` `PLN` `HKD` `HUF` `INR`

---

## Monitoring

| Tool | URL | Credentials |
|---|---|---|
| Kafdrop | http://localhost:9000 | — |
| pgAdmin | http://localhost:5050 | admin@admin.com / admin |

---

## Running Tests

```bash
# Install shared library first (required by all services)
mvn install -N -DskipTests
mvn install -f shared/pom.xml -DskipTests

# Run unit tests per service
mvn test -f services/payment-service/pom.xml
mvn test -f services/account-service/pom.xml
mvn test -f services/notification-service/pom.xml
```

CI runs automatically on every push to `master` via GitHub Actions — shared library is built first, then all three service test jobs run in parallel.

---

## Project Structure

```
PayFlow/
├── docker/
│   └── docker-compose.yml
├── shared/                           # Shared Maven library
│   └── src/main/java/com/B2B/
│       ├── events/                   # PaymentRequestedV1, PaymentAcceptedV1, PaymentRejectedV1
│       ├── exceptions/               # BaseException, ErrorResponse
│       ├── extra/                    # Currency, StatusPayment, RejectionCause, AccountStatus
│       └── topics/                   # TopicNamesV1
├── services/
│   ├── payment-service/              # Port 8081 — REST + Outbox + Inbox + JWT Auth
│   ├── account-service/              # Port 8082 — Async + Outbox + Inbox
│   └── notification-service/         # Port 8083 — Async + Inbox
└── .github/workflows/ci.yml
```

---

## Not Yet Implemented

- REST API for account management — create and manage accounts and balances via HTTP instead of direct DB inserts
- Role-based access control (RBAC) — ADMIN / USER roles embedded in the JWT token
- `spring.jpa.open-in-view=false` — to be set on all three services
