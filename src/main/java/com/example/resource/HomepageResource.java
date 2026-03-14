package com.example.resource;

import com.example.entity.Tenant;
import com.example.entity.User;
import io.quarkus.oidc.IdToken;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import org.eclipse.microprofile.jwt.JsonWebToken;

@Path("/")
public class HomepageResource {

    @Inject
    @IdToken
    JsonWebToken idToken;

    @Inject
    Template dashboard;

    @GET
    public TemplateInstance home() {
        if (idToken == null || idToken.getClaim("email") == null) {
            return dashboard
                    .instance()
                    .data("userEmail", null)
                    .data("userPicture", null)
                    .data("tenantDomain", null);
        }

        String userEmail = idToken.getClaim("email");
        String userPicture = idToken.getClaim("picture");

        Tenant tenant = Tenant.getOrCreate(domainOf(userEmail));
        User.upsert(userEmail, userPicture, tenant);

        return dashboard
                .instance()
                .data("userEmail", userEmail)
                .data("userPicture", userPicture)
                .data("tenantDomain", tenant.domain);
    }

    private static String domainOf(String email) {
        int at = email.indexOf('@');

        return at > 0 ? email.substring(at + 1).toLowerCase() : "unknown";
    }
}
