import json
import os
from django.core.management.base import BaseCommand
from confluent_kafka import Consumer, KafkaError
from payments.models import Payment

class Command(BaseCommand):
    help = 'Inicia el consumidor de Kafka para procesar pagos de nuevos pedidos'

    def handle(self, *args, **options):
        conf = {
            'bootstrap.servers': os.environ.get('KAFKA_BOOTSTRAP_SERVERS', 'kafka:29092'),
            'group.id': 'payment-service-group',
            'auto.offset.reset': 'earliest'
        }

        consumer = Consumer(conf)
        consumer.subscribe(['order-events'])

        self.stdout.write(self.style.SUCCESS("🚀 Consumidor de Django conectado a Kafka, escuchando 'order-events'..."))

        try:
            while True:
                msg = consumer.poll(1.0)
                if msg is None:
                    continue
                if msg.error():
                    if msg.error().code() == KafkaError._PARTITION_EOF:
                        continue
                    else:
                        print(f"Error de Kafka: {msg.error()}")
                        break

                # Procesar datos recibidos
                data = json.loads(msg.value().decode('utf-8'))
                order_id = data.get('order_id')
                amount = data.get('total_price')

                # Evitar duplicados comprobando si ya existe el pago (equivalente a un findById / exists)
                if not Payment.objects.filter(order_id=order_id).exists():
                    Payment.objects.create(
                        order_id=order_id,
                        amount=amount,
                        status='SUCCESS'
                    )
                    self.stdout.write(self.style.SUCCESS(f"💰 Pago registrado correctamente para el Pedido #{order_id}"))
                else:
                    self.stdout.write(self.style.WARNING(f"⚠️ El pago para el Pedido #{order_id} ya había sido procesado."))

        except KeyboardInterrupt:
            pass
        finally:
            consumer.close()