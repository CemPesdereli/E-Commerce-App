package com.cem.ecommerce.order;

import com.cem.ecommerce.customer.CustomerClient;
import com.cem.ecommerce.exception.BusinessException;
import com.cem.ecommerce.kafka.OrderConfirmation;
import com.cem.ecommerce.kafka.OrderProducer;
import com.cem.ecommerce.orderline.OrderLineRequest;
import com.cem.ecommerce.orderline.OrderLineService;
import com.cem.ecommerce.payment.PaymentClient;
import com.cem.ecommerce.payment.PaymentRequest;
import com.cem.ecommerce.product.ProductClient;
import com.cem.ecommerce.product.PurchaseRequest;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final CustomerClient customerClient;
    private final ProductClient productClient;
    private final PaymentClient paymentClient;

    private final OrderProducer orderProducer;

    private final OrderRepository repository;
    private final OrderMapper mapper;
    private final OrderLineService orderLineService;

    public Integer createOrder(OrderRequest request) {
        // check customer --> customer - microservice (OpenFeign)

        var customer = customerClient.findCustomerById(request.customerId())
                .orElseThrow(() -> new BusinessException("Cannot create order:: No Customer exist with the provided ID"));

        // purchase the products --> product - microservice (RestTemplate)

        var purchasedProducts = productClient.purchaseProducts(request.products());

        // persist order

        var order = repository.save(mapper.toOrder(request));

        // persist order lines

        for (PurchaseRequest purchaseRequest : request.products()) {

            orderLineService.saveOrderLine(
                    new OrderLineRequest(
                            null,
                            order.getId(), // bu olmayabilir, direk order göndermek daha mantıklı
                            purchaseRequest.productId(),
                            purchaseRequest.quantity()
                    )

            );
        }

        // todo start payment process

        var paymentRequest = new PaymentRequest(
                request.amount(),
                request.paymentMethod(),
                order.getId(),
                order.getReference(),
                customer
        );
        paymentClient.requestOrderPayment(paymentRequest);

        //send the order confirmation --> notification - microservice(kafka)
        orderProducer.sendOrderConfirmation(
                new OrderConfirmation(
                        request.reference(),
                        request.amount(),
                        request.paymentMethod(),
                        customer,
                        purchasedProducts
                )
        );

        return order.getId();
    }

    public List<OrderResponse> findAll() {
        return repository.findAll()
                .stream()
                .map(mapper::fromOrder)
                .collect(Collectors.toList());
    }

    public OrderResponse findById(Integer orderId) {
        return repository.findById(orderId)
                .map(mapper::fromOrder)
                .orElseThrow(() -> new EntityNotFoundException(String.format("No order found with the provided ID: %d", orderId)));
    }
}
