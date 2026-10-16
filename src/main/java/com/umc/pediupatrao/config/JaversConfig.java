package com.umc.pediupatrao.config;

import com.umc.pediupatrao.audit.AuditContext;
import org.javers.spring.auditable.AuthorProvider;
import org.javers.spring.auditable.CommitPropertiesProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class JaversConfig {

    private static boolean autenticado(Authentication auth) {
        return auth != null && auth.isAuthenticated()
                && !"anonymousUser".equals(auth.getPrincipal());
    }

    private static Map<String, String> propriedades() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Map<String, String> p = new HashMap<>();
        p.put("perfil", autenticado(auth)
                ? auth.getAuthorities().stream().findFirst().map(Object::toString).orElse("N/A")
                : "N/A");
        p.put("operacao", AuditContext.operacao());
        p.put("justificativa", AuditContext.justificativa());
        return p;
    }

    @Bean
    public AuthorProvider authorProvider() {
        return () -> {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            return autenticado(auth) ? auth.getName() : "SISTEMA";
        };
    }

    @Bean
    public CommitPropertiesProvider commitPropertiesProvider() {
        return new CommitPropertiesProvider() {
            @Override
            public Map<String, String> provideForCommittedObject(Object domainObject) {
                return propriedades();
            }

            public Map<String, String> provideForDeletedObject(Object domainObject) {
                return propriedades();
            }

            public Map<String, String> provideForDeleteById(Class<?> domainObjectClass, Object domainObjectId) {
                return propriedades();
            }
        };
    }
}
