package io.hohichh.marketplace.order.mapper;

import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = OrderMapper.class)
public interface OrderMapper {

}
