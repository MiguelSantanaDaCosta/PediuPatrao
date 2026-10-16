package com.umc.pediupatrao.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.umc.pediupatrao.service.AuditoriaService;

@Controller
@PreAuthorize("hasAnyRole('ADMIN','GERENTE')")
public class AuditoriaViewController {

    private final AuditoriaService auditoriaService;

    public AuditoriaViewController(AuditoriaService auditoriaService) {
        this.auditoriaService = auditoriaService;
    }

    @GetMapping("/auditoria")
    public String listar(@RequestParam(required = false) String entidade, Model model) {
        model.addAttribute("auditorias",
                (entidade == null || entidade.isBlank())
                        ? auditoriaService.listar()
                        : auditoriaService.listarPorEntidade(entidade));
        model.addAttribute("filtro", entidade == null ? "" : entidade);
        model.addAttribute("content", "auditoria/lista :: content");
        return "layout";
    }
}
