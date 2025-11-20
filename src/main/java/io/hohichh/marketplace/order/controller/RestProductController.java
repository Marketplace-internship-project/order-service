package io.hohichh.marketplace.order.controller;

import io.hohichh.marketplace.order.dto.product.NewProductDto;
import io.hohichh.marketplace.order.dto.product.ProductDto;
import io.hohichh.marketplace.order.service.ProductService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/v1/products")
@AllArgsConstructor
public class RestProductController {
    private ProductService productService;
    private static final Logger logger = LoggerFactory.getLogger(RestProductController.class);

    @PostMapping
    public ResponseEntity<ProductDto> createProduct(
            @Valid
            @RequestBody NewProductDto newProductDto) {
        logger.debug("Received request to create product");

        ProductDto productDto = productService.createProduct(newProductDto);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(productDto.id())
                .toUri();

        logger.info("Product created successfully with id: {}", productDto.id());
        return ResponseEntity.created(location).body(productDto);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductDto> updateProduct(
            @PathVariable UUID id,
            @Valid @RequestBody NewProductDto productDto){
        logger.debug("Received request to update product with id: {}", id);

        ProductDto updatedProductDto = productService.updateProduct(id, productDto);

        logger.info("Product updated successfully with id: {}", updatedProductDto.id());
        return ResponseEntity.ok(updatedProductDto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable UUID id){
        logger.debug("Received request to delete product with id: {}", id);

        productService.deleteProduct(id);

        logger.info("Product deleted successfully with id: {}", id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<Page<ProductDto>> getAllProducts(Pageable pageable) {
        logger.debug("Received request to get all products (page: {}, size: {})",
                pageable.getPageNumber(), pageable.getPageSize());

        Page<ProductDto> productDtoPage = productService.getAllProducts(pageable);

        logger.info("Product page returned successfully");
        return ResponseEntity.ok(productDtoPage);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductDto> getProduct(@PathVariable UUID id) {
        logger.debug("Received request to get product with id: {}", id);

        ProductDto productDto = productService.getProductById(id);

        logger.info("Product returned successfully");
        return ResponseEntity.ok(productDto);
    }
}