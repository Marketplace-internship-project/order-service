package io.hohichh.marketplace.order.repository;

import io.hohichh.marketplace.order.model.order.Order;
import io.hohichh.marketplace.order.model.order.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, UUID> {
    @Query("SELECT o FROM Order o WHERE o.status IN :statuses")
    public Page<Order> findOrdersByStatuses(List<Status> statuses, Pageable pageable);

    @Query("SELECT o FROM Order o WHERE o.id IN :ids")
    public List<Order> findOrdersByIds(List<UUID> ids);
}
