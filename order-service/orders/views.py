from django.shortcuts import render

# Create your views here.
from django.http import JsonResponse
from django.views.decorators.csrf import csrf_exempt
import json
from .models import Order
from .kafka_producer import send_order_created_event

@csrf_exempt
def create_order(request):
    if request.method == 'POST':
        data = json.loads(request.body)
        
        # Equivalente a guardar una entidad con JPA
        order = Order.objects.create(
            product_name=data.get('product_name'),
            quantity=data.get('quantity'),
            total_price=data.get('total_price'),
            status='CREATED'
        )

        # Disparar el evento de Kafka
        send_order_created_event(order)

        return JsonResponse({
            'message': 'Pedido creado y evento enviado a Kafka con éxito',
            'order_id': order.id
        }, status=201)
        
    return JsonResponse({'error': 'Método no permitido'}, status=405)