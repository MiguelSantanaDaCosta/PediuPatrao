package com.umc.pediupatrao.config;

import org.javers.spring.auditable.AuthorProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@Configuration
public class JaversConfig {

    @Bean
    public AuthorProvider authorProvider() {
        return () -> {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated()
                    || "anonymousUser".equals(auth.getPrincipal())) {
                return "SISTEMA";
            }
            String role = auth.getAuthorities().stream()
                    .findFirst().map(Object::toString).orElse("N/A");
            return auth.getName() + "[" + role + "]";
        };
    }
}
