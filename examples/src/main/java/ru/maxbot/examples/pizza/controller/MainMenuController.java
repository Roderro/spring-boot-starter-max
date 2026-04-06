package ru.maxbot.examples.pizza.controller;

import java.util.List;

import ru.maxbot.core.UpdateContext;
import ru.maxbot.core.outgoing.Button;
import ru.maxbot.core.outgoing.OutgoingMessage;
import ru.maxbot.starter.annotations.BotStartedRequest;
import ru.maxbot.starter.annotations.CallbackRequest;
import ru.maxbot.starter.annotations.CommandRequest;
import ru.maxbot.starter.annotations.MaxController;

@MaxController
public class MainMenuController {

    private static final List<List<Button>> MAIN_MENU = List.of(
            List.of(OutgoingMessage.callbackButton("Заказать пиццу", "menu:order")),
            List.of(
                    OutgoingMessage.callbackButton("Статус заказа", "menu:status"),
                    OutgoingMessage.callbackButton("Мой профиль", "menu:profile")
            ),
            List.of(
                    OutgoingMessage.callbackButton("Меню", "menu:catalog"),
                    OutgoingMessage.callbackButton("Контакты", "menu:contacts")
            ),
            List.of(OutgoingMessage.callbackButton("Помощь", "menu:help"))
    );

    @BotStartedRequest
    public void onBotStarted(UpdateContext ctx) {
        showMainMenu(ctx, "Добро пожаловать в PizzaBot.\nВыберите действие:");
    }

    @CommandRequest(value = "start", order = -100)
    public void start(UpdateContext ctx) {
        ctx.clearState();
        showMainMenu(ctx, "Главное меню:");
    }

    @CommandRequest("menu")
    public void menu(UpdateContext ctx) {
        showMainMenu(ctx, "Главное меню:");
    }

    @CommandRequest("help")
    public void help(UpdateContext ctx) {
        ctx.reply("Доступные команды:\n"
                + "/start - главное меню\n"
                + "/order - начать новый заказ\n"
                + "/cancel - отменить текущий заказ\n"
                + "/status - показать статус заказа\n"
                + "/profile - показать профиль\n"
                + "/staff-login <код> - включить режим сотрудника\n"
                + "/staff-logout - выйти из режима сотрудника\n"
                + "/staff - открыть кабинет сотрудника\n"
                + "/kitchen - очередь кухни для сотрудников\n"
                + "/stats - краткая статистика смены\n"
                + "/help - помощь");
    }

    @CallbackRequest(prefix = "menu:main")
    public void backToMain(UpdateContext ctx) {
        ctx.clearState();
        showMainMenu(ctx, "Главное меню:");
    }

    @CallbackRequest(prefix = "menu:help")
    public void helpFromMenu(UpdateContext ctx) {
        ctx.answerCallback("Помощь");
        help(ctx);
    }

    @CallbackRequest(prefix = "menu:catalog")
    public void catalog(UpdateContext ctx) {
        ctx.answerCallback("Меню");
        ctx.reply("Меню:\n"
                + "Маргарита - от 450 руб.\n"
                + "Пепперони - от 550 руб.\n"
                + "Четыре сыра - от 650 руб.\n"
                + "Гавайская - от 500 руб.\n\n"
                + "Размеры: 25 / 30 / 35 см\n"
                + "Среднее время доставки: 40 минут.");
    }

    @CallbackRequest(prefix = "menu:contacts")
    public void contacts(UpdateContext ctx) {
        ctx.answerCallback("Контакты");
        ctx.reply("Телефон: +7-999-123-45-67\n"
                + "Режим работы: 10:00 - 23:00\n"
                + "Адрес: улица Пиццы, 1");
    }

    @CallbackRequest(prefix = "menu:profile")
    public void profileFromMenu(UpdateContext ctx) {
        ctx.answerCallback("Мой профиль");
        ctx.reply("Откройте профиль командой /profile.");
    }

    private void showMainMenu(UpdateContext ctx, String text) {
        ctx.reply(OutgoingMessage.text(text)
                .keyboard(MAIN_MENU)
                .build());
    }
}
