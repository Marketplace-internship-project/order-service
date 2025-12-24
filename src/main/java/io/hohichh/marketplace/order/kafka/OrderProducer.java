package io.hohichh.marketplace.order.kafka;

import io.hohichh.marketplace.payment.dto.event.OrderCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderProducer {
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendOrderCreatedEvent(OrderCreatedEvent event) {
        log.info("Sending payment created event for Order: {}", event.orderId());
        kafkaTemplate.send("order-created-events", event.orderId(), event);
    }
}
