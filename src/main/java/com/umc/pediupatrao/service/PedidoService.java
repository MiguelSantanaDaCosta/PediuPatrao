package com.umc.pediupatrao.service;

import com.umc.pediupatrao.entity.AlteracaoCampo;
import com.umc.pediupatrao.entity.ItemPedido;
import com.umc.pediupatrao.entity.Pedido;
import com.umc.pediupatrao.entity.Produto;
import com.umc.pediupatrao.entity.enums.StatusPedido;
import com.umc.pediupatrao.entity.enums.TipoOperacao;
import com.umc.pediupatrao.exception.DescontoInvalidoException;
import com.umc.pediupatrao.exception.RecursoNaoEncontradoException;
import com.umc.pediupatrao.exception.TransicaoInvalidaException;
import com.umc.pediupatrao.repository.ClienteRepository;
import com.umc.pediupatrao.repository.PedidoRepository;
import com.umc.pediupatrao.repository.ProdutoRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final ClienteRepository clienteRepository;
    private final ProdutoRepository produtoRepository;
    private final AuditoriaService auditoriaService;

    public PedidoService(PedidoRepository pedidoRepository,
                         ClienteRepository clienteRepository,
                         ProdutoRepository produtoRepository,
                         AuditoriaService auditoriaService) {
        this.pedidoRepository = pedidoRepository;
        this.clienteRepository = clienteRepository;
        this.produtoRepository = produtoRepository;
        this.auditoriaService = auditoriaService;
    }

    // RN02 criação com cálculo server-side
    public Pedido criarPedido(Pedido pedido) {
        if (pedido.getItens() == null || pedido.getItens().isEmpty()) {
            throw new RecursoNaoEncontradoException("Pedido deve ter ao menos 1 item.");
        }
        if (!clienteRepository.existsById(pedido.getClienteId())) {
            throw new RecursoNaoEncontradoException("Cliente inexistente.");
        }

        for (ItemPedido item : pedido.getItens()) {
            Produto p = produtoRepository.findById(item.getProdutoId())
                    .orElseThrow(() -> new RecursoNaoEncontradoException(
                            "Produto não encontrado: " + item.getProdutoId()));
            if (Boolean.FALSE.equals(p.getAtivo())) {
                throw new RecursoNaoEncontradoException("Produto inativo: " + p.getNome());
            }
            item.setNomeProduto(p.getNome());
            item.setPrecoUnitario(p.getPreco());
            item.recalcularSubtotal();
        }

        pedido.setStatus(StatusPedido.RECEBIDO);
        pedido.setDataHoraEntrada(LocalDateTime.now());
        pedido.setResponsavelRegistro(getUsuarioLogado());
        pedido.setDescontoPercentual(0.0);
        calcularTotal(pedido);

        Pedido salvo = pedidoRepository.save(pedido);

        auditoriaService.registrar(
                TipoOperacao.INCLUSAO_PEDIDO, "Pedido", salvo.getId(),
                List.of(new AlteracaoCampo("valorTotal", null,
                        String.valueOf(salvo.getValorTotal()))),
                null);
        return salvo;
    }

    // RN03
    public Pedido atualizarStatus(String id, StatusPedido novo) {
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Pedido não encontrado."));
        StatusPedido anterior = pedido.getStatus();

        if (anterior.isTerminal()) {
            throw new TransicaoInvalidaException(
                    "Pedido em estado terminal (" + anterior + ") não pode ser alterado.");
        }
        if (!anterior.permiteTransicaoPara(novo)) {
            throw new TransicaoInvalidaException(
                    "Transição inválida de " + anterior + " para " + novo);
        }

        pedido.setStatus(novo);
        if (novo == StatusPedido.SAIU_PARA_ENTREGA || novo == StatusPedido.RETIRADO) {
            pedido.setDataHoraSaida(LocalDateTime.now());
            pedido.setResponsavelSaida(getUsuarioLogado());
        }

        Pedido salvo = pedidoRepository.save(pedido);

        auditoriaService.registrar(
                TipoOperacao.ALTERACAO_STATUS, "Pedido", id,
                List.of(new AlteracaoCampo("status",
                        String.valueOf(anterior), String.valueOf(novo))),
                null);
        return salvo;
    }

    // RN04 desconto restrito ao GERENTE
    @PreAuthorize("hasRole('GERENTE')")
    public Pedido aplicarDesconto(String id, Double percentual) {
        if (percentual == null || percentual < 0 || percentual > 20) {
            throw new DescontoInvalidoException("Desconto deve estar entre 0% e 20%.");
        }
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Pedido não encontrado."));

        if (pedido.getStatus().jaSaiu() || pedido.getStatus().isTerminal()) {
            throw new DescontoInvalidoException(
                    "Desconto não permitido após saída ou em estado terminal.");
        }

        Double anterior = pedido.getDescontoPercentual();
        Double valorAnterior = pedido.getValorTotal();

        pedido.setDescontoPercentual(percentual);
        calcularTotal(pedido);
        Pedido salvo = pedidoRepository.save(pedido);

        List<AlteracaoCampo> mudancas = new ArrayList<>();
        mudancas.add(new AlteracaoCampo("descontoPercentual",
                String.valueOf(anterior), String.valueOf(percentual)));
        mudancas.add(new AlteracaoCampo("valorTotal",
                String.valueOf(valorAnterior), String.valueOf(salvo.getValorTotal())));

        auditoriaService.registrar(TipoOperacao.DESCONTO, "Pedido", id, mudancas, null);
        return salvo;
    }

    // RN04 cancelamento restrito ao GERENTE com justificativa
    @PreAuthorize("hasRole('GERENTE')")
    public Pedido cancelar(String id, String justificativa) {
        if (justificativa == null || justificativa.trim().length() < 5) {
            throw new DescontoInvalidoException(
                    "Justificativa é obrigatória (mínimo 5 caracteres).");
        }
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Pedido não encontrado."));

        if (pedido.getStatus().jaSaiu() || pedido.getStatus().isTerminal()) {
            throw new TransicaoInvalidaException(
                    "Pedido já saiu ou está em estado terminal; não pode ser cancelado.");
        }

        StatusPedido anterior = pedido.getStatus();
        pedido.setStatus(StatusPedido.CANCELADO);
        pedido.setJustificativaCancelamento(justificativa);

        Pedido salvo = pedidoRepository.save(pedido);

        auditoriaService.registrar(
                TipoOperacao.CANCELAMENTO, "Pedido", id,
                List.of(new AlteracaoCampo("status",
                        String.valueOf(anterior), "CANCELADO")),
                justificativa);
        return salvo;
    }

    public List<Pedido> listarPedidos() { return pedidoRepository.findAll(); }

    public Pedido buscarPorId(String id) {
        return pedidoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Pedido não encontrado."));
    }

    private void calcularTotal(Pedido pedido) {
        double bruto = 0.0;
        if (pedido.getItens() != null) {
            for (ItemPedido item : pedido.getItens()) {
                item.recalcularSubtotal();
                bruto += item.getSubtotal();
            }
        }
        pedido.setValorBruto(bruto);
        double desc = pedido.getDescontoPercentual() == null ? 0.0 : pedido.getDescontoPercentual();
        pedido.setValorTotal(bruto * (1 - (desc / 100)));
    }

    private String getUsuarioLogado() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        return (auth != null && auth.isAuthenticated()) ? auth.getName() : "SISTEMA";
    }
}
