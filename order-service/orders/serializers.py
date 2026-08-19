from rest_framework import serializers
from .models import Order


class OrderSerializer(serializers.ModelSerializer):
    class Meta:
        model = Order
        fields = ['id', 'product_name', 'quantity', 'total_price', 'status', 'created_at']
        read_only_fields = ['id', 'status', 'created_at']


class OrderCreateSerializer(serializers.Serializer):
    product_name = serializers.CharField(max_length=150, trim_whitespace=True)
    quantity = serializers.IntegerField(min_value=1)
    total_price = serializers.DecimalField(max_digits=10, decimal_places=2, min_value=0.01)

    def validate_product_name(self, value):
        if not value.strip():
            raise serializers.ValidationError("No puede estar vacío.")
        return value.strip()
