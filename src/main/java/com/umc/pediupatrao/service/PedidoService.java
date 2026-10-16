package com.umc.pediupatrao.service;

import com.umc.pediupatrao.audit.AuditContext;
import com.umc.pediupatrao.dto.ItemPedidoDTO;
import com.umc.pediupatrao.dto.PedidoRequest;
import com.umc.pediupatrao.entity.ItemPedido;
import com.umc.pediupatrao.entity.Pedido;
import com.umc.pediupatrao.entity.Produto;
import com.umc.pediupatrao.entity.enums.EventoPedido;
import com.umc.pediupatrao.entity.enums.StatusPedido;
import com.umc.pediupatrao.entity.enums.TipoOperacao;
import com.umc.pediupatrao.exception.DescontoInvalidoException;
import com.umc.pediupatrao.exception.DominioException;
import com.umc.pediupatrao.exception.JustificativaInvalidaException;
import com.umc.pediupatrao.exception.RecursoNaoEncontradoException;
import com.umc.pediupatrao.exception.TransicaoInvalidaException;
import com.umc.pediupatrao.mapper.PedidoMapper;
import com.umc.pediupatrao.repository.ClienteRepository;
import com.umc.pediupatrao.repository.PedidoRepository;
import com.umc.pediupatrao.repository.ProdutoRepository;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.StateMachineEventResult;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.support.DefaultStateMachineContext;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class PedidoService {

    private final StateMachineFactory<StatusPedido, EventoPedido> stateMachineFactory;
    private final PedidoRepository pedidoRepository;
    private final ClienteRepository clienteRepository;
    private final ProdutoRepository produtoRepository;
    private final PedidoMapper mapper;

    public PedidoService(StateMachineFactory<StatusPedido, EventoPedido> stateMachineFactory,
                         PedidoRepository pedidoRepository,
                         ClienteRepository clienteRepository,
                         ProdutoRepository produtoRepository,
                         PedidoMapper mapper) {
        this.stateMachineFactory = stateMachineFactory;
        this.pedidoRepository = pedidoRepository;
        this.clienteRepository = clienteRepository;
        this.produtoRepository = produtoRepository;
        this.mapper = mapper;
    }

    // RF02 — criação com cálculo server-side
    public Pedido criarPedido(PedidoRequest request) {
        if (!clienteRepository.existsById(request.getClienteId())) {
            throw new RecursoNaoEncontradoException("Cliente inexistente.");
        }

        Pedido pedido = mapper.toEntity(request);

        if (pedido.getItens() == null) {
            pedido.setItens(new ArrayList<>());
        }

        for (int i = 0; i < request.getItens().size(); i++) {
            ItemPedidoDTO dto = request.getItens().get(i);
            ItemPedido item = pedido.getItens().get(i);

            Produto p = produtoRepository.findById(dto.getProdutoId())
                    .orElseThrow(() -> new RecursoNaoEncontradoException(
                            "Produto não encontrado: " + dto.getProdutoId()));
            if (Boolean.FALSE.equals(p.getAtivo())) {
                throw new DominioException("Produto inativo: " + p.getNome());
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

        // JaVers registra a inclusão
        return AuditContext.run(TipoOperacao.INCLUSAO_PEDIDO, null,
                () -> pedidoRepository.save(pedido));
    }

    // RF03 — transição via máquina de estados
    public Pedido processarEvento(String id, EventoPedido evento) {
        if (evento == EventoPedido.CANCELAR) {
            throw new TransicaoInvalidaException(
                    "Cancelamento deve ser feito pelo endpoint de cancelamento (GERENTE).");
        }

        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Pedido não encontrado."));

        StatusPedido anterior = pedido.getStatus();

        StateMachine<StatusPedido, EventoPedido> sm = stateMachineFactory.getStateMachine(id);
        sm.stopReactively().block();
        sm.getStateMachineAccessor().doWithAllRegions(acc ->
                acc.resetStateMachine(new DefaultStateMachineContext<>(anterior, null, null, null)));
        sm.startReactively().block();

        StateMachineEventResult<StatusPedido, EventoPedido> result = sm.sendEvent(
                Mono.just(MessageBuilder.withPayload(evento).build())
        ).blockLast();

        boolean aceito = result != null
                && result.getResultType() == StateMachineEventResult.ResultType.ACCEPTED;

        // Lê o estado ANTES de parar a máquina
        StatusPedido novo = sm.getState().getId();
        sm.stopReactively().block();

        if (!aceito) {
            throw new TransicaoInvalidaException(
                    "Evento " + evento + " não é permitido a partir de " + anterior);
        }

        pedido.setStatus(novo);

        if (novo.jaSaiu()) {
            pedido.setDataHoraSaida(LocalDateTime.now());
            pedido.setResponsavelSaida(getUsuarioLogado());
        }

        return AuditContext.run(TipoOperacao.ALTERACAO_STATUS, null,
                () -> pedidoRepository.save(pedido));
    }

    // RF04 — desconto restrito ao GERENTE
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

        pedido.setDescontoPercentual(percentual);
        calcularTotal(pedido);

        return AuditContext.run(TipoOperacao.DESCONTO, null,
                () -> pedidoRepository.save(pedido));
    }

    // RF04 — cancelamento restrito ao GERENTE com justificativa
    @PreAuthorize("hasRole('GERENTE')")
    public Pedido cancelar(String id, String justificativa) {
        if (justificativa == null || justificativa.trim().length() < 5) {
            throw new JustificativaInvalidaException(
                    "Justificativa é obrigatória (mínimo 5 caracteres).");
        }

        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Pedido não encontrado."));

        if (pedido.getStatus().jaSaiu() || pedido.getStatus().isTerminal()) {
            throw new TransicaoInvalidaException(
                    "Pedido já saiu ou está em estado terminal; não pode ser cancelado.");
        }

        String just = justificativa.trim();
        pedido.setStatus(StatusPedido.CANCELADO);
        pedido.setJustificativaCancelamento(just);

        return AuditContext.run(TipoOperacao.CANCELAMENTO, just,
                () -> pedidoRepository.save(pedido));
    }

    public List<Pedido> listarPedidos() {
        return pedidoRepository.findAll();
    }

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
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()
                || "anonymousUser".equals(auth.getPrincipal())) {
            return "SISTEMA";
        }
        return auth.getName();
    }
}
