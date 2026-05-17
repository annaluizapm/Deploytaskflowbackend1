package com.taskflow.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;

@Configuration
public class DataSourceConfig {

    @Bean
    @Primary
    @ConditionalOnExpression(
            "T(java.lang.System).getenv('SPRING_DATASOURCE_URL') != null "
                    + "|| T(java.lang.System).getenv('DATABASE_URL') != null "
                    + "|| T(java.lang.System).getenv('POSTGRES_URL') != null"
    )
    public DataSource renderDataSource() {
        RenderDatabaseUrl.DatabaseConnection connection = RenderDatabaseUrl.connectionFromEnvironment();
        if (connection == null) {
            throw new IllegalStateException("Invalid PostgreSQL database URL");
        }

        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(connection.jdbcUrl());
        dataSource.setUsername(connection.username());
        dataSource.setPassword(connection.password());
        dataSource.setMaximumPoolSize(readInt("SPRING_DATASOURCE_HIKARI_MAXIMUM_POOL_SIZE", 10));
        dataSource.setMinimumIdle(readInt("SPRING_DATASOURCE_HIKARI_MINIMUM_IDLE", 2));
        dataSource.setConnectionTimeout(readLong("SPRING_DATASOURCE_HIKARI_CONNECTION_TIMEOUT", 30000));
        dataSource.setValidationTimeout(readLong("SPRING_DATASOURCE_HIKARI_VALIDATION_TIMEOUT", 5000));
        dataSource.setIdleTimeout(readLong("SPRING_DATASOURCE_HIKARI_IDLE_TIMEOUT", 600000));
        dataSource.setMaxLifetime(readLong("SPRING_DATASOURCE_HIKARI_MAX_LIFETIME", 1800000));
        return dataSource;
    }

    private static int readInt(String name, int defaultValue) {
        String value = System.getenv(name);
        return value == null || value.isBlank() ? defaultValue : Integer.parseInt(value);
    }

    private static long readLong(String name, long defaultValue) {
        String value = System.getenv(name);
        return value == null || value.isBlank() ? defaultValue : Long.parseLong(value);
    }
}
