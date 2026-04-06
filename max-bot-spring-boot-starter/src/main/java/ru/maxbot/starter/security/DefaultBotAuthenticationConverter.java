package ru.maxbot.starter.security;

import ru.maxbot.core.UpdateContext;
import ru.maxbot.core.model.User;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;

public class DefaultBotAuthenticationConverter implements BotAuthenticationConverter {

    private static final Authentication ANONYMOUS = new AnonymousAuthenticationToken(
            "maxbot-anonymous-key",
            "anonymousUser",
            AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS")
    );

    @Override
    public Authentication convert(UpdateContext context) {
        User sender = context.sender();
        if (sender == null) {
            return ANONYMOUS;
        }
        return UsernamePasswordAuthenticationToken.authenticated(
                sender,
                "N/A",
                AuthorityUtils.createAuthorityList("ROLE_USER")
        );
    }
}

