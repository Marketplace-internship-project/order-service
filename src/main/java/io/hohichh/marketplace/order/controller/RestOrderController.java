package io.hohichh.marketplace.order.controller;

import io.hohichh.marketplace.order.dto.NewStatusOrderDto;
import io.hohichh.marketplace.order.dto.OrderDto;
import io.hohichh.marketplace.order.dto.OrderWithItemsDto;
import io.hohichh.marketplace.order.dto.item.NewOrderItemDto;
import io.hohichh.marketplace.order.model.order.Status;
import io.hohichh.marketplace.order.service.OrderService;
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
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/orders")
@AllArgsConstructor
public class RestOrderController {
    private final OrderService orderService;

    private static final Logger log = LoggerFactory.getLogger(RestOrderController.class);

    @PostMapping
    public ResponseEntity<OrderWithItemsDto> createOrder
            (@RequestBody List<NewOrderItemDto> items) {
        log.debug("Get POST request to create order");

        OrderWithItemsDto order = orderService.createOrder(items);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(order.id())
                .toUri();

        log.info("Create order request processed successfully");
        return ResponseEntity.created(location).body(order);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<OrderWithItemsDto> updateOrderStatus(
            @PathVariable UUID id,
            @Valid @RequestBody NewStatusOrderDto newStatusOrder
            ){
        log.debug("Get PATCH request to update order");

        OrderWithItemsDto order;
        if(newStatusOrder.status().equals(Status.CANCELLED)){
            order = orderService.cancelOrder(id);
        } else{
            order = orderService.updateOrderStatus(id, newStatusOrder);
        }

        log.info("Update order request processed successfully");
        return ResponseEntity.ok(order);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrder(@PathVariable UUID id) {
        log.debug("Get DELETE request to delete order");

        orderService.deleteOrder(id);

        log.info("Delete order request processed successfully");
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderWithItemsDto> getOrder(@PathVariable UUID id) {
        log.debug("Get GET request to get order");

        OrderWithItemsDto order = orderService.getOrderById(id);

        log.info("Get order request processed successfully");
        return ResponseEntity.ok(order);
    }

    @GetMapping(params="user-id")
    public ResponseEntity<List<OrderDto>> getOrdersByUserId(
            @RequestParam("user-id") UUID userId
    ) {
        log.debug("Get GET request to get orders by user id");

        List<OrderDto> orders = orderService.getOrdersByUserId(userId);

        log.info("Get orders by user id request processed successfully");
        return ResponseEntity.ok(orders);
    }

    @GetMapping
    public ResponseEntity<List<OrderDto>> getOrdersByIds(@RequestBody List<UUID> ids) {
        log.debug("Get GET request to get orders by id");

        List<OrderDto> orders = orderService.getOrdersByIds(ids);

        log.info("Get orders by id request processed successfully");
        return ResponseEntity.ok(orders);
    }

    @GetMapping
    public ResponseEntity<Page<OrderDto>> getOrderByStatuses (
            @RequestBody List<Status> statuses,
            Pageable pageable){
        log.debug("Get GET request to get orders by status");

        Page<OrderDto> orders = orderService.getOrderByStatuses(pageable, statuses);

        log.info("Get orders by status request processed successfully");
        return ResponseEntity.ok(orders);
    }



}
