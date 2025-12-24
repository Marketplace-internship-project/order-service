package io.hohichh.marketplace.order.kafka;


import io.hohichh.marketplace.order.dto.NewStatusOrderDto;
import io.hohichh.marketplace.order.dto.event.PaymentCreatedEvent;
import io.hohichh.marketplace.order.model.order.Status;
import io.hohichh.marketplace.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderConsumer {
    private final OrderService orderService;

    @KafkaListener(topics = "payment-events", groupId = "order-service-group")
    public void handlePaymentCreated(PaymentCreatedEvent event) {
        log.info("Received OrderCreatedEvent for orderId: {}", event.orderId());
        orderService.updateOrderStatus(UUID.fromString(event.orderId()),
                new NewStatusOrderDto(
                        Status.PROCESSING
                ));
    }
}