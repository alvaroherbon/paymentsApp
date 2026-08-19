from django.contrib import admin
from .models import Payment


@admin.register(Payment)
class PaymentAdmin(admin.ModelAdmin):
    list_display = ('id', 'order_id', 'amount', 'status', 'processed_at')
    list_filter = ('status',)
    readonly_fields = ('processed_at',)
