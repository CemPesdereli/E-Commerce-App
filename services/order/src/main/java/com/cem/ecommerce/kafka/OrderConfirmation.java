package com.cem.ecommerce.kafka;

import com.cem.ecommerce.customer.CustomerResponse;
import com.cem.ecommerce.order.PaymentMethod;
import com.cem.ecommerce.product.PurchaseResponse;

import java.math.BigDecimal;
import java.util.List;

public record OrderConfirmation(
        String orderReference,
        BigDecimal totalAmount,
        PaymentMethod paymentMethod,
        CustomerResponse customer,
        List<PurchaseResponse> products
) {
}
