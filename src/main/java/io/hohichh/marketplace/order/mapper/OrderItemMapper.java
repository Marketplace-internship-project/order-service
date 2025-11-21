package io.hohichh.marketplace.order.mapper;

import io.hohichh.marketplace.order.dto.item.NewOrderItemDto;
import io.hohichh.marketplace.order.dto.item.OrderItemDto;
import io.hohichh.marketplace.order.dto.product.ProductDto;
import io.hohichh.marketplace.order.model.OrderItem;
import io.hohichh.marketplace.order.model.order.Order;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;



@Mapper(componentModel = "spring")
public interface OrderItemMapper {
    @Mapping(target="id", ignore = true)
    @Mapping(target="order", source="order")
    @Mapping(target="productId", source = "product.id")
    @Mapping(target = "productName", source = "product.name")
    @Mapping(target = "pricePerUnit", source = "product.price")
    @Mapping(target = "quantity", source = "orderItemDto.quantity")
    OrderItem toOrderItem(NewOrderItemDto orderItemDto, Order order, ProductDto product);

    @Mapping(target = "orderId", source = "order.id")
    OrderItemDto toOrderItemDto(OrderItem item);
}
