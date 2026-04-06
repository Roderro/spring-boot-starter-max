package ru.maxbot.examples.minimal;

import ru.maxbot.core.UpdateContext;
import ru.maxbot.starter.annotations.BotStartedRequest;
import ru.maxbot.starter.annotations.CommandRequest;
import ru.maxbot.starter.annotations.MaxController;
import ru.maxbot.starter.annotations.MessageRequest;

@MaxController
public class MinimalBotController {

    @BotStartedRequest
    public void onStart(UpdateContext ctx) {
        ctx.reply("Привет! Это минимальный пример бота для MAX.");
    }

    @CommandRequest("help")
    public void help(UpdateContext ctx) {
        ctx.reply("Команды:\n/help\n/echo <текст>");
    }

    @CommandRequest("echo")
    public void echoCommand(UpdateContext ctx) {
        String text = ctx.text();
        String echoed = text.length() > 5 ? text.substring(5).trim() : "";
        ctx.reply(echoed.isEmpty() ? "После /echo укажите текст." : echoed);
    }

    @MessageRequest(textRegex = ".*")
    public void echoMessage(UpdateContext ctx) {
        ctx.reply("Эхо: " + ctx.text());
    }
}

