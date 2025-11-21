package io.hohichh.marketplace.order.mapper;

import io.hohichh.marketplace.order.dto.OrderDto;
import io.hohichh.marketplace.order.dto.OrderWithItemsDto;
import io.hohichh.marketplace.order.dto.UserDto;
import io.hohichh.marketplace.order.model.order.Order;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring", uses = {OrderItemMapper.class})
public interface OrderMapper {
    @Mapping(target = "id", source = "order.id")
    @Mapping(target = "userId", source = "order.userId")
    @Mapping(target = "status", source = "order.status")
    @Mapping(target = "creationDate", source = "order.creationDate")
    @Mapping(target = "orderItems", source = "order.orderItems")
    @Mapping(target = "userDto", source = "userDto")
    OrderWithItemsDto toDtoWithItems(Order order, UserDto userDto);

    List<OrderDto> toDtoList(List<Order> orders);

    OrderDto toDto(Order order);
}
