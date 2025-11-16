package io.hohichh.marketplace.order.mapper;

import io.hohichh.marketplace.order.dto.product.NewProductDto;
import io.hohichh.marketplace.order.dto.product.ProductDto;
import io.hohichh.marketplace.order.model.Product;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    ProductDto toProductDto(Product product);

    Product toProduct(NewProductDto productDto);

    void updateProductFromDto(NewProductDto dto, @MappingTarget Product entity);
}
