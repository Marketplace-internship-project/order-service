package io.hohichh.marketplace.order.service;

import io.hohichh.marketplace.order.dto.product.NewProductDto;
import io.hohichh.marketplace.order.dto.product.ProductDto;
import io.hohichh.marketplace.order.exception.ResourceNotFoundException;
import io.hohichh.marketplace.order.mapper.ProductMapper;
import io.hohichh.marketplace.order.model.Product;
import io.hohichh.marketplace.order.repository.ProductRepository;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;


@AllArgsConstructor
@Service
public class ProductServiceImpl implements ProductService {
    private ProductRepository productRepository;
    private ProductMapper productMapper;

    private static final Logger logger = LoggerFactory.getLogger(ProductServiceImpl.class);

    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public ProductDto createProduct(NewProductDto product) {
        logger.debug("Attemp to create new product");

        Product newProduct = productRepository.save(
                productMapper.toProduct(product));

        logger.info("New product created successfully");
        return productMapper.toProductDto(newProduct);
    }

    @Override
    @Transactional
    @CacheEvict(value="products", key="#id")
    @PreAuthorize("hasRole('ADMIN')")
    public ProductDto updateProduct(UUID id, NewProductDto product) {
        logger.debug("Attemp to update product");

        Product productToUpd = productRepository.findById(id)
                .orElseThrow(() -> {
                    logger.error("Product with id {} not found", id);
                    return new ResourceNotFoundException("Product with id " + id + " not found");
                });

        productMapper.updateProductFromDto(product, productToUpd);
        Product updProduct = productRepository.save(productToUpd);

        logger.info("Product updated successfully");
        return productMapper.toProductDto(updProduct);
    }


    @Override
    @Transactional
    @CacheEvict(value = "products", key = "#id")
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteProduct(UUID id) {
        logger.debug("Attempt to delete product with id {}", id);

        if (!productRepository.existsById(id)) {
            logger.error("Product with id {} not found, deletion failed", id);
            throw new ResourceNotFoundException("Product with id " + id + " not found");
        }

        productRepository.deleteById(id);
        logger.info("Product with id {} deleted successfully", id);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductDto> getAllProducts(Pageable pageable) {
        logger.debug("Attempt to retrieve all products (paginated: page={}, size={})",
                pageable.getPageNumber(), pageable.getPageSize());

        Page<Product> productPage = productRepository.findAll(pageable);

        logger.info("Retrieved {} products on page {}/{}",
                productPage.getNumberOfElements(), pageable.getPageNumber(), productPage.getTotalPages());

        return productPage.map(productMapper::toProductDto);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "products", key="#id")
    public ProductDto getProductById(UUID id) {
        logger.debug("Attempt to retrieve product by id {}", id);

        Product product = productRepository.findById(id).orElseThrow(
                () -> {
                    logger.error("Product with id {} not found", id);
                    return new ResourceNotFoundException("Product with id " + id + " not found");
                }
        );

        logger.info("Product with id {} found", id);
        return productMapper.toProductDto(product);
    }
}