package ru.maxbot.starter;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.http.HttpClient;
import java.time.Duration;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.maxbot.core.api.MaxApi;
import ru.maxbot.core.client.MaxBotHttpClient;
import ru.maxbot.core.impl.DefaultMaxApi;
import ru.maxbot.core.retry.RetryPolicy;

@Configuration
@EnableConfigurationProperties(MaxBotProperties.class)
@ConditionalOnProperty(name = "max.bot.access-token")
public class MaxBotAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public HttpClient httpClient() {
        return HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(30))
                .build();
    }

    @Bean
    @ConditionalOnMissingBean
    public ObjectMapper maxBotObjectMapper(ObjectProvider<ObjectMapper> objectMapperProvider) {
        ObjectMapper objectMapper = objectMapperProvider.getIfAvailable(MaxBotAutoConfiguration::createObjectMapper);
        return objectMapper.copy()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .disable(DeserializationFeature.READ_DATE_TIMESTAMPS_AS_NANOSECONDS);
    }

    public static ObjectMapper createObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.disable(DeserializationFeature.READ_DATE_TIMESTAMPS_AS_NANOSECONDS);
        return mapper;
    }

    @Bean
    @ConditionalOnMissingBean
    public MaxBotHttpClient maxBotHttpClient(MaxBotProperties props,
                                             HttpClient httpClient,
                                             ObjectMapper maxBotObjectMapper) {
        return new MaxBotHttpClient(props.getAccessToken(), httpClient, maxBotObjectMapper,
                props.getRateLimit().getRequestsPerSecond());
    }

    @Bean
    @ConditionalOnMissingBean
    public RetryPolicy retryPolicy() {
        return RetryPolicy.defaultPolicy();
    }

    @Bean
    @ConditionalOnMissingBean
    public MaxApi maxApi(MaxBotHttpClient client,
                         ObjectProvider<RetryPolicy> retryPolicyProvider) {
        RetryPolicy retryPolicy = retryPolicyProvider.getIfAvailable(RetryPolicy::defaultPolicy);
        return new DefaultMaxApi(client, retryPolicy);
    }
}

