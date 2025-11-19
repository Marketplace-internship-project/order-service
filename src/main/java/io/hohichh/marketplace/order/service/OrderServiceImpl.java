package io.hohichh.marketplace.order.service;

import io.hohichh.marketplace.order.dto.*;
import io.hohichh.marketplace.order.dto.item.NewOrderItemDto;
import io.hohichh.marketplace.order.dto.product.ProductDto;
import io.hohichh.marketplace.order.mapper.*;
import io.hohichh.marketplace.order.model.OrderItem;
import io.hohichh.marketplace.order.model.order.*;
import io.hohichh.marketplace.order.repository.*;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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

    @Transactional
    public OrderWithItemsDto createOrder(List<NewOrderItemDto> items){
        log.debug("Creating new order with {} items", items.size());

        Order order = new Order();
        order.setStatus(Status.PENDING);
        order.setCreationDate(LocalDate.now(clock));
        order.setUserId(UUID.randomUUID()); //TODO добавить security и контекст безопасности и достать айди юзера из jwt

        List<OrderItem> entityItems = new ArrayList<>();
        for(NewOrderItemDto itemDto : items){
            ProductDto product = productService.getProductById(itemDto.productId());
            OrderItem orderItem = orderItemMapper.toOrderItem(itemDto, order, product);

            entityItems.add(orderItem);
        }
        order.setOrderItems(entityItems);
        Order savedOrder = orderRepository.save(order);

        UserDto userDto = null; //TODO написать curcuit breaker клиент к юзер-сервису и забирать у него юзера по айди

        log.info("Order {} created with {} items successfully", savedOrder.getId(), entityItems.size());
        return orderMapper.toDtoWithItems(savedOrder, userDto);
    }

    @Override
    @Transactional
    public OrderWithItemsDto updateOrderStatus(UUID id, NewStatusOrderDto order) {
        log.debug("Updating order with id {}", id);
        //todo: проверка на админа
        Order orderToUpd = orderRepository.findOrderById(id);

        orderToUpd.setStatus(order.status());

        Order savedOrder = orderRepository.save(orderToUpd);

        //todo: добавть секьюрити контекст и взять юзер айди
        UserDto userDto = null; //todo: достать юзера из юзер-севриса

        log.info("order with id {} updated successfully", id);
        return orderMapper.toDtoWithItems(savedOrder, userDto);
    }

    @Override
    @Transactional
    public OrderWithItemsDto cancelOrder(UUID id) {
        log.debug("Cancelling order with id {}", id);
        //todo: (секьюрный класс)для безопасности тут еще будет проврека на соотвествия айди юзера из jwt и из параметра(если это не админ)
        Order order = orderRepository.findOrderById(id);

        if(order.getStatus().equals(Status.PENDING)){
            order.setStatus(Status.CANCELLED);
        }else {
            throw new RuntimeException("Order status is not PENDING"); //todo заменить на кастомное исключение
        }

        Order savedOrder = orderRepository.save(order);

        //todo: добавть секьюрити контекст и взять юзер айди
        UserDto userDto = null; //todo: достать юзера из юзер-севриса

        log.info("Oorder with id {} cancelled successfully", id);
        return orderMapper.toDtoWithItems(savedOrder, userDto);
    }

    @Override
    @Transactional
    public void deleteOrder(UUID id) {
        //todo проверка админской роли (секьюрный класс)
        log.debug("Deleting order with id {}", id);

        orderRepository.deleteById(id);

        log.info("Order with id {} deleted successfully", id);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderWithItemsDto getOrderById(UUID id) {
        log.debug("Getting order with id {}", id);
        //todo: (секьюрный класс)после получения заказа проверка на соотвествие айди юзера и юзера из jwt(если это не админ)
        Order order = orderRepository.findOrderById(id);

        //todo: добавть секьюрити контекст и взять юзер айди
        UserDto userDto = null; //todo: достать юзера из юзер-севриса

        log.info("Order with id {} got successfully", id);
        return orderMapper.toDtoWithItems(order, userDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderDto> getOrdersByUserId(UUID userId) {
        log.debug("Getting orders by user id {}", userId);
        //todo: (секьюрный класс)для безопасности тут еще будет проврека на соотвествия айди юзера из jwt и из параметра(если это не админ)
        List<Order> orders = orderRepository.findOrdersByUserId(userId);

        log.info("Orders by user id got successfully {}", userId);
        return orderMapper.toDtoList(orders);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderDto> getOrdersByIds(List<UUID> ids) {
        log.debug("Getting orders by ids");
        //todo проверка на админа
        List<Order> orders = orderRepository.findOrdersByIds(ids);

        log.info("Orders by ids got successfully");
        return orderMapper.toDtoList(orders);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderDto> getOrderByStatuses(Pageable pageable, List<Status> statuses) {
        log.debug("Getting orders by statuses");
        //todo проверка на админа
        Page<Order> orders = orderRepository.findOrdersByStatuses(statuses, pageable);

        log.info("Orders by statuses got successfully");
        return orders.map(orderMapper::toDto);
    }
}
