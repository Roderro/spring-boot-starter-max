package ru.maxbot.examples.security;

import org.springframework.security.access.prepost.PreAuthorize;
import ru.maxbot.core.UpdateContext;
import ru.maxbot.core.model.User;
import ru.maxbot.starter.annotations.CommandRequest;
import ru.maxbot.starter.annotations.MaxController;

@MaxController
public class SecurityBotController {

    @CommandRequest("whoami")
    public void whoAmI(UpdateContext ctx) {
        User sender = ctx.sender();
        if (sender == null) {
            ctx.reply("Пользователь не определен.");
            return;
        }
        ctx.reply("Вы вошли как @" + sender.getUsername() + " (userId=" + sender.getUserId() + ")");
    }

    @PreAuthorize("hasRole('ADMIN')")
    @CommandRequest("admin")
    public void admin(UpdateContext ctx) {
        ctx.reply("Команда /admin доступна. Проверка роли пройдена.");
    }
}

