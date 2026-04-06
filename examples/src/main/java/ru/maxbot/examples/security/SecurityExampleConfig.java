package ru.maxbot.examples.security;

import java.util.ArrayList;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import ru.maxbot.core.UpdateContext;
import ru.maxbot.core.model.User;
import ru.maxbot.starter.security.BotAuthenticationConverter;

@Configuration
public class SecurityExampleConfig {

    private static final Authentication ANONYMOUS = new AnonymousAuthenticationToken(
            "security-example-anonymous-key",
            "anonymousUser",
            AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS")
    );

    @Bean
    public BotAuthenticationConverter botAuthenticationConverter() {
        return context -> {
            User sender = context.sender();
            if (sender == null) {
                return ANONYMOUS;
            }

            List<String> authorities = new ArrayList<>();
            authorities.add("ROLE_USER");

            if ("admin".equalsIgnoreCase(sender.getUsername())
                    || Long.valueOf(1L).equals(sender.getUserId())) {
                authorities.add("ROLE_ADMIN");
            }

            return UsernamePasswordAuthenticationToken.authenticated(
                    sender,
                    "N/A",
                    AuthorityUtils.createAuthorityList(authorities)
            );
        };
    }
}

