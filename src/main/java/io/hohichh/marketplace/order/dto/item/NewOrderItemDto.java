package io.hohichh.marketplace.order.dto.item;

import jakarta.validation.constraints.NotNull;

import java.io.Serializable;
import java.util.UUID;


public record NewOrderItemDto(
        @NotNull UUID productId,
        @NotNull Integer quantity
) implements Serializable {
}
