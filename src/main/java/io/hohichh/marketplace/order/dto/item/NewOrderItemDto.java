package io.hohichh.marketplace.order.dto.item;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.UUID;


public record NewOrderItemDto(
        @NotNull UUID productId,
        @NotNull Integer quantity
) implements Serializable {
}
