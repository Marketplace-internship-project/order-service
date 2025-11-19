package io.hohichh.marketplace.order.security;

import io.hohichh.marketplace.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component("orderSecurity")
@RequiredArgsConstructor
public class OrderSecurity {

    private final OrderRepository orderRepository;

    public boolean isOrderOwner(UUID orderId, Authentication authentication) {
        String currentUserId = authentication.getName();

        return orderRepository.findById(orderId)
                .map(order -> order.getUserId().toString().equals(currentUserId))
                .orElse(false);
    }

    public boolean isAccountOwner(UUID targetUserId, Authentication authentication) {
        String currentUserId = authentication.getName();
        return targetUserId.toString().equals(currentUserId);
    }
}