package ru.maxbot.core.handler;

import ru.maxbot.core.UpdateContext;
import ru.maxbot.core.model.Update;

public interface Handler {

    boolean supports(Update update);

    void handle(UpdateContext ctx);
}


