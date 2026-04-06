package ru.maxbot.examples.pizza.controller;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import ru.maxbot.core.UpdateContext;
import ru.maxbot.core.model.User;
import ru.maxbot.examples.pizza.service.InMemoryRoleService;
import ru.maxbot.starter.annotations.CallbackRequest;
import ru.maxbot.starter.annotations.CommandRequest;
import ru.maxbot.starter.annotations.MaxBotExceptionHandler;
import ru.maxbot.starter.annotations.MaxController;

@MaxController
public class AccountController {

    private static final String STAFF_CODE = "pizza-staff";

    private final InMemoryRoleService roleService;

    public AccountController(InMemoryRoleService roleService) {
        this.roleService = roleService;
    }

    @CommandRequest(value = "profile", order = -50)
    public void profile(UpdateContext ctx) {
        User sender = ctx.sender();
        ctx.reply("Профиль:\n"
                + "Пользователь: " + roleService.describeUser(sender) + "\n"
                + "Роли: " + roleService.formatRoles(roleService.resolveRoles(sender)) + "\n\n"
                + "Для входа в режим сотрудника используйте /staff-login <код>.");
    }

    @CallbackRequest(prefix = "menu:profile")
    public void profileFromMenu(UpdateContext ctx) {
        ctx.answerCallback("Мой профиль");
        profile(ctx);
    }

    @CommandRequest("staff-login")
    public void staffLogin(UpdateContext ctx) {
        String[] parts = ctx.text().trim().split("\\s+", 2);
        if (parts.length < 2) {
            throw new IllegalArgumentException("Введите команду в формате /staff-login <код>.");
        }
        if (!STAFF_CODE.equals(parts[1])) {
            throw new IllegalArgumentException("Неверный код сотрудника.");
        }

        User sender = ctx.sender();
        roleService.grantAdminRole(sender);
        ctx.reply("Режим сотрудника включен.\n"
                + "Доступные команды: /staff, /kitchen, /stats, /staff-logout");
    }

    @CommandRequest("staff-logout")
    public void staffLogout(UpdateContext ctx) {
        User sender = ctx.sender();
        if (sender == null || sender.getUserId() == null) {
            throw new IllegalArgumentException("Требуется авторизованный пользователь.");
        }
        roleService.revokeRole(sender.getUserId(), "ADMIN");
        ctx.reply("Режим сотрудника отключен.");
    }

    @PreAuthorize("hasRole('ADMIN')")
    @CommandRequest("staff")
    public void staffCabinet(UpdateContext ctx) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User principal = (User) authentication.getPrincipal();
        List<String> roles = roleService.resolveRoles(principal);
        ctx.reply("Кабинет сотрудника:\n"
                + "Сотрудник: " + roleService.describeUser(principal) + "\n"
                + "Роли: " + roleService.formatRoles(roles) + "\n"
                + "Команды: /kitchen, /stats, /staff-logout");
    }

    @MaxBotExceptionHandler
    public void handleAccountErrors(IllegalArgumentException ex, UpdateContext ctx) {
        ctx.reply("Ошибка профиля: " + ex.getMessage());
    }
}
