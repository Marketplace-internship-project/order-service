package io.hohichh.marketplace.order.integration;

import io.hohichh.marketplace.order.client.UserServiceClient;
import io.hohichh.marketplace.order.dto.*;
import io.hohichh.marketplace.order.dto.item.NewOrderItemDto;
import io.hohichh.marketplace.order.model.Product;
import io.hohichh.marketplace.order.model.order.Order;
import io.hohichh.marketplace.order.model.order.Status;
import io.hohichh.marketplace.order.repository.OrderRepository;
import io.hohichh.marketplace.order.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

class OrderAppTest extends AbstractApplicationTest {

    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private OrderRepository orderRepository;

    @Mock
    private UserServiceClient userServiceClient;

    private UUID userId;
    private String userToken;
    private Product storedProduct;

    @BeforeEach
    void setUp() {
        orderRepository.deleteAll();
        productRepository.deleteAll();

        userId = UUID.randomUUID();
        userToken = generateToken(userId, "USER");

        Product product = new Product();
        product.setName("Test Item");
        product.setPrice(new BigDecimal("100.00"));
        storedProduct = productRepository.save(product);


        UserDto mockUser = new UserDto(userId, "John", "Doe", LocalDate.of(1990, 1, 1), "john@test.com");
        when(userServiceClient.getUserById(userId)).thenReturn(mockUser);
    }

    @Test
    void createOrder_shouldSucceed() {
        // Arrange
        List<NewOrderItemDto> items = List.of(
                new NewOrderItemDto(storedProduct.getId(), 2)
        );

        // Act
        ResponseEntity<OrderWithItemsDto> response = restTemplate.postForEntity(
                "/v1/orders",
                getAuthHeaders(userToken, items),
                OrderWithItemsDto.class
        );

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();

        OrderWithItemsDto createdOrder = response.getBody();
        assertThat(createdOrder.userId()).isEqualTo(userId);
        assertThat(createdOrder.status()).isEqualTo(Status.PENDING);
        assertThat(createdOrder.orderItems()).hasSize(1);
        assertThat(createdOrder.orderItems().get(0).pricePerUnit()).isEqualByComparingTo("100.00");
        assertThat(createdOrder.userDto()).isNotNull();
        assertThat(createdOrder.userDto().name()).isEqualTo("John");
    }


