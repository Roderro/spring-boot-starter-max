package ru.maxbot.starter.inbound;

import ru.maxbot.core.api.MaxApi;
import ru.maxbot.core.dispatcher.UpdateDispatcher;
import ru.maxbot.core.mapper.UpdateMapper;
import ru.maxbot.core.model.Update;
import ru.maxbot.core.transport.IncomingUpdate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class WebhookUpdateEndpoint {

    private static final Logger log = LoggerFactory.getLogger(WebhookUpdateEndpoint.class);

    private final UpdateDispatcher dispatcher;
    private final UpdateMapper updateMapper;
    private final MaxApi maxApi;
    private final String secret;

    public WebhookUpdateEndpoint(UpdateDispatcher dispatcher, UpdateMapper updateMapper,
                             MaxApi maxApi, String webhookSecret) {
        this.dispatcher = dispatcher;
        this.updateMapper = updateMapper;
        this.maxApi = maxApi;
        this.secret = webhookSecret;
    }

    @PostMapping("${max.bot.webhook.path:/webhook}")
    public ResponseEntity<Void> handleUpdate(
            @RequestHeader(value = "X-Max-Bot-Api-Secret", required = false) String headerSecret,
            @RequestBody IncomingUpdate apiUpdate
    ) {
        if (secret != null && !secret.isEmpty() && !secret.equals(headerSecret)) {
            log.warn("Webhook rejected: invalid secret");
            return ResponseEntity.status(403).build();
        }

        try {
            Update update = updateMapper.map(apiUpdate);
            log.debug("Webhook received: type={}, chatId={}", update.type(), update.chatId());
            dispatcher.dispatch(maxApi, update);
        } catch (Exception e) {
            log.error("Error processing webhook update", e);
        }

        return ResponseEntity.ok().build();
    }
}


