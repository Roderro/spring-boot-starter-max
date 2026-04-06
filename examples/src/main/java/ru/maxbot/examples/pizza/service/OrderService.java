package ru.maxbot.examples.pizza.service;

import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.stereotype.Service;
import ru.maxbot.examples.pizza.model.OrderDraft;

@Service
public class OrderService {

    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    private final ConcurrentHashMap<Long, OrderDraft> drafts = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Long, String> lastStatuses = new ConcurrentHashMap<>();
    private final AtomicInteger completedOrders = new AtomicInteger(0);

    public void createDraft(long chatId) {
        drafts.put(chatId, new OrderDraft());
    }

    public OrderDraft getDraft(long chatId) {
        return drafts.get(chatId);
    }

    public void setPizza(long chatId, String pizza) {
        OrderDraft draft = drafts.get(chatId);
        if (draft != null) {
            draft.setPizza(pizza);
        }
    }

    public void setSize(long chatId, String size) {
        OrderDraft draft = drafts.get(chatId);
        if (draft != null) {
            draft.setSize(size);
        }
    }

    public void setAddress(long chatId, String address) {
        OrderDraft draft = drafts.get(chatId);
        if (draft != null) {
            draft.setAddress(address);
        }
    }

    public void setComment(long chatId, String comment) {
        OrderDraft draft = drafts.get(chatId);
        if (draft != null) {
            draft.setComment(comment);
        }
    }

    public String placeOrder(long chatId) {
        OrderDraft draft = drafts.remove(chatId);
        if (draft != null) {
            completedOrders.incrementAndGet();
            String status = "Заказ принят в " + draft.createdAt().format(TIME_FORMAT)
                    + ". Курьер будет примерно через 40 минут.\n\n" + draft.summary();
            lastStatuses.put(chatId, status);
            return status;
        }
        return "Активный заказ не найден.";
    }

    public void cancelDraft(long chatId) {
        drafts.remove(chatId);
    }

    public int todayCount() {
        return completedOrders.get();
    }

    public int activeDraftsCount() {
        return drafts.size();
    }

    public String describeStatus(long chatId) {
        OrderDraft draft = drafts.get(chatId);
        if (draft != null) {
            return "Заказ сейчас оформляется.\n\n" + draft.progressSummary();
        }
        return lastStatuses.getOrDefault(chatId, "Активных заказов пока нет. Начните с /order.");
    }

    public String describeKitchenQueue() {
        if (drafts.isEmpty()) {
            return "Очередь кухни пуста.";
        }

        StringBuilder builder = new StringBuilder("Очередь кухни:\n");
        int index = 1;
        for (Map.Entry<Long, OrderDraft> entry : drafts.entrySet()) {
            builder.append(index++)
                    .append(". chatId=")
                    .append(entry.getKey())
                    .append(" -> ")
                    .append(entry.getValue().progressSummary())
                    .append("\n\n");
        }
        return builder.toString().trim();
    }
}
