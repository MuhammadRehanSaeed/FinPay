# FinPay

A RESTful financial payment backend built with Spring Boot. Supports user registration, JWT authentication, wallet management, peer-to-peer transfers, transaction history, email notifications, audit logging, and Prometheus metrics.

---

## Tech Stack

- **Java 17**
- **Spring Boot 4.0.8**
- **Spring Security** — JWT-based stateless auth
- **Spring Data JPA** — MySQL via HikariCP
- **jjwt 0.12.6** — JWT generation and validation
- **Bucket4j** — rate limiting
- **Spring Mail** — email notifications (Gmail SMTP)
- **Logstash Logback Encoder** — structured JSON logging with caller line numbers
- **Micrometer + Prometheus** — metrics and monitoring
- **Lombok** — boilerplate reduction

---

## Getting Started

### Prerequisites

- Java 17+
- Maven 3.8+
- MySQL 8+

### Database Setup

Create the database in MySQL:

```sql
CREATE DATABASE FinPay;
```

Hibernate will auto-create/update the tables on startup (`ddl-auto=update`).

### Environment Variables

The app reads secrets from environment variables. Set these before running:

| Variable        | Description                        |
|-----------------|------------------------------------|
| `DB_USERNAME`   | MySQL username                     |
| `DB_PASSWORD`   | MySQL password                     |
| `MAIL_USERNAME` | Gmail address for notifications    |
| `MAIL_PASSWORD` | Gmail app password                 |
| `JWT_SECRET`    | HS256 secret key (min 32 chars)    |

### Run

```bash
mvn spring-boot:run
```

Server starts on `http://localhost:8080`.

---

## API Endpoints

### Auth — `/api/auth`

| Method | Endpoint             | Auth | Description       |
|--------|----------------------|------|-------------------|
| POST   | `/api/auth/register` | No   | Register new user |
| POST   | `/api/auth/login`    | No   | Login, get JWT    |

### Wallet — `/api/v1`

| Method | Endpoint                | Auth | Description          |
|--------|-------------------------|------|----------------------|
| POST   | `/api/v1/wallet`        | Yes  | Fetch your wallet    |
| POST   | `/api/v1/createWallet`  | Yes  | Create a wallet      |
| POST   | `/api/v1/wallet/deposit`| Yes  | Deposit funds        |
| POST   | `/api/v1/wallet/withdraw`| Yes | Withdraw funds       |

### Transactions — `/api/v1/transaction`

| Method | Endpoint                       | Auth | Description                  |
|--------|--------------------------------|------|------------------------------|
| POST   | `/api/v1/transaction/transfer` | Yes  | Transfer to another user     |
| POST   | `/api/v1/transaction`          | Yes  | Full transaction history      |
| POST   | `/api/v1/transaction/{id}`     | Yes  | Single transaction by ID      |

> Transfer requires an `Idempotency-Key` header to prevent duplicate transactions.

### Admin — `/api/admin`

| Method | Endpoint          | Auth  | Role  |
|--------|-------------------|-------|-------|
| POST   | `/api/admin/hello`| Yes   | ADMIN |

---

## Authentication

All protected endpoints require a Bearer token in the `Authorization` header:

```
Authorization: Bearer <your_jwt_token>
```

Tokens expire after **30 minutes**.

---

## Logging

Logs are structured JSON via Logstash encoder. Every entry includes the class, method, and **exact line number** where the log was called:

```json
{
  "timestamp": "2026-10-11T14:57:34.052+05:00",
  "level": "INFO",
  "class": "c.r.f.Service.Impl.IWalletService",
  "method": "fetchWallet",
  "line": 58,
  "message": "Wallet id: 5 found for userId: 7",
  "requestId": "8252ffa3-..."
}
```

Errors log the full stack trace automatically via `GlobalExceptionHandler`.

---

## Monitoring

Actuator endpoints exposed at `/actuator`:

| Endpoint                  | Description          |
|---------------------------|----------------------|
| `/actuator/health`        | App health status    |
| `/actuator/metrics`       | JVM and app metrics  |
| `/actuator/prometheus`    | Prometheus scrape    |

---

## Project Structure

```
src/main/java/com/rehancode/finpay/
├── Controller/        # REST controllers
├── DTO/               # Request / response DTOs
├── Enum/              # Role, Status, TransactionType, etc.
├── Exception/         # Custom exceptions + GlobalExceptionHandler
├── JWT/               # JwtService, JwtFilter
├── Model/             # JPA entities
├── Repository/        # Spring Data repositories
├── SecurityConfig/    # Spring Security setup, UserDetails
├── Service/           # Interfaces + Impl/
└── Util/              # SecurityUtil (authenticated user helper)
```
