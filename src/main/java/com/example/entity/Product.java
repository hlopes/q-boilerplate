package com.example.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Product entity with price, stock, and category information. */
@Entity
@Table(
    name = "products",
    indexes = {
      @Index(name = "idx_products_sku", columnList = "sku", unique = true),
      @Index(name = "idx_products_category", columnList = "category"),
      @Index(name = "idx_products_status", columnList = "status")
    })
@Getter
@Setter
@NoArgsConstructor
public class Product extends PanacheEntityBase {

  @Id
  @GeneratedValue
  @Column(name = "id", updatable = false, nullable = false)
  public UUID id;

  @NotBlank(message = "Product name is required") @Size(max = 255) @Column(name = "name", nullable = false)
  public String name;

  @Column(name = "description", columnDefinition = "TEXT")
  public String description;

  @NotBlank(message = "SKU is required") @Size(max = 100) @Column(name = "sku", nullable = false, unique = true, length = 100)
  public String sku;

  @NotNull(message = "Price is required") @DecimalMin(value = "0.0", inclusive = false, message = "Price must be positive") @Digits(integer = 10, fraction = 2) @Column(name = "price", nullable = false, precision = 12, scale = 2)
  public BigDecimal price;

  @Min(value = 0, message = "Stock quantity cannot be negative") @Column(name = "stock_quantity", nullable = false)
  public Integer stockQuantity = 0;

  @Size(max = 100) @Column(name = "category", length = 100)
  public String category;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false)
  public ProductStatus status = ProductStatus.ACTIVE;

  @Column(name = "created_at", nullable = false, updatable = false)
  public LocalDateTime createdAt;

  @Column(name = "updated_at")
  public LocalDateTime updatedAt;

  @PrePersist
  protected void onCreate() {
    createdAt = LocalDateTime.now();
    updatedAt = LocalDateTime.now();
  }

  @PreUpdate
  protected void onUpdate() {
    updatedAt = LocalDateTime.now();
  }

  /** Product availability status */
  public enum ProductStatus {
    ACTIVE,
    INACTIVE,
    DISCONTINUED
  }
}
