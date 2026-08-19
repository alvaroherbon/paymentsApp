# AGENTS.md

## Architecture

Two independent Django services communicating via Kafka, sharing one PostgreSQL database (`ecommerce_db`).

- **order-service** (`:8000`): REST API (DRF) for creating and listing orders. Produces `order-events` to Kafka topic. Has `/health/` endpoint.
- **payment-service** (`:8001`): REST API (DRF) for listing payments. Has a `consume_orders` management command that listens to `order-events` and creates `Payment` records. Has `/health/` endpoint.

Event flow: `POST /api/orders/` → order saved → Kafka `order-events` topic → `consume_orders` creates payment.

## Running

```bash
# Full stack (Postgres, Kafka KRaft, both services, pgAdmin, kafka-ui)
docker compose up --build

# Per-service Django management commands (inside container or locally if DB is reachable)
docker exec -it order_app python manage.py <cmd>
docker exec -it payment_app python manage.py <cmd>

# Kafka consumer (payment-service) — runs as a long-lived process, not a server
docker exec -it payment_app python manage.py consume_orders
```

## Dev commands (local, outside Docker)

Each service is a standalone Django project. From `order-service/` or `payment-service/`:

```bash
pip install -r requirements.txt
export POSTGRES_HOST=localhost KAFKA_BOOTSTRAP_SERVERS=localhost:9092
python manage.py runserver 0.0.0.0:<port>
python manage.py migrate
python manage.py test
```

**Order service expects port 8000**, payment service expects port 8001. Both services use `runserver` in the Dockerfile (no production WSGI server configured).

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

## Codebase layout

```
order-service/
  order_project/     # Django settings, urls, wsgi/asgi
  orders/            # Order model, views (DRF), serializers, kafka_producer
  requirements.txt   # django, djangorestframework, psycopg2-binary, confluent-kafka

payment-service/
  payment_project/   # Django settings, urls, wsgi/asgi
  payments/          # Payment model, views (DRF), serializers, consume_orders command
  requirements.txt   # identical to order-service
```

## Shared infra (docker-compose.yml)

- PostgreSQL 15 on `:5432` — single shared DB, both services read/write
- Kafka 7.4.0 KRaft mode on `:9092` (host) / `:29092` (internal)
- pgAdmin on `:5050`, kafka-ui on `:8080`
- Both Django services have health checks on `/health/`
- `payment-service` depends on `order-service` being healthy before starting

## Gotchas

- Both services share the same Postgres DB and credentials. Migrations must not conflict.
- `Payment.order_id` is a plain `IntegerField(unique=True)` — it references `Order.id` by convention, not by FK.
- `consume_orders` deduplicates by checking `Payment.objects.filter(order_id=...)` before creating, then commits offset manually (`enable.auto.commit=False`).
- `kafka_producer.py` uses a singleton `Producer` — don't create new instances per request.
- `create_order` wraps order creation + Kafka publish in `transaction.atomic()` — if Kafka fails, the order is rolled back.
- Consumer has retry logic with max 5 retries and reconnects automatically on Kafka errors.
- Consumer validates message payload — malformed or incomplete messages are skipped and committed.
- No linting, typecheck, or formatter configured.
