package ru.maxbot.examples.pizza.controller;

import java.util.List;

import ru.maxbot.core.UpdateContext;
import ru.maxbot.core.outgoing.Button;
import ru.maxbot.core.outgoing.OutgoingMessage;
import ru.maxbot.examples.pizza.model.OrderDraft;
import ru.maxbot.examples.pizza.service.OrderService;
import ru.maxbot.starter.annotations.CallbackRequest;
import ru.maxbot.starter.annotations.CommandRequest;
import ru.maxbot.starter.annotations.MaxController;
import ru.maxbot.starter.annotations.MessageRequest;

@MaxController
public class OrderController {

    private static final List<List<Button>> PIZZA_KEYBOARD = List.of(
            List.of(
                    OutgoingMessage.callbackButton("Маргарита - 450", "pizza:margherita"),
                    OutgoingMessage.callbackButton("Пепперони - 550", "pizza:pepperoni")
            ),
            List.of(
                    OutgoingMessage.callbackButton("Четыре сыра - 650", "pizza:quattro"),
                    OutgoingMessage.callbackButton("Гавайская - 500", "pizza:hawaiian")
            ),
            List.of(OutgoingMessage.callbackButton("Назад", "menu:main"))
    );

    private static final List<List<Button>> SIZE_KEYBOARD = List.of(
            List.of(
                    OutgoingMessage.callbackButton("25 cm", "size:S"),
                    OutgoingMessage.callbackButton("30 cm (+30%)", "size:M"),
                    OutgoingMessage.callbackButton("35 cm (+60%)", "size:L")
            ),
            List.of(OutgoingMessage.callbackButton("Назад", "order:back_pizza"))
    );

    private static final List<List<Button>> CONFIRM_KEYBOARD = List.of(
            List.of(
                    OutgoingMessage.callbackButton("Подтвердить", "order:confirm"),
                    OutgoingMessage.callbackButton("Добавить комментарий", "order:add_comment")
            ),
            List.of(
                    OutgoingMessage.callbackButton("Изменить адрес", "order:back_address"),
                    OutgoingMessage.callbackButton("Отменить", "order:cancel")
            )
    );

    private static final List<List<Button>> BACK_MENU = List.of(
            List.of(OutgoingMessage.callbackButton("Вернуться в главное меню", "menu:main"))
    );

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @CallbackRequest(prefix = "menu:order")
    public void startFromMenu(UpdateContext ctx) {
        startOrderFlow(ctx);
    }

    @CommandRequest("order")
    public void startFromCommand(UpdateContext ctx) {
        startOrderFlow(ctx);
    }

    @CommandRequest("status")
    public void statusFromCommand(UpdateContext ctx) {
        ctx.reply(orderService.describeStatus(ctx.chatId()));
    }

    @CallbackRequest(prefix = "menu:status")
    public void statusFromMenu(UpdateContext ctx) {
        ctx.answerCallback("Статус заказа");
        ctx.reply(orderService.describeStatus(ctx.chatId()));
    }

    @CallbackRequest(prefix = "pizza:", state = "ORDER_PIZZA")
    public void choosePizza(UpdateContext ctx) {
        String pizza = ctx.callbackData().substring("pizza:".length());
        orderService.setPizza(ctx.chatId(), pizza);

        ctx.reply(OutgoingMessage.text("Выберите размер:")
                .keyboard(SIZE_KEYBOARD)
                .build());
        ctx.setState("ORDER_SIZE");
    }

    @CallbackRequest(prefix = "size:", state = "ORDER_SIZE")
    public void chooseSize(UpdateContext ctx) {
        String size = ctx.callbackData().substring("size:".length());
        orderService.setSize(ctx.chatId(), size);

        ctx.reply("Введите адрес доставки:");
        ctx.setState("ORDER_ADDRESS");
    }

    @CallbackRequest(prefix = "order:back_pizza", state = "ORDER_SIZE")
    public void backToPizza(UpdateContext ctx) {
        ctx.reply(OutgoingMessage.text("Выберите пиццу:")
                .keyboard(PIZZA_KEYBOARD)
                .build());
        ctx.setState("ORDER_PIZZA");
    }

    @MessageRequest(textRegex = ".{5,}", state = "ORDER_ADDRESS")
    public void enterAddress(UpdateContext ctx) {
        orderService.setAddress(ctx.chatId(), ctx.text());
        OrderDraft draft = orderService.getDraft(ctx.chatId());

        ctx.reply(OutgoingMessage.text("Проверьте заказ:\n" + draft.summary() + "\n\nВсе верно?")
                .keyboard(CONFIRM_KEYBOARD)
                .build());
        ctx.setState("ORDER_CONFIRM");
    }

    @MessageRequest(textRegex = ".{0,4}", state = "ORDER_ADDRESS")
    public void addressTooShort(UpdateContext ctx) {
        ctx.reply("Адрес слишком короткий. Введите полный адрес доставки:");
    }

    @CallbackRequest(prefix = "order:add_comment", state = "ORDER_CONFIRM")
    public void addComment(UpdateContext ctx) {
        ctx.reply("Добавьте комментарий для курьера. Например: подъезд 2, позвонить за 15 минут.");
        ctx.setState("ORDER_COMMENT");
    }

    @MessageRequest(textRegex = ".{3,}", state = "ORDER_COMMENT")
    public void saveComment(UpdateContext ctx) {
        orderService.setComment(ctx.chatId(), ctx.text());
        OrderDraft draft = orderService.getDraft(ctx.chatId());
        ctx.reply(OutgoingMessage.text("Комментарий сохранен.\n\n" + draft.summary() + "\n\nПодтвердить заказ?")
                .keyboard(CONFIRM_KEYBOARD)
                .build());
        ctx.setState("ORDER_CONFIRM");
    }

    @MessageRequest(textRegex = ".{0,2}", state = "ORDER_COMMENT")
    public void commentTooShort(UpdateContext ctx) {
        ctx.reply("Комментарий слишком короткий. Добавьте деталей или подтвердите заказ без комментария.");
    }

    @CallbackRequest(prefix = "order:back_address", state = "ORDER_CONFIRM")
    public void backToAddress(UpdateContext ctx) {
        ctx.reply("Введите новый адрес доставки:");
        ctx.setState("ORDER_ADDRESS");
    }

    @CallbackRequest(prefix = "order:confirm", state = "ORDER_CONFIRM")
    public void confirm(UpdateContext ctx) {
        String confirmation = orderService.placeOrder(ctx.chatId());
        ctx.clearState();
        ctx.reply(OutgoingMessage.text(confirmation)
                .keyboard(BACK_MENU)
                .build());
    }

    @CallbackRequest(prefix = "order:cancel")
    @CommandRequest("cancel")
    public void cancel(UpdateContext ctx) {
        orderService.cancelDraft(ctx.chatId());
        ctx.clearState();
        ctx.reply(OutgoingMessage.text("Заказ отменен.")
                .keyboard(BACK_MENU)
                .build());
    }

    private void startOrderFlow(UpdateContext ctx) {
        orderService.createDraft(ctx.chatId());
        ctx.reply(OutgoingMessage.text("Выберите пиццу:")
                .keyboard(PIZZA_KEYBOARD)
                .build());
        ctx.setState("ORDER_PIZZA");
    }
}
