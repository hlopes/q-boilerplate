package com.example.health;

import com.example.repository.UserRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.Liveness;
import org.eclipse.microprofile.health.Readiness;

/**
 * MicroProfile Health checks for liveness and readiness probes. These are consumed by Kubernetes /
 * OpenShift health endpoints at /health.
 */
public class HealthChecks {

  /** Liveness probe — confirms the application process is alive. */
  @Liveness
  @ApplicationScoped
  public static class ApplicationLivenessCheck implements HealthCheck {

    @Override
    public HealthCheckResponse call() {
      return HealthCheckResponse.named("application-live")
          .status(true)
          .withData("status", "UP")
          .build();
    }
  }

  /**
   * Readiness probe — confirms the application can serve requests (e.g., database is accessible).
   */
  @Readiness
  @ApplicationScoped
  public static class DatabaseReadinessCheck implements HealthCheck {

    private final UserRepository userRepository;

    @Inject
    public DatabaseReadinessCheck(UserRepository userRepository) {
      this.userRepository = userRepository;
    }

    @Override
    public HealthCheckResponse call() {
      try {
        // Lightweight query to verify DB connectivity
        long count = userRepository.count();

        return HealthCheckResponse.named("database-ready")
            .status(true)
            .withData("userCount", count)
            .build();
      } catch (Exception ex) {
        return HealthCheckResponse.named("database-ready")
            .status(false)
            .withData("error", ex.getMessage())
            .build();
      }
    }
  }
}
