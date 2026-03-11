package com.example.repository;

import com.example.entity.Product;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Repository for {@link Product} data access operations. */
@ApplicationScoped
public class ProductRepository implements PanacheRepositoryBase<Product, UUID> {

  /**
   * Finds a product by its unique SKU.
   *
   * @param sku the SKU to look up
   * @return Optional containing the product if found
   */
  public Optional<Product> findBySku(String sku) {
    return find("sku", sku).firstResultOptional();
  }

  /**
   * Finds all active products sorted by name.
   *
   * @param page page index
   * @param size page size
   * @return list of active products
   */
  public List<Product> findAllActive(int page, int size) {
    return find("status", Sort.by("name"), Product.ProductStatus.ACTIVE)
        .page(Page.of(page, size))
        .list();
  }

  /**
   * Finds products by category.
   *
   * @param category the category name
   * @param page page index
   * @param size page size
   * @return list of products in the given category
   */
  public List<Product> findByCategory(String category, int page, int size) {
    return find(
            "category = ?1 and status = ?2",
            Sort.by("name"),
            category,
            Product.ProductStatus.ACTIVE)
        .page(Page.of(page, size))
        .list();
  }

  /**
   * Finds products within a price range.
   *
   * @param minPrice minimum price (inclusive)
   * @param maxPrice maximum price (inclusive)
   * @param page page index
   * @param size page size
   * @return list of products within the price range
   */
  public List<Product> findByPriceRange(
      BigDecimal minPrice, BigDecimal maxPrice, int page, int size) {
    return find(
            "price >= ?1 and price <= ?2 and status = ?3",
            Sort.by("price"),
            minPrice,
            maxPrice,
            Product.ProductStatus.ACTIVE)
        .page(Page.of(page, size))
        .list();
  }

  /**
   * Searches products by name or description.
   *
   * @param keyword search keyword
   * @param page page index
   * @param size page size
   * @return matching products
   */
  public List<Product> search(String keyword, int page, int size) {
    String likePattern = "%" + keyword.toLowerCase() + "%";

    return find("lower(name) like ?1 or lower(description) like ?1", Sort.by("name"), likePattern)
        .page(Page.of(page, size))
        .list();
  }

  /**
   * Finds products with low stock (below threshold).
   *
   * @param threshold minimum stock level
   * @return products with stock below threshold
   */
  public List<Product> findLowStock(int threshold) {
    return find("stockQuantity < ?1 and status = ?2", threshold, Product.ProductStatus.ACTIVE)
        .list();
  }

  /**
   * Checks whether a product with the given SKU exists.
   *
   * @param sku the SKU to check
   * @return true if the SKU is already taken
   */
  public boolean existsBySku(String sku) {
    return count("sku", sku) > 0;
  }
}
