package com.cem.ecommerce.order;

import com.cem.ecommerce.customer.CustomerClient;
import com.cem.ecommerce.exception.BusinessException;
import com.cem.ecommerce.orderline.OrderLineRequest;
import com.cem.ecommerce.orderline.OrderLineService;
import com.cem.ecommerce.product.ProductClient;
import com.cem.ecommerce.product.PurchaseRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final CustomerClient customerClient;
    private final ProductClient productClient;

    private final OrderRepository repository;
    private final OrderMapper mapper;
    private final OrderLineService orderLineService;

    public Integer createOrder(OrderRequest request) {
        // check customer --> customer - microservice (OpenFeign)

        var customer = customerClient.findCustomerById(request.customerId())
                .orElseThrow(()-> new BusinessException("Cannot create order:: No Customer exist with the provided ID"));

        // purchase the products --> product - microservice (RestTemplate)

        productClient.purchaseProducts(request.products());

        // persist order

        var order = repository.save(mapper.toOrder(request));

        // persist order lines

        for(PurchaseRequest purchaseRequest: request.products()){

            orderLineService.saveOrderLine(
                    new OrderLineRequest(
                            null,
                            order.getId(),
                            purchaseRequest.productId(),
                            purchaseRequest.quantity()
                    )

            );
        }

        // todo start payment process

        //send the order confirmation --> notification - microservice(kafka)
        return null;
    }
}
