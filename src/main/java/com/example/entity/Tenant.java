package com.example.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Tenant entity with name and description information. */
@Entity
@Table(name = "tenants")
@Getter
@Setter
@NoArgsConstructor
public class Tenant extends PanacheEntityBase {

    @Id
    @GeneratedValue
    @Column(name = "id", updatable = false, nullable = false)
    public UUID id;

    @NotBlank(message = "Tenant domain is required") @Size(max = 255) @Column(name = "domain", unique = true, nullable = false)
    public String domain;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    public TenantStatus status = TenantStatus.ACTIVE;

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

    public enum TenantStatus {
        ACTIVE,
        INACTIVE,
        SUSPENDED
    }

    @Transactional
    public static Tenant getOrCreate(String domain) {
        Tenant tenant = find("domain", domain.toLowerCase()).firstResult();

        if (tenant == null) {
            tenant = new Tenant();
            tenant.domain = domain.toLowerCase();
            tenant.persist();
        }

        return tenant;
    }
}
