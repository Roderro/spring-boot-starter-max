package ru.maxbot.examples.fsm;

import ru.maxbot.core.UpdateContext;
import ru.maxbot.starter.annotations.CommandRequest;
import ru.maxbot.starter.annotations.MaxController;
import ru.maxbot.starter.annotations.MessageRequest;

@MaxController
public class FsmBotController {

    @CommandRequest("survey")
    public void startSurvey(UpdateContext ctx) {
        ctx.reply("Как вас зовут?");
        ctx.setState("WAIT_NAME");
    }

    @MessageRequest(textRegex = ".{2,}", state = "WAIT_NAME")
    public void receiveName(UpdateContext ctx) {
        ctx.reply("Приятно познакомиться, " + ctx.text() + ". Сколько уведомлений отправить?");
        ctx.setState("WAIT_COUNT");
    }

    @MessageRequest(textRegex = "\\d{1,2}", state = "WAIT_COUNT")
    public void receiveCount(UpdateContext ctx) {
        ctx.reply("Сценарий завершен. Будет отправлено уведомлений: " + ctx.text());
        ctx.clearState();
    }

    @MessageRequest(textRegex = ".*", state = "WAIT_COUNT")
    public void invalidCount(UpdateContext ctx) {
        ctx.reply("Введите целое число от 0 до 99.");
    }

    @CommandRequest("cancel")
    public void cancel(UpdateContext ctx) {
        ctx.clearState();
        ctx.reply("Диалог отменен.");
    }
}

