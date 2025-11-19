package io.hohichh.marketplace.order.service;

import io.hohichh.marketplace.order.client.UserServiceClient;
import io.hohichh.marketplace.order.dto.*;
import io.hohichh.marketplace.order.dto.item.NewOrderItemDto;
import io.hohichh.marketplace.order.dto.product.ProductDto;
import io.hohichh.marketplace.order.exception.ActionNotPermittedException;
import io.hohichh.marketplace.order.exception.ResourceNotFoundException;
import io.hohichh.marketplace.order.mapper.*;
import io.hohichh.marketplace.order.model.OrderItem;
import io.hohichh.marketplace.order.model.order.*;
import io.hohichh.marketplace.order.repository.*;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JCircuitBreakerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


//todo кеширование
@Service
@AllArgsConstructor
public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;

    private final ProductService productService;

    private final OrderMapper orderMapper;
    private final ProductMapper productMapper;

    private final Clock clock;

    private static final Logger log = LoggerFactory.getLogger(OrderServiceImpl.class);
    private final OrderItemMapper orderItemMapper;

    private final UserServiceClient userClient;
    private final Resilience4JCircuitBreakerFactory  circuitBreakerFactory;

    @Transactional
    public OrderWithItemsDto createOrder(List<NewOrderItemDto> items){
        log.debug("Creating new order with {} items", items.size());
        UUID userId = UUID.fromString(SecurityContextHolder.getContext().getAuthentication().getName());

        Order order = new Order();
        order.setStatus(Status.PENDING);
        order.setCreationDate(LocalDate.now(clock));
        order.setUserId(userId);

        List<OrderItem> entityItems = new ArrayList<>();
        for(NewOrderItemDto itemDto : items){
            ProductDto product = productService.getProductById(itemDto.productId());
            OrderItem orderItem = orderItemMapper.toOrderItem(itemDto, order, product);

            entityItems.add(orderItem);
        }
        order.setOrderItems(entityItems);
        Order savedOrder = orderRepository.save(order);

        UserDto userDto = getUserWithCircuitBreaker(userId);

        log.info("Order {} created with {} items successfully", savedOrder.getId(), entityItems.size());
        return orderMapper.toDtoWithItems(savedOrder, userDto);
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public OrderWithItemsDto updateOrderStatus(UUID id, NewStatusOrderDto order) {
        log.debug("Updating order with id {}", id);
        UUID userId = UUID.fromString(SecurityContextHolder.getContext().getAuthentication().getName());

        Order orderToUpd = orderRepository.findById(id).orElseThrow(
                () -> {
                    log.error("Can't update order: Order with id {} not found", id);
                    return new ResourceNotFoundException("Order with id " + id + " not found");
                }
        );

        orderToUpd.setStatus(order.status());

        Order savedOrder = orderRepository.save(orderToUpd);

        UserDto userDto = getUserWithCircuitBreaker(userId);

        log.info("order with id {} updated successfully", id);
        return orderMapper.toDtoWithItems(savedOrder, userDto);
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN') or @orderSecurity.isOrderOwner(#id, authentication)")
    public OrderWithItemsDto cancelOrder(UUID id) {
        log.debug("Cancelling order with id {}", id);
        UUID userId = UUID.fromString(SecurityContextHolder.getContext().getAuthentication().getName());

        Order order = orderRepository.findById(id).orElseThrow(() -> {
            log.error("Can't cancel order: Order with id {} not found", id);
            return new ResourceNotFoundException("Order with id " + id + " not found");
        });

        if(order.getStatus().equals(Status.PENDING)){
            order.setStatus(Status.CANCELLED);
        }else {
            throw new ActionNotPermittedException("Order status is not PENDING");
        }

        Order savedOrder = orderRepository.save(order);

        UserDto userDto = getUserWithCircuitBreaker(userId);

        log.info("Order with id {} cancelled successfully", id);
        return orderMapper.toDtoWithItems(savedOrder, userDto);
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteOrder(UUID id) {
        log.debug("Deleting order with id {}", id);

        orderRepository.deleteById(id);

        log.info("Order with id {} deleted successfully", id);
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ADMIN') or @orderSecurity.isOrderOwner(#id, authentication)")
    public OrderWithItemsDto getOrderById(UUID id) {
        log.debug("Getting order with id {}", id);
        UUID userId = UUID.fromString(SecurityContextHolder.getContext().getAuthentication().getName());

        Order order = orderRepository.findById(id).orElseThrow(() -> {
            log.error("Order not found with id: {}", id);
            return new ResourceNotFoundException("Order not found with id: " + id);
        });

        UserDto userDto = getUserWithCircuitBreaker(userId);

        log.info("Order with id {} got successfully", id);
        return orderMapper.toDtoWithItems(order, userDto);
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ADMIN') or @orderSecurity.isAccountOwner(#userId, authentication)")
    public List<OrderDto> getOrdersByUserId(UUID userId) {
        log.debug("Getting orders by user id {}", userId);

        List<Order> orders = orderRepository.findOrdersByUserId(userId);

        log.info("Orders by user id got successfully {}", userId);
        return orderMapper.toDtoList(orders);
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ADMIN')")
    public Page<OrderDto> searchOrders(List<UUID> ids, List<Status> statuses, Pageable pageable) {
        log.debug("Searching orders by filter. Ids count: {}, Statuses count: {}",
                ids != null ? ids.size() : "null",
                statuses != null ? statuses.size() : "null");

        List<UUID> safeIds = (ids != null && ids.isEmpty()) ? null : ids;
        List<Status> safeStatuses = (statuses != null && statuses.isEmpty()) ? null : statuses;

        Page<Order> ordersPage = orderRepository.findAllByFilter(safeIds, safeStatuses, pageable);

        log.info("Orders found: {}", ordersPage.getTotalElements());

        return ordersPage.map(orderMapper::toDto);
    }


    private UserDto getUserWithCircuitBreaker(UUID userId) {
        return circuitBreakerFactory.create("user-service").run(
                () -> userClient.getUserById(userId),
                throwable -> {
                    log.warn("Failed to get user info for id {}: {}", userId, throwable.getMessage());
                    return null;
                }
        );
    }
}
