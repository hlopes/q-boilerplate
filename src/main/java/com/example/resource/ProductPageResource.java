package com.example.resource;

import com.example.entity.Product;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.math.BigDecimal;
import java.net.URI;
import java.util.List;
import java.util.UUID;

@Path("/products")
@Produces(MediaType.TEXT_HTML)
public class ProductPageResource {

    private static final URI PRODUCTS_URI = URI.create("/products");

    @Inject
    Template products;

    @Inject
    Template product_detail;

    @Inject
    Template error;

    @GET
    public TemplateInstance list() {
        List<Product> allProducts = Product.listAll();
        return products.data("products", allProducts);
    }

    @GET
    @Path("/{id}")
    public Response get(@PathParam("id") UUID id) {
        Product product = Product.findById(id);
        if (product == null) {
            TemplateInstance errorPage = error.data("status", 404)
                    .data("error", "Not Found")
                    .data("message", "The requested product could not be found.");
            return Response.status(Response.Status.NOT_FOUND).entity(errorPage).build();
        }
        return Response.ok(product_detail.data("product", product)).build();
    }

    @POST
    @Transactional
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response create(
            @FormParam("name") String name,
            @FormParam("sku") String sku,
            @FormParam("price") String priceStr,
            @FormParam("stockQuantity") Integer stockQuantity) {
        Product product = new Product();
        product.name = name;
        product.sku = sku;
        product.price = new BigDecimal(priceStr);
        product.stockQuantity = stockQuantity;
        product.persist();

        return Response.seeOther(PRODUCTS_URI).build();
    }

    @POST
    @Path("/{id}/delete")
    @Transactional
    public Response delete(@PathParam("id") UUID id) {
        Product.deleteById(id);

        return Response.seeOther(PRODUCTS_URI).build();
    }
}
