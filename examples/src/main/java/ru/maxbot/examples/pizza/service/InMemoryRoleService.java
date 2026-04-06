package ru.maxbot.examples.pizza.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;
import ru.maxbot.core.model.User;

@Service
public class InMemoryRoleService {

    private final Map<Long, Set<String>> rolesByUserId = new ConcurrentHashMap<>();

    public List<String> resolveRoles(User user) {
        TreeSet<String> roles = new TreeSet<>();
        roles.add("ROLE_USER");
        if (user == null || user.getUserId() == null) {
            return List.copyOf(roles);
        }
        roles.addAll(rolesByUserId.getOrDefault(user.getUserId(), Set.of()));
        return List.copyOf(roles);
    }

    public List<String> getRoles(long userId) {
        TreeSet<String> roles = new TreeSet<>();
        roles.add("ROLE_USER");
        roles.addAll(rolesByUserId.getOrDefault(userId, Set.of()));
        return List.copyOf(roles);
    }

    public void grantRole(long userId, String role) {
        rolesByUserId.computeIfAbsent(userId, ignored -> ConcurrentHashMap.newKeySet())
                .add(normalizeRole(role));
    }

    public void grantAdminRole(User user) {
        if (user == null || user.getUserId() == null) {
            throw new IllegalArgumentException("Authenticated user is required");
        }
        grantRole(user.getUserId(), "ADMIN");
    }

    public void revokeRole(long userId, String role) {
        Set<String> roles = rolesByUserId.get(userId);
        if (roles == null) {
            return;
        }
        roles.remove(normalizeRole(role));
        if (roles.isEmpty()) {
            rolesByUserId.remove(userId);
        }
    }

    public String describeUser(User user) {
        if (user == null) {
            return "anonymous";
        }
        List<String> parts = new ArrayList<>();
        if (user.getUserId() != null) {
            parts.add("id=" + user.getUserId());
        }
        if (user.getUsername() != null && !user.getUsername().isBlank()) {
            parts.add("username=" + normalizeUsername(user.getUsername()));
        }
        if (parts.isEmpty()) {
            parts.add("unknown");
        }
        return String.join(", ", parts);
    }

    public String formatRoles(Collection<String> roles) {
        return String.join(", ", new TreeSet<>(roles));
    }

    private String normalizeUsername(String username) {
        String normalized = username.strip();
        if (normalized.startsWith("@")) {
            normalized = normalized.substring(1);
        }
        return normalized.toLowerCase(Locale.ROOT);
    }

    private String normalizeRole(String role) {
        if (role == null || role.isBlank()) {
            throw new IllegalArgumentException("Role must not be blank");
        }
        String normalized = role.strip().toUpperCase(Locale.ROOT);
        return normalized.startsWith("ROLE_") ? normalized : "ROLE_" + normalized;
    }
}

