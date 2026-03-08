package com.example.config;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;

/**
 * Type-safe application configuration using SmallRye Config mappings.
 * Values are read from application.properties under the "app" prefix.
 */
@ConfigMapping(prefix = "app")
public interface AppConfig {

    /** Pagination settings */
    Pagination pagination();

    /** Security settings */
    Security security();

    interface Pagination {
        @WithDefault("20")
        int defaultPageSize();

        @WithDefault("100")
        int maxPageSize();
    }

    interface Security {
        @WithDefault("60")
        long tokenExpiryMinutes();
    }
}
