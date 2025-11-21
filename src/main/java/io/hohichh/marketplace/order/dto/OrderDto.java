package io.hohichh.marketplace.order.dto;

import io.hohichh.marketplace.order.model.order.Status;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.UUID;
import java.io.Serializable;


public record OrderDto(
        @NotNull UUID id,
        @NotNull UUID userId,
        @NotNull Status status,
        @NotNull LocalDate creationDate
) implements Serializable {
}
