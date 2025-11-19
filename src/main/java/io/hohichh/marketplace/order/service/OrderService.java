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
    OrderWithItemsDto updateOrderStatus(UUID id, NewStatusOrderDto order);
    OrderWithItemsDto cancelOrder(UUID id);
    void deleteOrder(UUID id);
    OrderWithItemsDto getOrderById(UUID id);
    List<OrderDto> getOrdersByUserId(UUID userId);
    List<OrderDto> getOrdersByIds(List<UUID> ids);
    Page<OrderDto> getOrderByStatuses(Pageable pageable, List<Status> statuses);
}
