package ru.maxbot.starter.inbound;

import ru.maxbot.core.api.MaxApi;
import ru.maxbot.core.client.MaxBotHttpClient;
import ru.maxbot.core.dispatcher.UpdateDispatcher;
import ru.maxbot.core.mapper.UpdateMapper;
import ru.maxbot.starter.MaxBotProperties;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@ConditionalOnBean(MaxApi.class)
public class MaxBotInboundAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public UpdateMapper updateMapper() {
        return new UpdateMapper();
    }

    @Bean
    @ConditionalOnProperty(name = "max.bot.webhook.enabled", havingValue = "true")
    public WebhookUpdateEndpoint webhookUpdateEndpoint(UpdateDispatcher dispatcher, UpdateMapper updateMapper,
                                               MaxApi maxApi, MaxBotProperties props) {
        return new WebhookUpdateEndpoint(dispatcher, updateMapper, maxApi,
                props.getWebhook().getSecret());
    }

    @Bean
    @ConditionalOnProperty(name = "max.bot.webhook.enabled", havingValue = "false", matchIfMissing = true)
    public LongPollingUpdateReceiver longPollingUpdateReceiver(MaxBotHttpClient client, UpdateDispatcher dispatcher,
                                              UpdateMapper updateMapper, MaxApi maxApi,
                                              MaxBotProperties props) {
        return new LongPollingUpdateReceiver(client, dispatcher, updateMapper, maxApi,
                props.getPolling().getTimeout(), props.getPolling().getLimit());
    }
}


