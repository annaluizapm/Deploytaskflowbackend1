package com.taskflow.config;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

final class PostgresDataSourceCondition implements Condition {

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        String url = context.getEnvironment().getProperty("spring.datasource.url");
        return url != null
                && (url.startsWith("jdbc:postgresql://")
                || url.startsWith("postgres://")
                || url.startsWith("postgresql://"));
    }
}
