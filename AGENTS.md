# AGENTS.md

## Architecture

Three services communicating via Kafka, sharing one PostgreSQL database (`ecommerce_db`).

- **order-service** (`:8000`, Django): REST API (DRF) for creating and listing orders. Produces `order-events` to Kafka topic. Has `/health/` endpoint.
- **payment-service** (`:8001`, Django): REST API (DRF) for listing payments. No longer runs the Kafka consumer. Has `/health/` endpoint.
- **payment-service-spring** (`:8002`, Spring Boot): Kafka consumer that listens to `order-events` and creates `Payment` records. Uses Spring Data JPA + Spring Kafka.

Event flow: `POST /api/orders/` → order saved → Kafka `order-events` topic → Spring Boot consumer creates payment in PostgreSQL.

## Running

```bash
# Full stack (Postgres, Kafka KRaft, all services, pgAdmin, kafka-ui)
docker compose up --build

# Per-service Django management commands (inside container or locally if DB is reachable)
docker exec -it order_app python manage.py <cmd>
docker exec -it payment_app python manage.py <cmd>
```

## Dev commands (local, outside Docker)

### Django services (order-service, payment-service)

From `order-service/` or `payment-service/`:

```bash
pip install -r requirements.txt
export POSTGRES_HOST=localhost KAFKA_BOOTSTRAP_SERVERS=localhost:9092
python manage.py runserver 0.0.0.0:<port>
python manage.py migrate
python manage.py test
```

**Order service expects port 8000**, payment service expects port 8001.

### Spring Boot service (payment-service-spring)

```bash
# Requires Java 17+ and Maven
cd payment-service-spring
export POSTGRES_HOST=localhost KAFKA_BOOTSTRAP_SERVERS=localhost:9092
mvn spring-boot:run
```

Runs on port 8002.

## API Endpoints

### order-service (`:8000`)
| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/orders/` | Create order + publish Kafka event |
| `GET`  | `/api/orders/` | List all orders |
| `GET`  | `/health/` | Health check |

### payment-service (`:8001`)
| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET`  | `/api/payments/` | List all payments |
| `GET`  | `/health/` | Health check |

### payment-service-spring (`:8002`)
| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET`  | `/actuator/health` | Health check |

## Codebase layout

```
order-service/                # Django — Order API + Kafka producer
  order_project/              # Django settings, urls, wsgi/asgi
  orders/                     # Order model, views (DRF), serializers, kafka_producer
  requirements.txt

payment-service/              # Django — Payment API (read-only)
  payment_project/            # Django settings, urls, wsgi/asgi
  payments/                   # Payment model, views (DRF), serializers
  requirements.txt

payment-service-spring/       # Spring Boot — Kafka consumer
  pom.xml                     # Maven: spring-boot-starter-data-jpa, spring-kafka, postgresql
  src/main/java/.../
    PaymentApplication.java   # Entry point
    model/Payment.java        # JPA entity (maps to payments_payment table)
    repository/               # Spring Data JPA repository
    kafka/OrderEventConsumer.java  # @KafkaListener on "order-events"
    config/KafkaConfig.java   # @EnableKafka
  src/main/resources/
    application.yml           # DB, Kafka, JPA config
  Dockerfile                  # Multi-stage Maven build
```

## Shared infra (docker-compose.yml)

- PostgreSQL 15 on `:5432` — single shared DB, all services read/write
- Kafka 7.4.0 KRaft mode on `:9092` (host) / `:29092` (internal)
- pgAdmin on `:5050`, kafka-ui on `:8080`
- Django services have health checks on `/health/`
- Spring Boot service has health check on `/actuator/health`

## Gotchas

- All services share the same Postgres DB and credentials. Migrations must not conflict.
- `Payment.order_id` is a plain `IntegerField(unique=True)` — it references `Order.id` by convention, not by FK.
- The Spring Boot consumer uses the same `payments_payment` table as the Django Payment model. The JPA entity maps to this table explicitly.
- Spring Boot consumer deduplicates via `existsByOrderId()` before creating, with manual offset commit (`enable.auto.commit=false`).
- `kafka_producer.py` uses a singleton `Producer` — don't create new instances per request.
- `create_order` wraps order creation + Kafka publish in `transaction.atomic()` — if Kafka fails, the order is rolled back.
- The Django `consume_orders` management command still exists but is no longer started by docker-compose. Use the Spring Boot consumer instead.
- No linting, typecheck, or formatter configured.
