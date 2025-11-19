package io.hohichh.marketplace.order.dto;

import io.hohichh.marketplace.order.model.order.Status;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;

public record NewStatusOrderDto(
       @NotNull Status status
) implements Serializable {
}
