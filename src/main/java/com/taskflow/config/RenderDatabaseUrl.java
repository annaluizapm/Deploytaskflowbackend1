package com.taskflow.config;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

public final class RenderDatabaseUrl {

    private RenderDatabaseUrl() {
    }

    public static void applyIfPresent() {
        if (hasText(System.getenv("SPRING_DATASOURCE_URL"))) {
            return;
        }

        String databaseUrl = firstPresent(System.getenv("DATABASE_URL"), System.getenv("POSTGRES_URL"));
        if (!hasText(databaseUrl)) {
            return;
        }

        if (databaseUrl.startsWith("jdbc:postgresql://")) {
            System.setProperty("spring.datasource.url", databaseUrl);
            return;
        }

        URI uri = URI.create(databaseUrl);
        String scheme = uri.getScheme();
        if (!"postgres".equals(scheme) && !"postgresql".equals(scheme)) {
            return;
        }

        String jdbcUrl = "jdbc:postgresql://" + uri.getHost() + ":" + resolvePort(uri) + uri.getPath();
        if (hasText(uri.getQuery())) {
            jdbcUrl += "?" + uri.getQuery();
        }

        System.setProperty("spring.datasource.url", jdbcUrl);

        String userInfo = uri.getRawUserInfo();
        if (hasText(userInfo)) {
            String[] credentials = userInfo.split(":", 2);
            System.setProperty("spring.datasource.username", decode(credentials[0]));
            if (credentials.length > 1) {
                System.setProperty("spring.datasource.password", decode(credentials[1]));
            }
        }
    }

    private static int resolvePort(URI uri) {
        return uri.getPort() > 0 ? uri.getPort() : 5432;
    }

    private static String firstPresent(String first, String second) {
        return hasText(first) ? first : second;
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private static String decode(String value) {
        return URLDecoder.decode(value, StandardCharsets.UTF_8);
    }
}
