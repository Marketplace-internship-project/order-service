package io.hohichh.marketplace.order.repository;

import io.hohichh.marketplace.order.model.Item;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ItemRepository extends JpaRepository<Item, UUID> {
}
