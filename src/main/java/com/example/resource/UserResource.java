package com.example.resource;

import com.example.config.AppConfig;
import com.example.dto.UserDto;
import com.example.service.UserService;
import io.quarkus.logging.Log;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

/** REST resource exposing User management endpoints. */
@Path("/api/v1/users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Users", description = "User management operations")
@SecurityRequirement(name = "jwt")
public class UserResource {

  private final UserService userService;
  private final AppConfig appConfig;

  @Inject
  public UserResource(UserService userService, AppConfig appConfig) {
    this.userService = userService;
    this.appConfig = appConfig;
  }

  // -------------------------------------------------------
  // GET /api/v1/users
  // -------------------------------------------------------

  @GET
  @RolesAllowed({"ADMIN", "MODERATOR"})
  @Operation(summary = "List users", description = "Returns a paginated list of active users")
  @APIResponses({
    @APIResponse(
        responseCode = "200",
        description = "Users retrieved successfully",
        content = @Content(schema = @Schema(implementation = UserDto.UserPageResponse.class))),
    @APIResponse(responseCode = "401", description = "Unauthorized"),
    @APIResponse(responseCode = "403", description = "Forbidden")
  })
  public Response listUsers(
      @Parameter(description = "Page index (0-based)") @QueryParam("page") @DefaultValue("0")
          int page,
      @Parameter(description = "Page size") @QueryParam("size") @DefaultValue("20") int size) {

    Log.debugf("GET /users page=%d size=%d", page, size);
    UserDto.UserPageResponse result =
        userService.listUsers(page, Math.min(size, appConfig.pagination().maxPageSize()));

    return Response.ok(result).build();
  }

  // -------------------------------------------------------
  // GET /api/v1/users/search
  // -------------------------------------------------------

  @GET
  @Path("/search")
  @RolesAllowed({"ADMIN", "MODERATOR"})
  @Operation(summary = "Search users", description = "Searches users by username or email keyword")
  @APIResponse(
      responseCode = "200",
      description = "Search results",
      content = @Content(schema = @Schema(implementation = UserDto.UserResponse.class)))
  public Response searchUsers(
      @Parameter(required = true) @QueryParam("q") String keyword,
      @QueryParam("page") @DefaultValue("0") int page,
      @QueryParam("size") @DefaultValue("20") int size) {

    List<UserDto.UserResponse> results = userService.searchUsers(keyword, page, size);

    return Response.ok(results).build();
  }

  // -------------------------------------------------------
  // GET /api/v1/users/{id}
  // -------------------------------------------------------

  @GET
  @Path("/{id}")
  @RolesAllowed({"ADMIN", "MODERATOR", "USER"})
  @Operation(summary = "Get user by ID")
  @APIResponses({
    @APIResponse(
        responseCode = "200",
        description = "User found",
        content = @Content(schema = @Schema(implementation = UserDto.UserResponse.class))),
    @APIResponse(responseCode = "404", description = "User not found")
  })
  public Response getUserById(
      @Parameter(description = "User ID", required = true) @PathParam("id") UUID id) {

    UserDto.UserResponse user = userService.findById(id);

    return Response.ok(user).build();
  }

  // -------------------------------------------------------
  // POST /api/v1/users
  // -------------------------------------------------------

  @POST
  @RolesAllowed("ADMIN")
  @Operation(summary = "Create a new user")
  @APIResponses({
    @APIResponse(
        responseCode = "201",
        description = "User created",
        content = @Content(schema = @Schema(implementation = UserDto.UserResponse.class))),
    @APIResponse(responseCode = "400", description = "Validation error"),
    @APIResponse(responseCode = "409", description = "Email or username already taken")
  })
  public Response createUser(@Valid UserDto.CreateUserRequest request, @Context UriInfo uriInfo) {

    Log.infof("POST /users username=%s", request.username());
    UserDto.UserResponse created = userService.createUser(request);
    URI location = uriInfo.getAbsolutePathBuilder().path(created.id().toString()).build();

    return Response.created(location).entity(created).build();
  }

  // -------------------------------------------------------
  // PUT /api/v1/users/{id}
  // -------------------------------------------------------

  @PUT
  @Path("/{id}")
  @RolesAllowed({"ADMIN", "USER"})
  @Operation(summary = "Update a user")
  @APIResponses({
    @APIResponse(
        responseCode = "200",
        description = "User updated",
        content = @Content(schema = @Schema(implementation = UserDto.UserResponse.class))),
    @APIResponse(responseCode = "404", description = "User not found"),
    @APIResponse(responseCode = "409", description = "Email already taken")
  })
  public Response updateUser(@PathParam("id") UUID id, @Valid UserDto.UpdateUserRequest request) {

    UserDto.UserResponse updated = userService.updateUser(id, request);

    return Response.ok(updated).build();
  }

  // -------------------------------------------------------
  // PATCH /api/v1/users/{id}/deactivate
  // -------------------------------------------------------

  @PATCH
  @Path("/{id}/deactivate")
  @RolesAllowed("ADMIN")
  @Operation(summary = "Deactivate a user account")
  @APIResponses({
    @APIResponse(responseCode = "204", description = "User deactivated"),
    @APIResponse(responseCode = "404", description = "User not found")
  })
  public Response deactivateUser(@PathParam("id") UUID id) {
    userService.deactivateUser(id);

    return Response.noContent().build();
  }

  // -------------------------------------------------------
  // DELETE /api/v1/users/{id}
  // -------------------------------------------------------

  @DELETE
  @Path("/{id}")
  @RolesAllowed("ADMIN")
  @Operation(summary = "Delete a user")
  @APIResponses({
    @APIResponse(responseCode = "204", description = "User deleted"),
    @APIResponse(responseCode = "404", description = "User not found")
  })
  public Response deleteUser(@PathParam("id") UUID id) {
    userService.deleteUser(id);

    return Response.noContent().build();
  }
}
