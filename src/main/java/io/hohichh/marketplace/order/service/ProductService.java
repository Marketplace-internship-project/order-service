package io.hohichh.marketplace.order.service;

import io.hohichh.marketplace.order.dto.product.NewProductDto;
import io.hohichh.marketplace.order.dto.product.ProductDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface ProductService {
    ProductDto createProduct(NewProductDto product);
    ProductDto updateProduct(UUID id, NewProductDto product);
    void deleteProduct(UUID id);
    Page<ProductDto> getAllProducts(Pageable pageable);
    Optional<ProductDto> getProductById(UUID id);
}
