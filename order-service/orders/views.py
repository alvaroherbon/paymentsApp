import logging

from django.db import transaction
from rest_framework import status
from rest_framework.response import Response
from rest_framework.views import APIView

from .kafka_producer import send_order_created_event
from .models import Order
from .serializers import OrderCreateSerializer, OrderSerializer

logger = logging.getLogger(__name__)


class OrderListCreateView(APIView):
    """
    GET  /api/orders/  → lista de órdenes
    POST /api/orders/  → crear orden y publicar evento Kafka
    """

    def get(self, request):
        orders = Order.objects.all().order_by('-created_at')
        serializer = OrderSerializer(orders, many=True)
        return Response(serializer.data)

    def post(self, request):
        serializer = OrderCreateSerializer(data=request.data)
        if not serializer.is_valid():
            return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)

        data = serializer.validated_data

        try:
            with transaction.atomic():
                order = Order.objects.create(
                    product_name=data['product_name'],
                    quantity=data['quantity'],
                    total_price=data['total_price'],
                    status='CREATED',
                )
                send_order_created_event(order)
        except Exception as e:
            logger.exception("Error al crear pedido o enviar evento a Kafka")
            return Response(
                {'error': 'Error al crear el pedido', 'detail': str(e)},
                status=status.HTTP_500_INTERNAL_SERVER_ERROR,
            )

        logger.info("Pedido #%s creado y evento enviado a Kafka", order.id)
        return Response(
            OrderSerializer(order).data,
            status=status.HTTP_201_CREATED,
        )
