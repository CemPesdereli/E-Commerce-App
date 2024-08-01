package com.cem.ecommerce.product;

import java.math.BigDecimal;

public record ProductRequest(
        String name,
        String description,
        double availableQuantity,
        BigDecimal price
) {
}
