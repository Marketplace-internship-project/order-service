package io.hohichh.marketplace.order.integration;

import io.hohichh.marketplace.order.dto.product.NewProductDto;
import io.hohichh.marketplace.order.dto.product.ProductDto;
import io.hohichh.marketplace.order.model.Product;
import io.hohichh.marketplace.order.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ProductAppTest extends AbstractApplicationTest {

    @Autowired
    private ProductRepository productRepository;

    private String adminToken;
    private String userToken;

    @BeforeEach
    void setUp() {
        productRepository.deleteAll();
        adminToken = generateToken(UUID.randomUUID(), "ADMIN");
        userToken = generateToken(UUID.randomUUID(), "USER");
    }

    @Test
    void createProduct_shouldSucceed_whenAdmin() {
        // Arrange
        NewProductDto newProduct = new NewProductDto("iPhone 15", new BigDecimal("999.99"));

        // Act
        ResponseEntity<ProductDto> response = restTemplate.postForEntity(
                "/v1/products",
                getAuthHeaders(adminToken, newProduct),
                ProductDto.class
        );

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().name()).isEqualTo("iPhone 15");

        // Проверяем БД
        assertThat(productRepository.findAll()).hasSize(1);
    }

    @Test
    void createProduct_shouldReturn403_whenUser() {
        // Arrange
        NewProductDto newProduct = new NewProductDto("Hacker Item", new BigDecimal("0.00"));

        // Act
        ResponseEntity<String> response = restTemplate.postForEntity(
                "/v1/products",
                getAuthHeaders(userToken, newProduct),
                String.class // Ожидаем ошибку
        );

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(productRepository.findAll()).isEmpty();
    }

    // ... предыдущие методы ...

    @Test
    void updateProduct_shouldSucceedWhenAdmin() {
        // Arrange
        Product productEntity = new Product();
        productEntity.setName("Old Name");
        productEntity.setPrice(BigDecimal.ONE);
        Product savedProduct = productRepository.save(productEntity);

        NewProductDto updateDto = new NewProductDto("New Name", BigDecimal.TEN);

        // Act
        ResponseEntity<ProductDto> response = restTemplate.exchange(
                "/v1/products/" + savedProduct.getId(),
                HttpMethod.PUT,
                getAuthHeaders(adminToken, updateDto),
                ProductDto.class
        );

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().name()).isEqualTo("New Name");

        // Проверяем БД
        Product updatedInDb = productRepository.findById(savedProduct.getId()).orElseThrow();
        assertThat(updatedInDb.getName()).isEqualTo("New Name");
    }

    @Test
    void updateProduct_shouldFailedWhenUser() {
        // Arrange
        Product productEntity = new Product();
        productEntity.setName("Old Name");
        productEntity.setPrice(BigDecimal.ONE);
        Product savedProduct = productRepository.save(productEntity);

        NewProductDto updateDto = new NewProductDto("Hacker Update", BigDecimal.TEN);

        // Act
        ResponseEntity<String> response = restTemplate.exchange(
                "/v1/products/" + savedProduct.getId(),
                HttpMethod.PUT,
                getAuthHeaders(userToken, updateDto),
                String.class
        );

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        Product notUpdatedInDb = productRepository.findById(savedProduct.getId()).orElseThrow();
        assertThat(notUpdatedInDb.getName()).isEqualTo("Old Name");
    }

    @Test
    void deleteProduct_shouldSucceed_WhenAdmin() {
        // Arrange
        Product productEntity = new Product();
        productEntity.setName("To Delete");
        productEntity.setPrice(BigDecimal.TEN);
        Product savedProduct = productRepository.save(productEntity);

        // Act
        ResponseEntity<Void> response = restTemplate.exchange(
                "/v1/products/" + savedProduct.getId(),
                HttpMethod.DELETE,
                getAuthHeaders(adminToken),
                Void.class
        );

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(productRepository.existsById(savedProduct.getId())).isFalse();
    }

    @Test
    void deleteProduct_shouldReturn403_whenUser() {
        // Arrange
        Product productEntity = new Product();
        productEntity.setName("To Delete");
        productEntity.setPrice(BigDecimal.TEN);
        Product savedProduct = productRepository.save(productEntity);

        // Act
        ResponseEntity<Void> response = restTemplate.exchange(
                "/v1/products/" + savedProduct.getId(),
                HttpMethod.DELETE,
                getAuthHeaders(userToken),
                Void.class
        );

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(productRepository.existsById(savedProduct.getId())).isTrue();
    }


    @Test
    void getProduct_shouldSucceed_whenUser() {
        // Arrange
        Product productEntity = new Product();
        productEntity.setName("Bread");
        productEntity.setPrice(new BigDecimal("2.50"));
        Product savedProduct = productRepository.save(productEntity);

        // Act
        ResponseEntity<ProductDto> response = restTemplate.exchange(
                "/v1/products/" + savedProduct.getId(),
                HttpMethod.GET,
                getAuthHeaders(userToken),
                ProductDto.class
        );

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().name()).isEqualTo("Bread");
    }

    @Test
    void getAllProducts_shouldSucceed_WhenUser() {
        // Arrange
        productRepository.save(createProduct("P1", BigDecimal.ONE));
        productRepository.save(createProduct("P2", BigDecimal.TEN));

        // Act
        ResponseEntity<RestResponsePage<ProductDto>> response = restTemplate.exchange(
                "/v1/products",
                HttpMethod.GET,
                getAuthHeaders(userToken),
                new org.springframework.core.ParameterizedTypeReference<RestResponsePage<ProductDto>>() {}
        );

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getContent()).hasSize(2);
        assertThat(response.getBody().getTotalElements()).isEqualTo(2);
    }

    private Product createProduct(String name, BigDecimal price) {
        Product p = new Product();
        p.setName(name);
        p.setPrice(price);
        return p;
    }
}