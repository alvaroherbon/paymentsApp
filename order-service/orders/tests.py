import json
from unittest.mock import patch

from django.test import TestCase
from rest_framework.test import APIClient

from .models import Order


class OrderCreateTest(TestCase):
    def setUp(self):
        self.client = APIClient()
        self.url = '/api/orders/'

    @patch('orders.views.send_order_created_event')
    def test_create_order_success(self, mock_kafka):
        payload = {
            'product_name': 'Laptop',
            'quantity': 1,
            'total_price': 999.99,
        }
        response = self.client.post(
            self.url,
            data=json.dumps(payload),
            content_type='application/json',
        )
        self.assertEqual(response.status_code, 201)
        self.assertEqual(response.data['product_name'], 'Laptop')
        self.assertEqual(response.data['status'], 'CREATED')
        self.assertIn('id', response.data)
        self.assertTrue(mock_kafka.called)

    def test_create_order_missing_fields(self):
        response = self.client.post(
            self.url,
            data=json.dumps({}),
            content_type='application/json',
        )
        self.assertEqual(response.status_code, 400)
        self.assertIn('product_name', response.data)
        self.assertIn('quantity', response.data)
        self.assertIn('total_price', response.data)

    def test_create_order_invalid_quantity(self):
        payload = {
            'product_name': 'Laptop',
            'quantity': -1,
            'total_price': 999.99,
        }
        response = self.client.post(
            self.url,
            data=json.dumps(payload),
            content_type='application/json',
        )
        self.assertEqual(response.status_code, 400)
        self.assertIn('quantity', response.data)

    def test_create_order_invalid_json(self):
        response = self.client.post(
            self.url,
            data='not json',
            content_type='application/json',
        )
        self.assertEqual(response.status_code, 400)

    def test_get_orders_list(self):
        Order.objects.create(
            product_name='Laptop', quantity=1, total_price=999.99, status='CREATED'
        )
        response = self.client.get(self.url)
        self.assertEqual(response.status_code, 200)
        self.assertEqual(len(response.data), 1)
        self.assertEqual(response.data[0]['product_name'], 'Laptop')

    def test_get_orders_empty(self):
        response = self.client.get(self.url)
        self.assertEqual(response.status_code, 200)
        self.assertEqual(len(response.data), 0)


class OrderModelTest(TestCase):
    def test_order_str(self):
        order = Order.objects.create(
            product_name='Laptop', quantity=1, total_price=999.99
        )
        self.assertEqual(str(order), f'Order #{order.id} - Laptop')

    def test_order_default_status(self):
        order = Order.objects.create(
            product_name='Laptop', quantity=1, total_price=999.99
        )
        self.assertEqual(order.status, 'CREATED')
