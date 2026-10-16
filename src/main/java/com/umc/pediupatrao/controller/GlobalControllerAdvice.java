package com.umc.pediupatrao.controller;

import java.time.Year;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalControllerAdvice {

    private static Authentication auth() {
        Authentication a = SecurityContextHolder.getContext().getAuthentication();
        if (a == null || !a.isAuthenticated()
                || "anonymousUser".equals(a.getPrincipal())) {
            return null;
        }
        return a;
    }

    @ModelAttribute("usuarioLogado")
    public String usuarioLogado() {
        Authentication a = auth();
        return a == null ? null : a.getName();
    }

    @ModelAttribute("perfilLogado")
    public String perfilLogado() {
        Authentication a = auth();
        if (a == null)
            return null;
        return a.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst()
                .orElse(null);
    }

    @ModelAttribute("anoAtual")
    public int anoAtual() {
        return Year.now().getValue();
    }

    @ModelAttribute("isGerente")
    public boolean isGerente() {
        return temRole("ROLE_GERENTE");
    }

    @ModelAttribute("isAdmin")
    public boolean isAdmin() {
        return temRole("ROLE_ADMIN");
    }

    @ModelAttribute("isGerenteOrAdmin")
    public boolean isGerenteOrAdmin() {
        return temRole("ROLE_GERENTE") || temRole("ROLE_ADMIN");
    }

    private boolean temRole(String role) {
        Authentication a = auth();
        if (a == null)
            return false;
        return a.getAuthorities().stream()
                .anyMatch(x -> x.getAuthority().equals(role));
    }
}
