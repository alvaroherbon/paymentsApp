import json
import logging
import os

from confluent_kafka import Producer

logger = logging.getLogger(__name__)

_producer = None


def get_producer():
    global _producer
    if _producer is None:
        conf = {
            'bootstrap.servers': os.environ.get('KAFKA_BOOTSTRAP_SERVERS', 'kafka:29092')
        }
        _producer = Producer(conf)
    return _producer


def send_order_created_event(order):
    producer = get_producer()
    topic = 'order-events'

    payload = {
        'order_id': order.id,
        'product_name': order.product_name,
        'quantity': order.quantity,
        'total_price': float(order.total_price),
        'status': order.status
    }

    producer.produce(
        topic,
        key=str(order.id),
        value=json.dumps(payload),
        callback=_delivery_report
    )
    producer.flush()


def _delivery_report(err, msg):
    if err is not None:
        logger.error("Error al enviar mensaje a Kafka: %s", err)
    else:
        logger.info(
            "Mensaje enviado con éxito al tópico %s [partición %s]",
            msg.topic(), msg.partition(),
        )
