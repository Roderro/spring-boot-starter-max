package ru.maxbot.examples.pizza.controller;

import org.springframework.core.annotation.Order;
import org.springframework.security.authorization.AuthorizationDeniedException;
import ru.maxbot.core.UpdateContext;
import ru.maxbot.starter.annotations.MaxBotControllerAdvice;
import ru.maxbot.starter.annotations.MaxBotExceptionHandler;

@Order(10)
@MaxBotControllerAdvice
public class GlobalErrorControllerAdvice {

    @MaxBotExceptionHandler(IllegalArgumentException.class)
    public void handleIllegalArgument(UpdateContext ctx, IllegalArgumentException ex) {
        ctx.reply("Не удалось выполнить запрос: " + ex.getMessage());
    }

    @MaxBotExceptionHandler(AuthorizationDeniedException.class)
    public void handleDenied(UpdateContext ctx, AuthorizationDeniedException ex) {
        ctx.reply("Этот раздел доступен только сотрудникам.");
    }
}
