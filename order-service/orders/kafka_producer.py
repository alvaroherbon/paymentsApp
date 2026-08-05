import json
import os
from confluent_kafka import Producer

def get_producer():
    conf = {
        'bootstrap.servers': os.environ.get('KAFKA_BOOTSTRAP_SERVERS', 'kafka:29092')
    }
    return Producer(conf)

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
    
    # Serializamos a JSON y enviamos el mensaje
    producer.produce(
        topic,
        key=str(order.id),
        value=json.dumps(payload),
        callback=delivery_report
    )
    producer.flush()

def delivery_report(err, msg):
    if err is not None:
        print(f"❌ Error al enviar mensaje a Kafka: {err}")
    else:
        print(f"✅ Mensaje enviado con éxito al tópico {msg.topic()} [partición {msg.partition()}]")