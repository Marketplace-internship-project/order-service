package io.hohichh.marketplace.order.dto.item;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.UUID;

public record OrderItemDto(
        @NotNull UUID id,
        @NotNull UUID orderId,
        @NotNull UUID productId,
        @NotBlank String productName,
        @NotNull BigDecimal pricePerUnit,
        @NotNull Integer quantity
) implements Serializable {
}
