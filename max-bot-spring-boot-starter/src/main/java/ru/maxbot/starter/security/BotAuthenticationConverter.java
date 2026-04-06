package ru.maxbot.starter.security;

import ru.maxbot.core.UpdateContext;
import org.springframework.security.core.Authentication;

public interface BotAuthenticationConverter {

    Authentication convert(UpdateContext context);
}

