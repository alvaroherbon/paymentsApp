from django.test import TestCase

from .models import Payment


class PaymentModelTest(TestCase):
    def setUp(self):
        self.payment = Payment.objects.create(
            order_id=1, amount=999.99, status='COMPLETED'
        )

    def test_payment_str(self):
        self.assertEqual(
            str(self.payment),
            f'Payment for Order #1 - COMPLETED',
        )

    def test_payment_unique_order_id(self):
        from django.db import IntegrityError
        with self.assertRaises(IntegrityError):
            Payment.objects.create(
                order_id=1, amount=500.00, status='COMPLETED'
            )

    def test_payment_default_status(self):
        payment = Payment.objects.create(order_id=2, amount=100.00)
        self.assertEqual(payment.status, 'COMPLETED')
