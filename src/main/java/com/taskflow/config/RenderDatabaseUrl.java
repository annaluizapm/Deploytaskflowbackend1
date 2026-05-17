package com.taskflow.config;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

public final class RenderDatabaseUrl {

    private RenderDatabaseUrl() {
    }

    public static void applyIfPresent() {
        String databaseUrl = rawUrl();
        if (!hasText(databaseUrl)) {
            return;
        }

        DatabaseConnection connection = parse(databaseUrl);
        if (connection == null) {
            return;
        }

        System.setProperty("spring.datasource.url", connection.jdbcUrl());
        if (hasText(connection.username())) {
            System.setProperty("spring.datasource.username", connection.username());
        }
        if (hasText(connection.password())) {
            System.setProperty("spring.datasource.password", connection.password());
        }
    }

    public static DatabaseConnection connectionFromEnvironment() {
        String databaseUrl = rawUrl();
        return hasText(databaseUrl) ? parse(databaseUrl) : null;
    }

    public static DatabaseConnection connectionFromUrl(String databaseUrl, String username, String password) {
        if (!hasText(databaseUrl)) {
            return null;
        }

        DatabaseConnection connection = parse(databaseUrl);
        if (connection == null) {
            return null;
        }

        return new DatabaseConnection(
                connection.jdbcUrl(),
                hasText(connection.username()) ? connection.username() : username,
                hasText(connection.password()) ? connection.password() : password
        );
    }

    private static DatabaseConnection parse(String databaseUrl) {
        if (databaseUrl.startsWith("jdbc:postgresql://")) {
            return new DatabaseConnection(
                    databaseUrl,
                    System.getenv("SPRING_DATASOURCE_USERNAME"),
                    System.getenv("SPRING_DATASOURCE_PASSWORD")
            );
        }

        URI uri = URI.create(databaseUrl);
        String scheme = uri.getScheme();
        if (!"postgres".equals(scheme) && !"postgresql".equals(scheme)) {
            return null;
        }

        String jdbcUrl = "jdbc:postgresql://" + uri.getHost() + ":" + resolvePort(uri) + uri.getPath();
        if (hasText(uri.getQuery())) {
            jdbcUrl += "?" + uri.getQuery();
        }

        String username = null;
        String password = null;
        String userInfo = uri.getRawUserInfo();
        if (hasText(userInfo)) {
            String[] credentials = userInfo.split(":", 2);
            username = decode(credentials[0]);
            if (credentials.length > 1) {
                password = decode(credentials[1]);
            }
        }

        return new DatabaseConnection(jdbcUrl, username, password);
    }

    private static String rawUrl() {
        return firstPresent(
                System.getenv("SPRING_DATASOURCE_URL"),
                System.getenv("DATABASE_URL"),
                System.getenv("POSTGRES_URL")
        );
    }

    private static int resolvePort(URI uri) {
        return uri.getPort() > 0 ? uri.getPort() : 5432;
    }

    private static String firstPresent(String... values) {
        for (String value : values) {
            if (hasText(value)) {
                return value;
            }
        }

        return null;
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private static String decode(String value) {
        return URLDecoder.decode(value, StandardCharsets.UTF_8);
    }

    public record DatabaseConnection(String jdbcUrl, String username, String password) {
    }
}
