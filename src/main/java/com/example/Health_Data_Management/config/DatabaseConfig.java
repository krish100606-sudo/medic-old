package com.example.Health_Data_Management.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.net.URI;

@Configuration
public class DatabaseConfig {

    private static final Logger log = LoggerFactory.getLogger(DatabaseConfig.class);

    @Value("${spring.datasource.url:}")
    private String configuredUrl;

    @Value("${spring.datasource.username:}")
    private String configuredUsername;

    @Value("${spring.datasource.password:}")
    private String configuredPassword;

    @Bean
    @Primary
    public DataSource dataSource() {
        // Check DATABASE_URL first (Render/Heroku standard), then configured URL
        String rawUrl = System.getenv("DATABASE_URL");
        if (rawUrl == null || rawUrl.trim().isEmpty()) {
            rawUrl = System.getenv("SPRING_DATASOURCE_URL");
        }
        if (rawUrl == null || rawUrl.trim().isEmpty()) {
            rawUrl = configuredUrl;
        }

        HikariConfig config = new HikariConfig();
        String jdbcUrl = null;
        String user = configuredUsername;
        String pass = configuredPassword;

        if (rawUrl != null && !rawUrl.trim().isEmpty()) {
            rawUrl = rawUrl.trim();
            log.info("Processing database connection string...");

            try {
                if (rawUrl.startsWith("postgres://") || rawUrl.startsWith("postgresql://")) {
                    // Convert postgres://user:pass@host:port/db to jdbc:postgresql://host:port/db
                    URI uri = new URI(rawUrl);
                    String host = uri.getHost();
                    int port = uri.getPort() == -1 ? 5432 : uri.getPort();
                    String path = uri.getPath();

                    jdbcUrl = "jdbc:postgresql://" + host + ":" + port + path;

                    if (uri.getUserInfo() != null) {
                        String[] parts = uri.getUserInfo().split(":", 2);
                        user = parts[0];
                        if (parts.length > 1) {
                            pass = parts[1];
                        }
                    }
                    log.info("Converted cloud PostgreSQL URL to JDBC URL: {}", jdbcUrl);
                } else if (rawUrl.startsWith("jdbc:postgresql://")) {
                    jdbcUrl = rawUrl;
                } else if (!rawUrl.startsWith("jdbc:")) {
                    jdbcUrl = "jdbc:" + rawUrl;
                } else {
                    jdbcUrl = rawUrl;
                }
            } catch (Exception e) {
                log.error("Error parsing database URL: {}. Falling back to default JDBC configuration.", rawUrl, e);
                jdbcUrl = rawUrl.startsWith("jdbc:") ? rawUrl : "jdbc:" + rawUrl;
            }
        }

        // If no valid URL could be determined, fallback to in-memory H2
        if (jdbcUrl == null || jdbcUrl.trim().isEmpty()) {
            log.warn("No database URL provided. Starting with in-memory H2 database.");
            jdbcUrl = "jdbc:h2:mem:health_data;DB_CLOSE_DELAY=-1;MODE=PostgreSQL";
            config.setDriverClassName("org.h2.Driver");
            user = "sa";
            pass = "";
        } else {
            config.setDriverClassName("org.postgresql.Driver");
        }

        config.setJdbcUrl(jdbcUrl);
        if (user != null && !user.isEmpty()) {
            config.setUsername(user);
        }
        if (pass != null && !pass.isEmpty()) {
            config.setPassword(pass);
        }

        config.setMaximumPoolSize(10);
        config.setMinimumIdle(2);
        config.setConnectionTimeout(30000);
        config.setIdleTimeout(600000);
        config.setMaxLifetime(1800000);

        return new HikariDataSource(config);
    }
}
