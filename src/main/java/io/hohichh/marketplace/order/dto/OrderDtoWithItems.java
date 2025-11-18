package io.hohichh.marketplace.order.dto;

import io.hohichh.marketplace.order.dto.item.OrderItemDto;
import io.hohichh.marketplace.order.model.order.Status;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record OrderDtoWithItems(
        @NotNull UUID id,
        @NotNull UUID userId,
        @NotNull Status status,
        @NotNull LocalDate creationDate,
        @Valid List<OrderItemDto> orderItems
) implements Serializable {
}
