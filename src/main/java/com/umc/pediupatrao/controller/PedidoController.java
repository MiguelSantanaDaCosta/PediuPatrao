package com.umc.pediupatrao.controller;

import com.umc.pediupatrao.dto.PedidoRequest;
import com.umc.pediupatrao.entity.Pedido;
import com.umc.pediupatrao.entity.enums.EventoPedido;
import com.umc.pediupatrao.service.PedidoService;
import jakarta.validation.Valid;
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
    public Pedido criar(@Valid @RequestBody PedidoRequest request) {
        return pedidoService.criarPedido(request);
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ATENDENTE','GERENTE','ADMIN')")
    public Pedido processarEvento(@PathVariable String id,
                                  @RequestParam EventoPedido evento) {

        if (evento == EventoPedido.CANCELAR){
            throw new TransicaoInvalidoException(
                    "Use POST /api/pedidos/{id}/cancelar para cancelar o pedido"
                    )
        }
        return pedidoService.processarEvento(id, evento);
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
