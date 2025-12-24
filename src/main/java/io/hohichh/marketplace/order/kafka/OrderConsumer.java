package io.hohichh.marketplace.order.kafka;


import io.hohichh.marketplace.order.dto.event.PaymentCreatedEvent;
import io.hohichh.marketplace.order.model.PaymentStatus;
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
        Status newOrderStatus;

        if (event.status() == PaymentStatus.SUCCEED) {
            newOrderStatus = Status.PROCESSING;
        } else if (event.status() == PaymentStatus.DECLINED) {
            newOrderStatus = Status.CANCELLED;
            log.warn("Payment declined for order {}. Cancelling order.", event.orderId());
        } else {
            log.info("Ignored payment status {} for order {}", event.status(), event.orderId());
            return;
        }

        try {
            orderService.updateOrderStatusSystem(UUID.fromString(event.orderId()), newOrderStatus);
        } catch (Exception e) {
            log.error("Failed to update order status for orderId: {}", event.orderId(), e);
        }
    }
}