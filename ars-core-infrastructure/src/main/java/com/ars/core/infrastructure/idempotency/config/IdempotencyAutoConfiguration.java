package com.ars.core.infrastructure.idempotency.config;

import com.ars.core.infrastructure.idempotency.aspect.IdempotencyAspect;
import com.ars.core.infrastructure.idempotency.entity.ProcessedEvent;
import com.ars.core.infrastructure.idempotency.repo.ProcessedEventRepository;
import com.ars.core.infrastructure.idempotency.support.SpelKeyResolver;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@AutoConfiguration
@ConditionalOnClass(ProcessedEventRepository.class)
@EnableConfigurationProperties(IdempotencyProperties.class)
@ConditionalOnProperty(prefix = "ars.idempotency", name = "enabled", havingValue = "true", matchIfMissing = true)
@EntityScan(basePackageClasses = ProcessedEvent.class)
@EnableJpaRepositories(basePackageClasses = ProcessedEventRepository.class)
public class IdempotencyAutoConfiguration {

    @Bean
    public SpelKeyResolver spelKeyResolver() {
        return new SpelKeyResolver();
    }

    @Bean
    public IdempotencyAspect idempotencyAspect(ProcessedEventRepository repository, SpelKeyResolver spel) {
        return new IdempotencyAspect(repository, spel);
    }
}
