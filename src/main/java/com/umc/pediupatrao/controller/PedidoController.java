package com.umc.pediupatrao.controller;

import com.umc.pediupatrao.entity.Pedido;
import com.umc.pediupatrao.entity.enums.StatusPedido;
import com.umc.pediupatrao.service.PedidoService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pedidos")
public class PedidoController {

    private final PedidoService pedidoService;

    public PedidoController(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ATENDENTE','GERENTE','ADMIN')")
    public List<Pedido> listar() {
        return pedidoService.listarPedidos();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ATENDENTE','GERENTE','ADMIN')")
    public Pedido buscar(@PathVariable String id) {
        return pedidoService.buscarPorId(id);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ATENDENTE','GERENTE','ADMIN')")
    public Pedido criar(@RequestBody Pedido pedido) {
        return pedidoService.criarPedido(pedido);
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ATENDENTE','GERENTE','ADMIN')")
    public Pedido atualizarStatus(@PathVariable String id,
                                  @RequestParam StatusPedido status) {
        return pedidoService.atualizarStatus(id, status);
    }

    @PostMapping("/{id}/desconto")
    @PreAuthorize("hasRole('GERENTE')")
    public Pedido aplicarDesconto(@PathVariable String id,
                                  @RequestParam Double percentual) {
        return pedidoService.aplicarDesconto(id, percentual);
    }

    @PostMapping("/{id}/cancelar")
    @PreAuthorize("hasRole('GERENTE')")
    public Pedido cancelar(@PathVariable String id,
                           @RequestParam String justificativa) {
        return pedidoService.cancelar(id, justificativa);
    }
}
