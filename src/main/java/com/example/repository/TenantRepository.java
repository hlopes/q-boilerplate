package com.example.repository;

import com.example.entity.Tenant;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.UUID;

/** Repository for Tenant entities using PanacheRepository. */
@ApplicationScoped
public class TenantRepository implements PanacheRepositoryBase<Tenant, UUID> {
    // Add custom query methods here if needed
}
