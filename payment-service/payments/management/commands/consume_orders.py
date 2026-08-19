import json
import logging
import os
import time

from django.core.management.base import BaseCommand
from confluent_kafka import Consumer, KafkaError
from payments.models import Payment

logger = logging.getLogger(__name__)

MAX_RETRIES = 5
RETRY_DELAY = 5


class Command(BaseCommand):
    help = 'Inicia el consumidor de Kafka para procesar pagos de nuevos pedidos'

    def handle(self, *args, **options):
        conf = {
            'bootstrap.servers': os.environ.get('KAFKA_BOOTSTRAP_SERVERS', 'kafka:29092'),
            'group.id': 'payment-service-group',
            'auto.offset.reset': 'earliest',
            'enable.auto.commit': False,
        }

        consumer = self._create_consumer(conf)
        retries = 0

        logger.info("Consumidor conectado a Kafka, escuchando 'order-events'...")

        try:
            while True:
                msg = consumer.poll(1.0)
                if msg is None:
                    continue

                if msg.error():
                    if msg.error().code() == KafkaError._PARTITION_EOF:
                        continue

                    retries += 1
                    logger.error(
                        "Error de Kafka: %s (intento %d/%d)",
                        msg.error(), retries, MAX_RETRIES,
                    )
                    if retries >= MAX_RETRIES:
                        logger.critical("Máximo de reintentos alcanzado. Terminando consumidor.")
                        break
                    consumer.close()
                    time.sleep(RETRY_DELAY)
                    consumer = self._create_consumer(conf)
                    continue

                retries = 0

                try:
                    data = json.loads(msg.value().decode('utf-8'))
                except (json.JSONDecodeError, UnicodeDecodeError) as e:
                    logger.error("Mensaje malformado: %s — saltando offset", e)
                    consumer.commit(asynchronous=False)
                    continue

                order_id = data.get('order_id')
                amount = data.get('total_price')

                if order_id is None or amount is None:
                    logger.warning(
                        "Mensaje sin campos requeridos (order_id=%s, total_price=%s) — saltando",
                        order_id, amount,
                    )
                    consumer.commit(asynchronous=False)
                    continue

                if not Payment.objects.filter(order_id=order_id).exists():
                    Payment.objects.create(
                        order_id=order_id,
                        amount=amount,
                        status='COMPLETED',
                    )
                    logger.info("Pago registrado correctamente para el Pedido #%s", order_id)
                else:
                    logger.warning("El pago para el Pedido #%s ya había sido procesado.", order_id)

                consumer.commit(asynchronous=False)

        except KeyboardInterrupt:
            logger.info("Interrupción por teclado. Cerrando consumidor...")
        finally:
            consumer.close()

    def _create_consumer(self, conf):
        return Consumer(conf)
