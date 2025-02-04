package com.cem.ecommerce.payment;

import com.cem.ecommerce.customer.CustomerResponse;
import com.cem.ecommerce.order.PaymentMethod;

import java.math.BigDecimal;

public record PaymentRequest(
        BigDecimal amount,
        PaymentMethod paymentMethod,
        Integer orderId,
        String orderReference,
        CustomerResponse customer
) {
}
