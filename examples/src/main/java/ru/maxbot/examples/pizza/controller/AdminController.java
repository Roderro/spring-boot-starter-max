package ru.maxbot.examples.pizza.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import ru.maxbot.core.UpdateContext;
import ru.maxbot.examples.pizza.service.OrderService;
import ru.maxbot.starter.annotations.CommandRequest;
import ru.maxbot.starter.annotations.MaxController;

@MaxController
public class AdminController {

    private final OrderService orderService;

    public AdminController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @CommandRequest("kitchen")
    public void kitchen(UpdateContext ctx) {
        ctx.reply(orderService.describeKitchenQueue());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @CommandRequest(value = "stats", order = -10)
    public void stats(UpdateContext ctx) {
        ctx.reply("Статистика смены:\n"
                + "Заказов за сегодня: " + orderService.todayCount() + "\n"
                + "Активных черновиков: " + orderService.activeDraftsCount());
    }
}
