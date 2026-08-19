import logging

from rest_framework import generics

from .models import Payment
from .serializers import PaymentSerializer

logger = logging.getLogger(__name__)


class PaymentListView(generics.ListAPIView):
    """GET /api/payments/ → lista de pagos"""
    serializer_class = PaymentSerializer
    queryset = Payment.objects.all().order_by('-processed_at')
