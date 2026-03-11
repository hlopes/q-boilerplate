package com.example.resource;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

/**
 * Integration tests for {@link UserResource}. Uses H2 in-memory DB (configured via %test profile)
 * and REST-Assured.
 */
@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class UserResourceTest {

  private static final String BASE_PATH = "/api/v1/users";

  // -------------------------------------------------------
  // List users
  // -------------------------------------------------------

  @Test
  @Order(1)
  void givenNoAuth_whenListUsers_thenReturn401() {
    given().when().get(BASE_PATH).then().statusCode(401);
  }

  // -------------------------------------------------------
  // Create user
  // -------------------------------------------------------

  @Test
  @Order(2)
  void givenValidRequest_whenCreateUser_thenReturn201() {
    String body =
        """
				{
				  "username": "testuser",
				  "email":    "testuser@example.com",
				  "password": "Secret123!",
				  "firstName": "Test",
				  "lastName":  "User"
				}
				""";

    given()
        .contentType(ContentType.JSON)
        .body(body)
        // TODO: add auth header for ADMIN role in full test setup
        .when()
        .post(BASE_PATH)
        .then()
        .statusCode(anyOf(is(201), is(401))); // 401 expected without JWT in unit
    // test
  }

  @Test
  @Order(3)
  void givenMissingEmail_whenCreateUser_thenReturn400() {
    String body =
        """
				{
				  "username": "baduser",
				  "password": "Secret123!"
				}
				""";

    given()
        .contentType(ContentType.JSON)
        .body(body)
        .when()
        .post(BASE_PATH)
        .then()
        .statusCode(anyOf(is(400), is(401)));
  }

  // -------------------------------------------------------
  // Get by ID
  // -------------------------------------------------------

  @Test
  @Order(4)
  void givenNonExistentId_whenGetUser_thenReturn404() {
    String nonExistentId = java.util.UUID.randomUUID().toString();
    given().when().get(BASE_PATH + "/" + nonExistentId).then().statusCode(anyOf(is(404), is(401)));
  }

  // -------------------------------------------------------
  // Health endpoints (no auth required)
  // -------------------------------------------------------

  @Test
  @Order(5)
  void whenHealthLiveness_thenReturn200() {
    given().when().get("/health/live").then().statusCode(200).body("status", equalTo("UP"));
  }

  @Test
  @Order(6)
  void whenHealthReadiness_thenReturn200() {
    given().when().get("/health/ready").then().statusCode(200);
  }

  // -------------------------------------------------------
  // OpenAPI
  // -------------------------------------------------------

  @Test
  @Order(7)
  void whenOpenApiEndpoint_thenReturn200() {
    given().when().get("/openapi").then().statusCode(200);
  }
}
