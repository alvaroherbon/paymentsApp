from django.db import models

# Create your models here.
class Payment(models.Model):
    order_id = models.IntegerField(unique=True)
    amount = models.DecimalField(max_digits=10, decimal_places=2)
    status = models.CharField(max_length=50, default='COMPLETED')
    processed_at = models.DateTimeField(auto_now_add=True)

    def __str__(self):
        return f"Payment for Order #{self.order_id} - {self.status}"