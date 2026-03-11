package com.example.service;

import com.example.config.AppConfig;
import com.example.dto.ProductDto;
import com.example.entity.Product;
import com.example.exception.ConflictException;
import com.example.exception.ResourceNotFoundException;
import com.example.mapper.ProductMapper;
import com.example.repository.ProductRepository;
import io.quarkus.cache.CacheInvalidate;
import io.quarkus.cache.CacheResult;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;

/** Service layer for Product business logic. */
@ApplicationScoped
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class ProductService {

  private static final String PRODUCT_CACHE = "product-cache";

  private final ProductRepository productRepository;
  private final ProductMapper productMapper;
  private final AppConfig appConfig;

  /**
   * Retrieves a product by ID with caching.
   *
   * @param id product id
   * @return product response DTO
   */
  @CacheResult(cacheName = PRODUCT_CACHE)
  public ProductDto.ProductResponse findById(UUID id) {
    return productRepository
        .findByIdOptional(id)
        .map(productMapper::toResponse)
        .orElseThrow(() -> new ResourceNotFoundException("Product", id));
  }

  /**
   * Lists all active products with pagination.
   *
   * @param page page index
   * @param size page size
   * @return paginated product response
   */
  public ProductDto.ProductPageResponse listProducts(int page, int size) {
    int effectiveSize = Math.min(size, appConfig.pagination().maxPageSize());
    List<ProductDto.ProductResponse> products =
        productRepository.findAllActive(page, effectiveSize).stream()
            .map(productMapper::toResponse)
            .toList();

    long total = productRepository.count("status", Product.ProductStatus.ACTIVE);
    int totalPages = (int) Math.ceil((double) total / effectiveSize);

    return new ProductDto.ProductPageResponse(products, page, effectiveSize, total, totalPages);
  }

  /**
   * Lists products filtered by category.
   *
   * @param category the category name
   * @param page page index
   * @param size page size
   * @return matching products
   */
  public List<ProductDto.ProductResponse> listByCategory(String category, int page, int size) {
    int effectiveSize = Math.min(size, appConfig.pagination().maxPageSize());
    return productRepository.findByCategory(category, page, effectiveSize).stream()
        .map(productMapper::toResponse)
        .toList();
  }

  /**
   * Lists products within a price range.
   *
   * @param minPrice minimum price
   * @param maxPrice maximum price
   * @param page page index
   * @param size page size
   * @return matching products
   */
  public List<ProductDto.ProductResponse> listByPriceRange(
      BigDecimal minPrice, BigDecimal maxPrice, int page, int size) {
    int effectiveSize = Math.min(size, appConfig.pagination().maxPageSize());
    return productRepository.findByPriceRange(minPrice, maxPrice, page, effectiveSize).stream()
        .map(productMapper::toResponse)
        .toList();
  }

  /**
   * Creates a new product.
   *
   * @param request the create request DTO
   * @return the created product response DTO
   */
  @Transactional
  public ProductDto.ProductResponse createProduct(ProductDto.CreateProductRequest request) {
    Log.infof("Creating product with sku=%s", request.sku());

    if (productRepository.existsBySku(request.sku())) {
      throw new ConflictException("Product with SKU '" + request.sku() + "' already exists");
    }

    Product product = productMapper.toEntity(request);
    if (product.stockQuantity == null) {
      product.stockQuantity = 0;
    }

    productRepository.persist(product);
    Log.infof("Product created with id=%s", product.id);

    return productMapper.toResponse(product);
  }

  /**
   * Updates an existing product.
   *
   * @param id product id
   * @param request update request DTO
   * @return updated product response DTO
   */
  @Transactional
  @CacheInvalidate(cacheName = PRODUCT_CACHE)
  public ProductDto.ProductResponse updateProduct(
      UUID id, ProductDto.UpdateProductRequest request) {
    Product product =
        productRepository
            .findByIdOptional(id)
            .orElseThrow(() -> new ResourceNotFoundException("Product", id));

    productMapper.updateEntity(request, product);

    return productMapper.toResponse(product);
  }

  /**
   * Deletes a product by ID.
   *
   * @param id product id
   */
  @Transactional
  @CacheInvalidate(cacheName = PRODUCT_CACHE)
  public void deleteProduct(UUID id) {
    boolean deleted = productRepository.deleteById(id);
    if (!deleted) {
      throw new ResourceNotFoundException("Product", id);
    }
  }

  /**
   * Searches products by name or description.
   *
   * @param keyword search keyword
   * @param page page index
   * @param size page size
   * @return matching products
   */
  public List<ProductDto.ProductResponse> searchProducts(String keyword, int page, int size) {
    int effectiveSize = Math.min(size, appConfig.pagination().maxPageSize());
    return productRepository.search(keyword, page, effectiveSize).stream()
        .map(productMapper::toResponse)
        .toList();
  }

  /**
   * Returns products with stock below the given threshold.
   *
   * @param threshold minimum stock level to alert on
   * @return low-stock products
   */
  public List<ProductDto.ProductResponse> getLowStockProducts(int threshold) {
    return productRepository.findLowStock(threshold).stream()
        .map(productMapper::toResponse)
        .toList();
  }
}
