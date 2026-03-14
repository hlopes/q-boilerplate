package com.example.dto;

import com.example.entity.Product;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.NoArgsConstructor;

/** Data Transfer Objects for Product operations. */
@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public final class ProductDto {

    /** Request DTO for creating a new product */
    public record CreateProductRequest(
            @NotBlank(message = "Product name is required") @Size(max = 255) String name,

            String description,

            @NotBlank(message = "SKU is required") @Size(max = 100) String sku,

            @NotNull(message = "Price is required") @DecimalMin(value = "0.0", inclusive = false, message = "Price must be positive") @Digits(integer = 10, fraction = 2) BigDecimal price,

            @Min(value = 0, message = "Stock quantity cannot be negative") Integer stockQuantity,

            @Size(max = 100) String category) {}

    /** Request DTO for updating an existing product */
    public record UpdateProductRequest(
            @Size(max = 255) String name,
            String description,

            @DecimalMin(value = "0.0", inclusive = false, message = "Price must be positive") @Digits(integer = 10, fraction = 2) BigDecimal price,

            @Min(value = 0, message = "Stock quantity cannot be negative") Integer stockQuantity,

            @Size(max = 100) String category,
            Product.ProductStatus status) {}

    /** Full product response DTO */
    public record ProductResponse(
            UUID id,
            String name,
            String description,
            String sku,
            BigDecimal price,
            Integer stockQuantity,
            String category,
            Product.ProductStatus status,
            LocalDateTime createdAt,
            LocalDateTime updatedAt) {}

    /** Paginated product list response */
    public record ProductPageResponse(
            List<ProductResponse> content, int page, int size, long totalElements, int totalPages) {}
}
