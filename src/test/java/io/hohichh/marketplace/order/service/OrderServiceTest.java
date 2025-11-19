package io.hohichh.marketplace.order.service;

import io.hohichh.marketplace.order.client.UserServiceClient;
import io.hohichh.marketplace.order.dto.*;
import io.hohichh.marketplace.order.dto.item.NewOrderItemDto;
import io.hohichh.marketplace.order.dto.product.ProductDto;
import io.hohichh.marketplace.order.exception.ActionNotPermittedException;
import io.hohichh.marketplace.order.exception.ResourceNotFoundException;
import io.hohichh.marketplace.order.mapper.OrderItemMapper;
import io.hohichh.marketplace.order.mapper.OrderMapper;
import io.hohichh.marketplace.order.mapper.ProductMapper;
import io.hohichh.marketplace.order.model.OrderItem;
import io.hohichh.marketplace.order.model.order.Order;
import io.hohichh.marketplace.order.model.order.Status;
import io.hohichh.marketplace.order.repository.OrderItemRepository;
import io.hohichh.marketplace.order.repository.OrderRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.client.circuitbreaker.CircuitBreaker;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private OrderItemRepository orderItemRepository;
    @Mock
    private ProductService productService;
    @Mock
    private OrderMapper orderMapper;
    @Mock
    private ProductMapper productMapper;
    @Mock
    private OrderItemMapper orderItemMapper;
    @Mock
    private UserServiceClient userClient;
    @Mock
    private CircuitBreakerFactory circuitBreakerFactory;
    @Mock
    private CircuitBreaker circuitBreaker;
    @Mock
    private Clock clock;

    @InjectMocks
    private OrderServiceImpl orderService;

    private SecurityContext securityContext;
    private Authentication authentication;

    private final UUID userId = UUID.randomUUID();
    private final LocalDate fixedDate = LocalDate.of(2025, 1, 1);

    @BeforeEach
    void setUp() {
        lenient().when(circuitBreakerFactory.create(any())).thenReturn(circuitBreaker);
        lenient().when(circuitBreaker.run(any(), any())).thenAnswer(invocation -> {
            Supplier<Object> supplier = invocation.getArgument(0);
            try {
                return supplier.get();
            } catch (Exception e) {
                Function<Throwable, Object> fallback = invocation.getArgument(1);
                return fallback.apply(e);
            }
        });

        lenient().when(clock.instant()).thenReturn(Instant.parse("2025-01-01T10:00:00Z"));
        lenient().when(clock.getZone()).thenReturn(ZoneId.of("UTC"));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void mockSecurityContext() {
        securityContext = mock(SecurityContext.class);
        authentication = mock(Authentication.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(userId.toString());
        SecurityContextHolder.setContext(securityContext);
    }

    private void setOrderId(Order order, UUID id) {
        ReflectionTestUtils.setField(order, "id", id);
    }

    @Test
    void createOrder_shouldReturnOrderWithItemsDto_userDtoShouldBeNull() {
        mockSecurityContext();

        UUID productId = UUID.randomUUID();
        NewOrderItemDto itemDto = new NewOrderItemDto(productId, 2);
        List<NewOrderItemDto> items = List.of(itemDto);
        UUID orderId = UUID.randomUUID();

        ProductDto productDto = new ProductDto(productId, "Test Product", BigDecimal.TEN);

        Order savedOrder = new Order();
        setOrderId(savedOrder, orderId);
        savedOrder.setUserId(userId);
        savedOrder.setStatus(Status.PENDING);
        savedOrder.setCreationDate(fixedDate);

        OrderWithItemsDto expectedDto = new OrderWithItemsDto(
                orderId, userId, Status.PENDING, fixedDate, null, List.of()
        );

        when(productService.getProductById(productId)).thenReturn(productDto);
        when(orderItemMapper.toOrderItem(any(), any(), any())).thenReturn(new OrderItem());
        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);
        when(userClient.getUserById(userId)).thenReturn(null);
        when(orderMapper.toDtoWithItems(savedOrder, null)).thenReturn(expectedDto);

        OrderWithItemsDto result = orderService.createOrder(items);

        assertNotNull(result);
        assertEquals(expectedDto, result);
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    void updateOrderStatus_shouldReturnOrderWithItemsDto_userDtoShouldBeNull() {
        UUID orderId = UUID.randomUUID();
        NewStatusOrderDto statusDto = new NewStatusOrderDto(Status.SHIPPED);

        Order existingOrder = new Order();
        setOrderId(existingOrder, orderId);
        existingOrder.setUserId(userId);
        existingOrder.setStatus(Status.PENDING);

        Order updatedOrder = new Order();
        setOrderId(updatedOrder, orderId);
        updatedOrder.setUserId(userId);
        updatedOrder.setStatus(Status.SHIPPED);

        OrderWithItemsDto expectedDto = new OrderWithItemsDto(
                orderId, userId, Status.SHIPPED, fixedDate, null, List.of()
        );

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(existingOrder));
        when(orderRepository.save(existingOrder)).thenReturn(updatedOrder);
        when(userClient.getUserById(userId)).thenReturn(null);
        when(orderMapper.toDtoWithItems(updatedOrder, null)).thenReturn(expectedDto);

        OrderWithItemsDto result = orderService.updateOrderStatus(orderId, statusDto);

        assertEquals(expectedDto, result);
        assertEquals(Status.SHIPPED, existingOrder.getStatus());
    }

    @Test
    void updateOrderStatus_shouldThrowResourceNotFoundException() {
        UUID orderId = UUID.randomUUID();
        NewStatusOrderDto statusDto = new NewStatusOrderDto(Status.SHIPPED);
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> orderService.updateOrderStatus(orderId, statusDto));
    }

    @Test
    void cancelOrder_shouldReturnOrderWithItemsDto_userDtoShouldBeNull() {
        UUID orderId = UUID.randomUUID();
        Order order = new Order();
        setOrderId(order, orderId);
        order.setUserId(userId);
        order.setStatus(Status.PENDING);

        OrderWithItemsDto expectedDto = new OrderWithItemsDto(
                orderId, userId, Status.CANCELLED, fixedDate, null, List.of()
        );

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(orderRepository.save(order)).thenReturn(order);
        when(orderMapper.toDtoWithItems(order, null)).thenReturn(expectedDto);

        OrderWithItemsDto result = orderService.cancelOrder(orderId);

        assertEquals(expectedDto, result);
        assertEquals(Status.CANCELLED, order.getStatus());
    }

    @Test
    void cancelOrder_shouldThrowActionNotPermittedException() {
        UUID orderId = UUID.randomUUID();
        Order order = new Order();
        setOrderId(order, orderId);
        order.setStatus(Status.SHIPPED);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        assertThrows(ActionNotPermittedException.class,
                () -> orderService.cancelOrder(orderId));
    }

    @Test
    void deleteOrder_void() {
        UUID orderId = UUID.randomUUID();
        when(orderRepository.existsById(orderId)).thenReturn(true);

        orderService.deleteOrder(orderId);

        verify(orderRepository).deleteById(orderId);
    }

    @Test
    void deleteOrder_shouldThrowResourceNotFoundException() {
        UUID orderId = UUID.randomUUID();
        when(orderRepository.existsById(orderId)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class,
                () -> orderService.deleteOrder(orderId));
    }

    @Test
    void getOrderById_shouldReturnOrderWithItemsDto_userDtoShouldBeNull() {
        UUID orderId = UUID.randomUUID();
        Order order = new Order();
        setOrderId(order, orderId);
        order.setUserId(userId);

        OrderWithItemsDto expectedDto = new OrderWithItemsDto(
                orderId, userId, Status.PENDING, fixedDate, null, List.of()
        );

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(userClient.getUserById(userId)).thenReturn(null);
        when(orderMapper.toDtoWithItems(order, null)).thenReturn(expectedDto);

        OrderWithItemsDto result = orderService.getOrderById(orderId);

        assertEquals(expectedDto, result);
    }

    @Test
    void getOrderById_shouldThrowResourceNotFoundException() {
        UUID orderId = UUID.randomUUID();
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> orderService.getOrderById(orderId));
    }

    @Test
    void getOrdersByUserId_shouldReturnListOfOrderDto() {
        Order order = new Order();
        setOrderId(order, UUID.randomUUID());
        List<Order> orders = List.of(order);
        OrderDto orderDto = new OrderDto(order.getId(), userId, Status.PENDING, fixedDate);

        when(orderRepository.findOrdersByUserId(userId)).thenReturn(orders);
        when(orderMapper.toDtoList(orders)).thenReturn(List.of(orderDto));

        List<OrderDto> result = orderService.getOrdersByUserId(userId);

        assertEquals(1, result.size());
        assertEquals(orderDto, result.get(0));
    }

    @Test
    void searchOrders_shouldReturnPageOfOrderDto() {
        Pageable pageable = PageRequest.of(0, 10);
        Order order = new Order();
        setOrderId(order, UUID.randomUUID());
        Page<Order> orderPage = new PageImpl<>(List.of(order));
        OrderDto orderDto = new OrderDto(order.getId(), userId, Status.PENDING, fixedDate);

        when(orderRepository.findAllByFilter(any(), any(), eq(pageable))).thenReturn(orderPage);
        when(orderMapper.toDto(order)).thenReturn(orderDto);

        Page<OrderDto> result = orderService.searchOrders(null, null, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(orderDto, result.getContent().get(0));
    }
}