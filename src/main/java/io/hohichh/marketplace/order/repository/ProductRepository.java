package io.hohichh.marketplace.order.repository;

import io.hohichh.marketplace.order.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ProductRepository extends JpaRepository<Product, UUID> {
}
