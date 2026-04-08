# PayFlow

PayFlow is a B2B payment platform built with Java 21, Spring Boot, Apache Kafka and PostgreSQL.

It simulates how a real payment system works between businesses: a company sends a payment request, the platform checks if the accounts and funds are valid, and then either accepts or rejects the payment. All parties involved receive a notification at the end.

---

## How it works

When a payment is created, the platform goes through three steps automatically:

**1. Payment Request**
The sender submits a payment (amount, currency, sender and receiver account). The platform saves it with status `PENDING` and puts it in a queue.

**2. Accept or Reject**
The system checks:
- Do both accounts exist?
- Are both accounts active?
- Does the sender have enough balance in the requested currency?
- Does the receiver support that currency?

If all checks pass → the balances are updated and the payment becomes `ACCEPTED`.
If any check fails → the payment becomes `REJECTED` with a reason (e.g. `NOT_ENOUGH_FUNDS`).

**3. Notification**
Once the payment is accepted or rejected, a notification record is saved for the sender.

---

## Architecture

The platform is made of 5 independent services:

| Service | Port | Description |
|---|---|---|
| gateway | 8080 | Single entry point — handles login tokens and access control |
| auth-service | 8084 | Issues login tokens |
| payment-service | 8081 | Accepts payment requests, tracks payment status |
| account-service | 8082 | Manages accounts and balances, processes payments |
| notification-service | 8083 | Records notifications for accepted/rejected payments |

All requests go through the **gateway on port 8080**. You never call the other services directly.

The services communicate with each other asynchronously using **Apache Kafka** — when a payment is created, an event is published on Kafka, the account-service picks it up, processes it, and publishes the result back. This keeps the services fully decoupled.

---

## Authentication

PayFlow uses JWT tokens. There are two hardcoded users for demo purposes:

| Username | Password | Role |
|---|---|---|
| admin | password | ADMIN — full access |
| user | password | USER — can create and view own payments |

> This is intentionally simple for a portfolio project. No user database, no registration.

### Get a token

```
POST http://localhost:8080/auth/login
Content-Type: application/json

{
  "username": "admin",
  "password": "password"
}
```

Use the token in all subsequent requests:

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

This starts Kafka, Kafdrop, pgAdmin and 3 PostgreSQL databases.

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

```bash
cd services/auth-service         && mvn spring-boot:run
cd services/gateway              && mvn spring-boot:run
cd services/payment-service      && mvn spring-boot:run
cd services/account-service      && mvn spring-boot:run
cd services/notification-service && mvn spring-boot:run
```

---

## Try it out

### Step 1 — Get a token

```
POST http://localhost:8080/auth/login

{ "username": "admin", "password": "password" }
```

### Step 2 — Create two accounts

```
POST http://localhost:8080/account

{ "email": "alice@payflow.io" }
{ "email": "bob@payflow.io" }
```

### Step 3 — Add balance to both accounts

Both sender and receiver need a balance row for the payment currency.

```
POST http://localhost:8080/account/{aliceId}/balance

{ "currency": "EUR", "amount": 10000 }
```

```
POST http://localhost:8080/account/{bobId}/balance

{ "currency": "EUR", "amount": 0 }
```

### Step 4 — Create a payment

```
POST http://localhost:8080/payment

{
  "senderAccountId": "<aliceId>",
  "receiverAccountId": "<bobId>",
  "amount": 250.00,
  "currency": "EUR",
  "reasonPayment": "Invoice #1042"
}
```

### Step 5 — Check the result (wait 2-3 seconds)

```
GET http://localhost:8080/payment/{paymentId}
```

The status will be `ACCEPTED` or `REJECTED`. If rejected, the response includes the reason.

---

## Rejection causes

| Cause | Meaning |
|---|---|
| `USER_NOT_FOUND` | Account ID does not exist |
| `USER_NOT_ACTIVE` | Account is disabled or blocked |
| `CURRENCY_NOT_SUPPORTED` | No balance row for that currency |
| `NOT_ENOUGH_FUNDS` | Sender does not have enough balance |
| `GENERAL_REJECTION` | Unexpected error |

## Supported currencies

`USD` `EUR` `GBP` `CHF` `CAD` `JPY` `AUD` `RON` `CZK` `DKK` `NOK` `PLN` `HKD` `HUF` `INR`

---

## Monitoring

| Tool | URL | Credentials |
|---|---|---|
| Kafdrop (Kafka UI) | http://localhost:9000 | — |
| pgAdmin | http://localhost:5050 | admin@admin.com / admin |

---

## Running Tests

```bash
mvn install -N -DskipTests
mvn install -f shared/pom.xml -DskipTests

mvn test -f services/payment-service/pom.xml
mvn test -f services/account-service/pom.xml
mvn test -f services/notification-service/pom.xml
mvn test -f services/auth-service/pom.xml
```

---

## Project Structure

```
PayFlow/
├── docker/
│   └── docker-compose.yml
├── shared/                     # Shared library (events, exceptions, enums)
├── services/
│   ├── auth-service/           # port 8084
│   ├── gateway/                # port 8080
│   ├── payment-service/        # port 8081
│   ├── account-service/        # port 8082
│   └── notification-service/   # port 8083
└── .github/workflows/ci.yml
```
