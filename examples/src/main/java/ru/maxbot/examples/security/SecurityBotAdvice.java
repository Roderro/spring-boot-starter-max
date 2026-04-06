package ru.maxbot.examples.security;

import org.springframework.security.authorization.AuthorizationDeniedException;
import ru.maxbot.core.UpdateContext;
import ru.maxbot.starter.annotations.MaxBotControllerAdvice;
import ru.maxbot.starter.annotations.MaxBotExceptionHandler;

@MaxBotControllerAdvice
public class SecurityBotAdvice {

    @MaxBotExceptionHandler(AuthorizationDeniedException.class)
    public void handleDenied(UpdateContext ctx, AuthorizationDeniedException ex) {
        ctx.reply("Доступ запрещен. Для команды /admin требуется роль ADMIN.");
    }
}

