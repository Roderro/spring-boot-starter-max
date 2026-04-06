package ru.maxbot.starter.actuator;

import ru.maxbot.core.api.MaxApi;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@AutoConfiguration
@ConditionalOnBean(MaxApi.class)
public class MaxBotActuatorAutoConfiguration {

    @Configuration
    @ConditionalOnClass(HealthIndicator.class)
    static class HealthConfig {

        @Bean
        @ConditionalOnMissingBean(name = "maxBotHealthIndicator")
        public MaxBotHealthIndicator maxBotHealthIndicator(MaxApi maxApi) {
            return new MaxBotHealthIndicator(maxApi);
        }
    }

    @Configuration
    @ConditionalOnClass(MeterRegistry.class)
    static class MetricsConfig {

        @Bean
        @ConditionalOnMissingBean(MetricsHandlerInterceptor.class)
        @ConditionalOnBean(MeterRegistry.class)
        public MetricsHandlerInterceptor metricsHandlerInterceptor(MeterRegistry meterRegistry) {
            return new MetricsHandlerInterceptor(meterRegistry);
        }
    }
}


