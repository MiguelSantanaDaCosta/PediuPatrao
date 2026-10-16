package com.umc.pediupatrao.controller;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.umc.pediupatrao.dto.RegistroAuditoria;
import com.umc.pediupatrao.service.AuditoriaService;

/** Somente consulta (GET). Não existe endpoint de alteração ou exclusão. */
@RestController
@RequestMapping("/api/auditoria")
@PreAuthorize("hasAnyRole('ADMIN','GERENTE')")
public class AuditoriaController {

    private final AuditoriaService auditoriaService;

    public AuditoriaController(AuditoriaService auditoriaService) {
        this.auditoriaService = auditoriaService;
    }

    // GET /api/auditoria?entidade=Cliente&entidadeId=...
    @GetMapping
    public List<RegistroAuditoria> listar(@RequestParam(required = false) String entidade,
                                          @RequestParam(required = false) String entidadeId) {
        if (entidade != null && entidadeId != null) {
            return auditoriaService.listarPorInstancia(entidade, entidadeId);
        }
        if (entidade != null) {
            return auditoriaService.listarPorEntidade(entidade);
        }
        return auditoriaService.listar();
    }

    /** Histórico completo (time-machine) de um pedido. */
    @GetMapping("/pedidos/{id}")
    public List<RegistroAuditoria> historicoPedido(@PathVariable String id) {
        return auditoriaService.listarPorInstancia("Pedido", id);
    }

    @GetMapping("/clientes/{id}")
    public List<RegistroAuditoria> historicoCliente(@PathVariable String id) {
        return auditoriaService.listarPorInstancia("Cliente", id);
    }
}
