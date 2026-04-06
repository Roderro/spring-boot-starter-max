package ru.maxbot.examples.pizza.model;

import java.time.LocalDateTime;

public class OrderDraft {

    private String pizza;
    private String size;
    private String address;
    private String comment;
    private final LocalDateTime createdAt = LocalDateTime.now();

    public String pizza() {
        return pizza;
    }

    public void setPizza(String pizza) {
        this.pizza = pizza;
    }

    public String size() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String address() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String comment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public LocalDateTime createdAt() {
        return createdAt;
    }

    public int price() {
        int base = switch (pizza) {
            case "margherita" -> 450;
            case "pepperoni" -> 550;
            case "quattro" -> 650;
            case "hawaiian" -> 500;
            default -> 500;
        };
        return switch (size) {
            case "M" -> (int) (base * 1.3);
            case "L" -> (int) (base * 1.6);
            default -> base;
        };
    }

    public String pizzaName() {
        return switch (pizza) {
            case "margherita" -> "Маргарита";
            case "pepperoni" -> "Пепперони";
            case "quattro" -> "Четыре сыра";
            case "hawaiian" -> "Гавайская";
            default -> pizza;
        };
    }

    public String sizeName() {
        return switch (size) {
            case "S" -> "25 см";
            case "M" -> "30 см";
            case "L" -> "35 см";
            default -> size;
        };
    }

    public String progressSummary() {
        StringBuilder builder = new StringBuilder("Текущий заказ:\n");
        builder.append("Пицца: ").append(pizza == null ? "не выбрана" : pizzaName()).append('\n');
        builder.append("Размер: ").append(size == null ? "не выбран" : sizeName()).append('\n');
        builder.append("Адрес: ").append(address == null ? "не указан" : address);
        if (comment != null && !comment.isBlank()) {
            builder.append('\n').append("Комментарий: ").append(comment);
        }
        if (pizza != null && size != null) {
            builder.append('\n').append("Предварительная сумма: ").append(price()).append(" руб.");
        }
        return builder.toString();
    }

    public String summary() {
        StringBuilder builder = new StringBuilder();
        builder.append(pizzaName()).append(" (").append(sizeName()).append(')');
        builder.append('\n').append("Адрес: ").append(address);
        if (comment != null && !comment.isBlank()) {
            builder.append('\n').append("Комментарий: ").append(comment);
        }
        builder.append('\n').append("Итого: ").append(price()).append(" руб.");
        return builder.toString();
    }
}