    @Test
    void updateOrderStatus_shouldSucceed_whenAdmin() {
        // Arrange
        UUID orderId = createOrderForUser(userToken);
        String adminToken = generateToken(UUID.randomUUID(), "ADMIN");
        NewStatusOrderDto updateDto = new NewStatusOrderDto(Status.SHIPPED);

        // Act
        ResponseEntity<OrderWithItemsDto> response = restTemplate.exchange(
                "/v1/orders/" + orderId,
                HttpMethod.PATCH,
                getAuthHeaders(adminToken, updateDto),
                OrderWithItemsDto.class
        );

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().status()).isEqualTo(Status.SHIPPED);
        assertThat(orderRepository.findById(orderId).get().getStatus()).isEqualTo(Status.SHIPPED);
    }

    @Test
    void updateOrderStatus_shouldThrow403_whenUser() {
        // Arrange
        UUID orderId = createOrderForUser(userToken);
        NewStatusOrderDto updateDto = new NewStatusOrderDto(Status.SHIPPED);

        // Act
        ResponseEntity<String> response = restTemplate.exchange(
                "/v1/orders/" + orderId,
                HttpMethod.PATCH,
                getAuthHeaders(userToken, updateDto),
                String.class
        );

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(orderRepository.findById(orderId).get().getStatus()).isEqualTo(Status.PENDING);
    }

    @Test
    void cancelOrder_shouldSucceed_WhenUser() {
        // Arrange
        UUID orderId = createOrderForUser(userToken);
        NewStatusOrderDto cancelDto = new NewStatusOrderDto(Status.CANCELLED);

        // Act
        ResponseEntity<OrderWithItemsDto> response = restTemplate.exchange(
                "/v1/orders/" + orderId,
                HttpMethod.PATCH,
                getAuthHeaders(userToken, cancelDto),
                OrderWithItemsDto.class
        );

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().status()).isEqualTo(Status.CANCELLED);
    }

    @Test
    void cancelOrder_shouldThrowConflict_WhenStatusNotPending() {
        // Arrange
        Order order = new Order();
        order.setUserId(userId);
        order.setStatus(Status.SHIPPED);
        order.setCreationDate(LocalDate.now());

        io.hohichh.marketplace.order.model.OrderItem item = new io.hohichh.marketplace.order.model.OrderItem();
        item.setOrder(order);
        item.setProductId(storedProduct.getId());
        item.setProductName("Test Product");
        item.setPricePerUnit(BigDecimal.TEN);
        item.setQuantity(1);
        order.setOrderItems(List.of(item));

        Order savedOrder = orderRepository.save(order);

        NewStatusOrderDto cancelDto = new NewStatusOrderDto(Status.CANCELLED);

        // Act
        ResponseEntity<String> response = restTemplate.exchange(
                "/v1/orders/" + savedOrder.getId(),
                HttpMethod.PATCH,
                getAuthHeaders(userToken, cancelDto),
                String.class
        );

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        Order orderInDb = orderRepository.findById(savedOrder.getId()).orElseThrow();
        assertThat(orderInDb.getStatus()).isEqualTo(Status.SHIPPED);
    }


    @Test
    void cancelOrder_shouldThrow403_WhenNotOwner() {
        // Arrange
        UUID orderId = createOrderForUser(userToken);
        String hackerToken = generateToken(UUID.randomUUID(), "USER");
        NewStatusOrderDto cancelDto = new NewStatusOrderDto(Status.CANCELLED);

        // Act
        ResponseEntity<String> response = restTemplate.exchange(
                "/v1/orders/" + orderId,
                HttpMethod.PATCH,
                getAuthHeaders(hackerToken, cancelDto),
                String.class
        );

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void deleteOrder_shouldSucceed_whenAdmin() {
        // Arrange
        UUID orderId = createOrderForUser(userToken);
        String adminToken = generateToken(UUID.randomUUID(), "ADMIN");

        // Act
        ResponseEntity<Void> response = restTemplate.exchange(
                "/v1/orders/" + orderId,
                HttpMethod.DELETE,
                getAuthHeaders(adminToken),
                Void.class
        );

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(orderRepository.existsById(orderId)).isFalse();
    }

    @Test
    void deleteOrder_shouldThrow403_whenUser() {
        // Arrange
        UUID orderId = createOrderForUser(userToken);

        // Act
        ResponseEntity<String> response = restTemplate.exchange(
                "/v1/orders/" + orderId,
                HttpMethod.DELETE,
                getAuthHeaders(userToken),
                String.class
        );

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(orderRepository.existsById(orderId)).isTrue();
    }

    @Test
    void getOrder_shouldSucceed_whenOwner() {
        // Arrange
        List<NewOrderItemDto> items = List.of(new NewOrderItemDto(storedProduct.getId(), 1));
        ResponseEntity<OrderWithItemsDto> createResponse = restTemplate.postForEntity(
                "/v1/orders",
                getAuthHeaders(userToken, items),
                OrderWithItemsDto.class
        );
        UUID orderId = createResponse.getBody().id();

        // Act
        ResponseEntity<OrderWithItemsDto> getResponse = restTemplate.exchange(
                "/v1/orders/" + orderId,
                HttpMethod.GET,
                getAuthHeaders(userToken),
                OrderWithItemsDto.class
        );

        // Assert
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getResponse.getBody().id()).isEqualTo(orderId);
    }

    @Test
    void getOrder_shouldReturn403_whenNotOwner() {
        // Arrange
        List<NewOrderItemDto> items = List.of(new NewOrderItemDto(storedProduct.getId(), 1));
        ResponseEntity<OrderWithItemsDto> createResponse = restTemplate.postForEntity(
                "/v1/orders",
                getAuthHeaders(userToken, items),
                OrderWithItemsDto.class
        );
        UUID orderId = createResponse.getBody().id();

        // Act
        String hackerToken = generateToken(UUID.randomUUID(), "USER");

        ResponseEntity<String> getResponse = restTemplate.exchange(
                "/v1/orders/" + orderId,
                HttpMethod.GET,
                getAuthHeaders(hackerToken),
                String.class
        );

        // Assert
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }


    @Test
    void getOrdersByUserId_shouldSucceed_WhenOwner() {
        // Arrange
        createOrderForUser(userToken);
        createOrderForUser(userToken);

        // Act
        ResponseEntity<List<OrderDto>> response = restTemplate.exchange(
                "/v1/orders?user-id=" + userId, // Параметр запроса
                HttpMethod.GET,
                getAuthHeaders(userToken),
                new org.springframework.core.ParameterizedTypeReference<List<OrderDto>>() {}
        );

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(2);
        assertThat(response.getBody().get(0).userId()).isEqualTo(userId);
    }

    @Test
    void getOrdersByUserId_shouldSucceed_WhenAdmin() {
        // Arrange
        createOrderForUser(userToken);
        String adminToken = generateToken(UUID.randomUUID(), "ADMIN");

        // Act
        ResponseEntity<List<OrderDto>> response = restTemplate.exchange(
                "/v1/orders?user-id=" + userId,
                HttpMethod.GET,
                getAuthHeaders(adminToken),
                new org.springframework.core.ParameterizedTypeReference<List<OrderDto>>() {}
        );

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotEmpty();
    }

    @Test
    void getOrdersByUserId_shouldThrow403_WhenNotOwner() {
        // Arrange
        UUID victimUserId = userId;
        UUID hackerId = UUID.randomUUID();
        String hackerToken = generateToken(hackerId, "USER");

        // Act
        ResponseEntity<String> response = restTemplate.exchange(
                "/v1/orders?user-id=" + victimUserId,
                HttpMethod.GET,
                getAuthHeaders(hackerToken),
                String.class
        );

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void searchOrders_shouldSucceed_WhenAdmin() {
        // Arrange
        UUID order1 = createOrderForUser(userToken);
        UUID order2 = createOrderForUser(userToken);
        String adminToken = generateToken(UUID.randomUUID(), "ADMIN");

        // Act
        ResponseEntity<RestResponsePage<OrderDto>> response = restTemplate.exchange(
                "/v1/orders?size=10&page=0",
                HttpMethod.GET,
                getAuthHeaders(adminToken),
                new org.springframework.core.ParameterizedTypeReference<RestResponsePage<OrderDto>>() {}
        );

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getTotalElements()).isGreaterThanOrEqualTo(2);
        assertThat(response.getBody().getContent())
                .extracting(OrderDto::id)
                .contains(order1, order2);
    }

    @Test
    void searchOrders_shouldSucceed_WhenAdmin_WithFilters() {
        // Arrange
        UUID orderId = createOrderForUser(userToken);
        String adminToken = generateToken(UUID.randomUUID(), "ADMIN");

        // Act
        ResponseEntity<RestResponsePage<OrderDto>> response = restTemplate.exchange(
                "/v1/orders?ids=" + orderId,
                HttpMethod.GET,
                getAuthHeaders(adminToken),
                new org.springframework.core.ParameterizedTypeReference<RestResponsePage<OrderDto>>() {}
        );

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getTotalElements()).isEqualTo(1);
        assertThat(response.getBody().getContent().get(0).id()).isEqualTo(orderId);
    }

    @Test
    void searchOrders_shouldThrow403_WhenUser() {
        // Act
        ResponseEntity<String> response = restTemplate.exchange(
                "/v1/orders",
                HttpMethod.GET,
                getAuthHeaders(userToken),
                String.class
        );

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    private UUID createOrderForUser(String token) {
        List<NewOrderItemDto> items = List.of(new NewOrderItemDto(storedProduct.getId(), 1));
        ResponseEntity<OrderWithItemsDto> response = restTemplate.postForEntity(
                "/v1/orders",
                getAuthHeaders(token, items),
                OrderWithItemsDto.class
        );
        return response.getBody().id();
    }
}