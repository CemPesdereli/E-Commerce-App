package com.cem.ecommerce.order;

import com.cem.ecommerce.customer.CustomerClient;
import com.cem.ecommerce.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final CustomerClient customerClient;
    private final OrderRepository repository;
    private final OrderMapper mapper;

    public Integer createOrder(OrderRequest request) {
        var customer = customerClient.findCustomerById(request.customerId())
                .orElseThrow(()-> new BusinessException("Cannot create order:: No Customer exist with the provided ID"));

        // purchase the products --> product - microservice

        // persist order

        // persist order lines

        // start payment process

        //send the order confirmation --> notification - microservice(kafka)
        return null;
    }
}
