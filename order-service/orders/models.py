from django.db import models

# Create your models here.
class Order(models.Model):
    product_name = models.CharField(max_length=150)
    quantity = models.IntegerField()
    total_price = models.DecimalField(max_digits=10, decimal_places=2)
    status = models.CharField(max_length=50, default='CREATED')
    created_at = models.DateTimeField(auto_now_add=True)

    def __str__(self):
        return f"Order #{self.id} - {self.product_name}"