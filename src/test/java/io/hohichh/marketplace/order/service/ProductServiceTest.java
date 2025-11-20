package io.hohichh.marketplace.order.service;

import io.hohichh.marketplace.order.dto.product.NewProductDto;
import io.hohichh.marketplace.order.dto.product.ProductDto;
import io.hohichh.marketplace.order.exception.ResourceNotFoundException;
import io.hohichh.marketplace.order.mapper.ProductMapper;
import io.hohichh.marketplace.order.model.Product;
import io.hohichh.marketplace.order.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductMapper productMapper;

    @InjectMocks
    private ProductServiceImpl productService;

    @Test
    void createProduct_shouldReturnProductDto() {
        // Arrange
        NewProductDto newProductDto = new NewProductDto("Test Product", BigDecimal.TEN);
        Product product = new Product();
        product.setName("Test Product");
        product.setPrice(BigDecimal.TEN);

        Product savedProduct = new Product();
        ProductDto expectedDto = new ProductDto(UUID.randomUUID(), "Test Product", BigDecimal.TEN);

        when(productMapper.toProduct(newProductDto)).thenReturn(product);
        when(productRepository.save(product)).thenReturn(savedProduct);
        when(productMapper.toProductDto(savedProduct)).thenReturn(expectedDto);

        // Act
        ProductDto result = productService.createProduct(newProductDto);

        // Assert
        assertNotNull(result);
        assertEquals(expectedDto.name(), result.name());
        verify(productRepository).save(product);
    }

    @Test
    void updateProduct_shouldReturnProductDto() {
        // Arrange
        UUID id = UUID.randomUUID();
        NewProductDto updateDto = new NewProductDto("Updated Name", BigDecimal.ONE);

        Product existingProduct = new Product();
        existingProduct.setName("Old Name");

        Product updatedProduct = new Product();
        updatedProduct.setName("Updated Name");

        ProductDto expectedDto = new ProductDto(id, "Updated Name", BigDecimal.ONE);

        when(productRepository.findById(id)).thenReturn(Optional.of(existingProduct));
        when(productRepository.save(existingProduct)).thenReturn(updatedProduct);
        when(productMapper.toProductDto(updatedProduct)).thenReturn(expectedDto);

        // Act
        ProductDto result = productService.updateProduct(id, updateDto);

        // Assert
        assertEquals(expectedDto, result);
        verify(productMapper).updateProductFromDto(updateDto, existingProduct);
        verify(productRepository).save(existingProduct);
    }

    @Test
    void updateProduct_shouldThrowResourceNotFoundException() {
        // Arrange
        UUID id = UUID.randomUUID();
        NewProductDto updateDto = new NewProductDto("Name", BigDecimal.TEN);

        when(productRepository.findById(id)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
                () -> productService.updateProduct(id, updateDto));

        verify(productRepository, never()).save(any());
    }

    @Test
    void deleteProduct_voidType() {
        // Arrange
        UUID id = UUID.randomUUID();
        when(productRepository.existsById(id)).thenReturn(true);

        // Act
        productService.deleteProduct(id);

        // Assert
        verify(productRepository).deleteById(id);
    }

    @Test
    void deleteProduct_shouldThrowResourceNotFoundException() {
        // Arrange
        UUID id = UUID.randomUUID();
        when(productRepository.existsById(id)).thenReturn(false);

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
                () -> productService.deleteProduct(id));

        verify(productRepository, never()).deleteById(any());
    }

    @Test
    void getAllProducts_shouldReturnPageOfProductDto() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Product product = new Product();
        Page<Product> productPage = new PageImpl<>(List.of(product));
        ProductDto productDto = new ProductDto(UUID.randomUUID(), "Name", BigDecimal.TEN);

        when(productRepository.findAll(pageable)).thenReturn(productPage);
        when(productMapper.toProductDto(product)).thenReturn(productDto);

        // Act
        Page<ProductDto> result = productService.getAllProducts(pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(productDto, result.getContent().get(0));
    }

    @Test
    void getProductById_shouldReturnProductDto() {
        // Arrange
        UUID id = UUID.randomUUID();
        Product product = new Product();
        ProductDto expectedDto = new ProductDto(id, "Name", BigDecimal.TEN);

        when(productRepository.findById(id)).thenReturn(Optional.of(product));
        when(productMapper.toProductDto(product)).thenReturn(expectedDto);

        // Act
        ProductDto result = productService.getProductById(id);

        // Assert
        assertEquals(expectedDto, result);
    }

    @Test
    void getProductById_shouldThrowResourceNotFoundException() {
        // Arrange
        UUID id = UUID.randomUUID();
        when(productRepository.findById(id)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
                () -> productService.getProductById(id));
    }
}