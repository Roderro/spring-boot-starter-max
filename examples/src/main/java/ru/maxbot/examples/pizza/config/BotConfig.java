package ru.maxbot.examples.pizza.config;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import ru.maxbot.core.UpdateContext;
import ru.maxbot.core.exception.HandlerExceptionResolver;
import ru.maxbot.core.interceptor.HandlerInterceptor;
import ru.maxbot.core.model.User;
import ru.maxbot.examples.pizza.service.InMemoryRoleService;
import ru.maxbot.starter.security.BotAuthenticationConverter;

@Configuration
public class BotConfig {

    private static final Logger log = LoggerFactory.getLogger(BotConfig.class);
    private static final Authentication ANONYMOUS = new AnonymousAuthenticationToken(
            "pizza-demo-anonymous-key",
            "anonymousUser",
            AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS")
    );
    private static final String GENERIC_ERROR_MESSAGE =
            "Что-то пошло не так. Попробуйте еще раз или введите /start.";

    @Bean
    public BotAuthenticationConverter botAuthenticationConverter(InMemoryRoleService roleService) {
        return context -> {
            User sender = context.sender();
            if (sender == null) {
                return ANONYMOUS;
            }

            List<String> authorities = new ArrayList<>(roleService.resolveRoles(sender));

            return UsernamePasswordAuthenticationToken.authenticated(
                    sender,
                    "N/A",
                    AuthorityUtils.createAuthorityList(authorities)
            );
        };
    }

    @Bean
    public HandlerExceptionResolver botExceptionResolver() {
        return (ctx, handler, ex) -> {
            log.error("Bot error in chatId={}: {}", ctx.chatId(), ex.getMessage(), ex);
            ctx.clearState();
            ctx.reply(GENERIC_ERROR_MESSAGE);
            return true;
        };
    }

    @Bean
    public HandlerInterceptor loggingInterceptor() {
        return new HandlerInterceptor() {
            @Override
            public boolean preHandle(UpdateContext ctx) {
                log.info("[{}] {} from chatId={}",
                        ctx.update().type(), textPreview(ctx), ctx.chatId());
                return true;
            }

            private String textPreview(UpdateContext ctx) {
                if (ctx.callbackData() != null) {
                    return "callback=" + ctx.callbackData();
                }
                if (ctx.text() != null) {
                    return "text=" + truncate(ctx.text(), 50);
                }
                return "";
            }

            private String truncate(String s, int max) {
                return s.length() <= max ? s : s.substring(0, max) + "...";
            }
        };
    }
}
