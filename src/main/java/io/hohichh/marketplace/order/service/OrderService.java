package io.hohichh.marketplace.order.service;

import io.hohichh.marketplace.order.dto.;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.UUID;

public interface OrderService {
    OrderDto createOrder(NewOrderDto order);
    OrderDto updateOrder(UUID id, NewOrderDto order);
    void deleteOrder(UUID id);
    OrderDto getOrderById(UUID id);
    List<OrderDto> getOrdersByUserId(UUID userId);
    List<OrderDto> getOrdersById(List<UUID> ids);
    Page<OrderDto> getOrderByStatuses(Pageable pageable, List<String> statuses);
}
