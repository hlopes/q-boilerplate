package com.example.resource;

import io.quarkus.oidc.OidcSession;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;
import java.net.URI;

@Path("/logout")
public class LogoutResource {

    @Inject
    OidcSession oidcSession;

    @GET
    public Response logout() {
        oidcSession.logout().await().indefinitely();

        return Response.seeOther(URI.create("/")).build();
    }
}
