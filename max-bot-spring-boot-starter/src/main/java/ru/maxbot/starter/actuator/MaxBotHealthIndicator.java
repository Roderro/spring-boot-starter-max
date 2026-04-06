package ru.maxbot.starter.actuator;

import ru.maxbot.core.api.MaxApi;
import ru.maxbot.core.model.User;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;

public class MaxBotHealthIndicator implements HealthIndicator {

    private final MaxApi maxApi;

    public MaxBotHealthIndicator(MaxApi maxApi) {
        this.maxApi = maxApi;
    }

    @Override
    public Health health() {
        try {
            User me = maxApi.getMe();
            return Health.up()
                    .withDetail("botName", me.getFirstName())
                    .withDetail("botUsername", me.getUsername())
                    .withDetail("botId", me.getUserId())
                    .build();
        } catch (Exception e) {
            return Health.down(e)
                    .withDetail("error", e.getMessage())
                    .build();
        }
    }
}


