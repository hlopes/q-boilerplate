package com.example.mapper;

import com.example.dto.ProductDto;
import com.example.entity.Product;
import org.mapstruct.*;

/**
 * MapStruct mapper for {@link Product} entities and DTOs.
 */
@Mapper(
    componentModel = "cdi",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface ProductMapper {

    /**
     * Maps a Product entity to a ProductResponse DTO.
     *
     * @param product the source entity
     * @return the response DTO
     */
    ProductDto.ProductResponse toResponse(Product product);

    /**
     * Maps a CreateProductRequest to a Product entity.
     *
     * @param request the create request
     * @return the new Product entity
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", constant = "ACTIVE")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Product toEntity(ProductDto.CreateProductRequest request);

    /**
     * Updates an existing Product entity from an UpdateProductRequest.
     *
     * @param request the update request
     * @param product the entity to update
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "sku", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(ProductDto.UpdateProductRequest request, @MappingTarget Product product);
}
