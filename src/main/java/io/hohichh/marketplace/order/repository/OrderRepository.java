package io.hohichh.marketplace.order.repository;

import io.hohichh.marketplace.order.model.order.Order;
import io.hohichh.marketplace.order.model.order.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, UUID> {

//мб понадобиться
//    @Query("SELECT o FROM Order o WHERE " +
//            "(:#{#ids == null} = true OR o.id IN :ids) AND " +
//            "(:#{#statuses == null} = true OR o.status IN :statuses)")
    @Query("SELECT o FROM Order o WHERE " +
            "(:ids IS NULL OR o.id IN :ids) AND " +
            "(:statuses IS NULL OR o.status IN :statuses)")
    Page<Order> findAllByFilter(
            @Param("ids") List<UUID> ids,
            @Param("statuses") List<Status> statuses,
            Pageable pageable
    );

    List<Order> findOrdersByUserId(UUID userId);
}
