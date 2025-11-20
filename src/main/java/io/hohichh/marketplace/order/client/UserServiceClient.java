package io.hohichh.marketplace.order.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import org.springframework.web.bind.annotation.PathVariable;
import io.hohichh.marketplace.order.dto.UserDto;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.UUID;

@FeignClient(
        name = "user-service",
        url = "${application.config.user-url}",
        path = "/api",
        configuration = io.hohichh.marketplace.order.config.FeignConfig.class)
public interface UserServiceClient {

    @GetMapping("/v1/users/{id}")
    UserDto getUserById(@RequestHeader("Authorization") String token, @PathVariable("id") UUID id);
}
