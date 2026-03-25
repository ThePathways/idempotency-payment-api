# idempotency-payment-api

Spring Boot API that demonstrates idempotent payment creation using an `Idempotency-Key` header.

## What It Does

`POST /payments` supports three outcomes:

- first request with a new idempotency key returns `201 Created`
- same key with the same payload returns `200 OK` and replays the original payment
- same key with a different payload returns `409 Conflict`

The conflict response is handled through [`ApiExceptionHandler`](src/main/java/com/thepathways/idempotencypaymentapi/api/exception/ApiExceptionHandler.java).

## Tech Stack

- Java 21
- Spring Boot 4
- Spring Web MVC
- Spring Data JPA
- H2 in-memory database

## Project Structure

```text
src/main/java/com/thepathways/idempotencypaymentapi
├── api
│   ├── PaymentController
│   ├── dto
│   ├── exception
│   └── mapper
├── application
│   ├── CreatePaymentHandler / CreatePaymentHandlerImpl
│   ├── dto
│   ├── exception
│   ├── mapper
│   ├── port
│   └── IdempotencyKeyLockManager
├── domain
└── infrastructure
    └── persistence
```

Flow:

`Controller -> Application Handler -> Ports -> JPA Adapters -> Repositories -> H2`

## Configuration

The app is configured in [`src/main/resources/application.properties`](src/main/resources/application.properties).

Current local setup:

- `server.port=8080`
- H2 URL: `jdbc:h2:mem:idempotencydb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE`
- H2 console: `http://localhost:8080/h2-console`
- schema generation: `create-drop`

## Build And Run

```bash
./mvnw clean package
java -jar target/IdempotencyPaymentAPI-0.0.1-SNAPSHOT.jar
```

Application URL:

```text
http://localhost:8080
```

## API Example

Create a payment:

```bash
curl -i -X POST http://localhost:8080/payments \
  -H 'Content-Type: application/json' \
  -H 'Idempotency-Key: order-001' \
  -d '{"amount":10.50,"currency":"USD","merchantReference":"merchant-order-001"}'
```

Replay the same request:

```bash
curl -i -X POST http://localhost:8080/payments \
  -H 'Content-Type: application/json' \
  -H 'Idempotency-Key: order-001' \
  -d '{"amount":10.50,"currency":"USD","merchantReference":"merchant-order-001"}'
```

Send the same key with a different payload:

```bash
curl -i -X POST http://localhost:8080/payments \
  -H 'Content-Type: application/json' \
  -H 'Idempotency-Key: order-001' \
  -d '{"amount":99.99,"currency":"USD","merchantReference":"merchant-order-001"}'
```

## Example Responses

Created:

```json
{
  "paymentId": 1,
  "amount": 10.50,
  "currency": "USD",
  "merchantReference": "merchant-order-001",
  "paymentStatus": "ACCEPTED",
  "createdAt": "2026-03-22T17:17:58.689054"
}
```

Conflict:

```json
{
  "detail": "The same idempotency key was used with a different request.",
  "instance": "/payments",
  "status": 409,
  "title": "Idempotency conflict"
}
```

## Concurrency

This application is concurrency-safe for a single running instance.

Current behavior:

- requests with different idempotency keys can run concurrently
- requests with the same idempotency key are serialized by [`IdempotencyKeyLockManager`](src/main/java/com/thepathways/idempotencypaymentapi/application/IdempotencyKeyLockManager.java)
- the database transaction runs inside that same lock in [`CreatePaymentHandlerImpl`](src/main/java/com/thepathways/idempotencypaymentapi/application/CreatePaymentHandlerImpl.java)
- the database unique constraint on `idempotency_key` provides an additional guard

Verified behavior on one running instance:

- 5 concurrent requests with the same key and same payload produced `1 x 201` and `4 x 200`
- all 5 responses returned the same `paymentId`
- 2 concurrent requests with the same key and different payloads produced `1 x 201` and `1 x 409`

Current limitation:

- this is not a distributed locking design
- if you run multiple app instances, the in-memory lock is not shared across JVMs

For multi-instance production support, use a shared coordination mechanism such as:

- PostgreSQL/MySQL with transaction and unique-key strategy
- Redis-based distributed locking
- insert-first idempotency record handling in a shared database

## Current Scope

This project is intentionally scoped as a solid single-instance idempotency demo.

- concurrency support is for a single JVM instance only
- multi-instance or distributed deployment is not implemented yet
- H2 is used as a local development database, not as a production database

## Tests

Run tests:

```bash
./mvnw test
```

Current automated coverage:

- application context load test
- controller integration tests for `201`, replay `200`, and conflict `409`
- concurrent same-key test for the application handler
