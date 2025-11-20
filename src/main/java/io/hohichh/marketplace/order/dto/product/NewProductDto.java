package io.hohichh.marketplace.order.dto.product;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;
import java.math.BigDecimal;

public record NewProductDto(
        @NotBlank String name,
        @NotNull BigDecimal price) implements Serializable {
}
