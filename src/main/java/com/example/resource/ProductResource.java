package com.example.resource;

import com.example.dto.ProductDto;
import com.example.service.ProductService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import java.math.BigDecimal;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

/** REST resource for Product management. */
@Path("/api/v1/products")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Products", description = "Product catalog management")
@SecurityRequirement(name = "jwt")
public class ProductResource {

  private final ProductService productService;

  @Inject
  public ProductResource(ProductService productService) {
    this.productService = productService;
  }

  @GET
  @Operation(summary = "List products")
  @APIResponse(
      responseCode = "200",
      description = "Products retrieved",
      content = @Content(schema = @Schema(implementation = ProductDto.ProductPageResponse.class)))
  public Response listProducts(
      @QueryParam("page") @DefaultValue("0") int page,
      @QueryParam("size") @DefaultValue("20") int size) {
    return Response.ok(productService.listProducts(page, size)).build();
  }

  @GET
  @Path("/search")
  @Operation(summary = "Search products by name or description")
  @APIResponse(responseCode = "200", description = "Search results")
  public Response searchProducts(
      @QueryParam("q") String keyword,
      @QueryParam("page") @DefaultValue("0") int page,
      @QueryParam("size") @DefaultValue("20") int size) {
    List<ProductDto.ProductResponse> results = productService.searchProducts(keyword, page, size);

    return Response.ok(results).build();
  }

  @GET
  @Path("/category/{category}")
  @Operation(summary = "List products by category")
  public Response listByCategory(
      @PathParam("category") String category,
      @QueryParam("page") @DefaultValue("0") int page,
      @QueryParam("size") @DefaultValue("20") int size) {
    return Response.ok(productService.listByCategory(category, page, size)).build();
  }

  @GET
  @Path("/price-range")
  @Operation(summary = "Filter products by price range")
  public Response listByPriceRange(
      @QueryParam("min") @DefaultValue("0") BigDecimal min,
      @QueryParam("max") @DefaultValue("999999") BigDecimal max,
      @QueryParam("page") @DefaultValue("0") int page,
      @QueryParam("size") @DefaultValue("20") int size) {
    return Response.ok(productService.listByPriceRange(min, max, page, size)).build();
  }

  @GET
  @Path("/low-stock")
  @RolesAllowed({"ADMIN", "MODERATOR"})
  @Operation(summary = "Get products with low stock")
  public Response getLowStock(@QueryParam("threshold") @DefaultValue("10") int threshold) {
    return Response.ok(productService.getLowStockProducts(threshold)).build();
  }

  @GET
  @Path("/{id}")
  @Operation(summary = "Get product by ID")
  @APIResponses({
    @APIResponse(
        responseCode = "200",
        description = "Product found",
        content = @Content(schema = @Schema(implementation = ProductDto.ProductResponse.class))),
    @APIResponse(responseCode = "404", description = "Product not found")
  })
  public Response getProductById(@PathParam("id") UUID id) {
    return Response.ok(productService.findById(id)).build();
  }

  @POST
  @RolesAllowed({"ADMIN", "MODERATOR"})
  @Operation(summary = "Create a new product")
  @APIResponses({
    @APIResponse(responseCode = "201", description = "Product created"),
    @APIResponse(responseCode = "400", description = "Validation error"),
    @APIResponse(responseCode = "409", description = "SKU already exists")
  })
  public Response createProduct(
      @Valid ProductDto.CreateProductRequest request, @Context UriInfo uriInfo) {
    ProductDto.ProductResponse created = productService.createProduct(request);
    URI location = uriInfo.getAbsolutePathBuilder().path(created.id().toString()).build();

    return Response.created(location).entity(created).build();
  }

  @PUT
  @Path("/{id}")
  @RolesAllowed({"ADMIN", "MODERATOR"})
  @Operation(summary = "Update a product")
  @APIResponses({
    @APIResponse(responseCode = "200", description = "Product updated"),
    @APIResponse(responseCode = "404", description = "Product not found")
  })
  public Response updateProduct(
      @PathParam("id") UUID id, @Valid ProductDto.UpdateProductRequest request) {
    return Response.ok(productService.updateProduct(id, request)).build();
  }

  @DELETE
  @Path("/{id}")
  @RolesAllowed("ADMIN")
  @Operation(summary = "Delete a product")
  @APIResponses({
    @APIResponse(responseCode = "204", description = "Product deleted"),
    @APIResponse(responseCode = "404", description = "Product not found")
  })
  public Response deleteProduct(@PathParam("id") UUID id) {
    productService.deleteProduct(id);
    return Response.noContent().build();
  }
}
