package io.hohichh.marketplace.order.service;

import io.hohichh.marketplace.order.dto.*;
import io.hohichh.marketplace.order.dto.item.NewOrderItemDto;
import io.hohichh.marketplace.order.model.order.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.UUID;

public interface OrderService {
    OrderWithItemsDto createOrder(List<NewOrderItemDto> items);
    void updateOrderStatusSystem(UUID id, Status status);
    OrderWithItemsDto updateOrderStatus(UUID id, NewStatusOrderDto order);
    OrderWithItemsDto cancelOrder(UUID id);
    void deleteOrder(UUID id);
    OrderWithItemsDto getOrderById(UUID id);
    List<OrderDto> getOrdersByUserId(UUID userId);
    Page<OrderDto> searchOrders(List<UUID> ids, List<Status> statuses, Pageable pageable);
}
