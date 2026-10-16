package com.umc.pediupatrao.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.umc.pediupatrao.audit.AuditContext;
import com.umc.pediupatrao.dto.ClienteDTO;
import com.umc.pediupatrao.dto.ClienteResponse;
import com.umc.pediupatrao.entity.Cliente;
import com.umc.pediupatrao.entity.enums.TipoOperacao;
import com.umc.pediupatrao.exception.RecursoNaoEncontradoException;
import com.umc.pediupatrao.mapper.ClienteMapper;
import com.umc.pediupatrao.repository.ClienteRepository;

@Service
public class ClienteService {

    private final ClienteRepository repo;
    private final ClienteMapper mapper;

    public ClienteService(ClienteRepository repo, ClienteMapper mapper) {
        this.repo = repo;
        this.mapper = mapper;
    }

    public Cliente novoCliente(Cliente cliente) {
        cliente.setId(null);
        return AuditContext.run(TipoOperacao.INCLUSAO_CLIENTE, null, () -> repo.save(cliente));
    }

    public List<Cliente> listarClientes() {
        return repo.findAll();
    }

    public void excluir(String id) {
        AuditContext.executar(TipoOperacao.EXCLUSAO_CLIENTE, null, () -> repo.deleteById(id));
    }

    public Optional<Cliente> buscarPorId(String id) {
        return repo.findById(id);
    }

    // Usado pela tela (Thymeleaf) e pelo ClienteController
    public Cliente salvar(Cliente cliente) {
        if (cliente.getId() == null || cliente.getId().isBlank()) {
            return novoCliente(cliente);
        }
        if (!repo.existsById(cliente.getId())) {
            throw new RecursoNaoEncontradoException("Cliente não encontrado para atualização.");
        }
        return AuditContext.run(TipoOperacao.ALTERACAO_CLIENTE, null, () -> repo.save(cliente));
    }

    public ClienteResponse criar(ClienteDTO dto) {
        Cliente c = mapper.toEntity(dto);
        Cliente salvo = AuditContext.run(TipoOperacao.INCLUSAO_CLIENTE, null, () -> repo.save(c));
        return mapper.toResponse(salvo);
    }

    public ClienteResponse atualizar(String id, ClienteDTO dto) {
        Cliente c = repo.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Cliente não encontrado."));
        mapper.updateEntity(dto, c);
        Cliente salvo = AuditContext.run(TipoOperacao.ALTERACAO_CLIENTE, null, () -> repo.save(c));
        return mapper.toResponse(salvo);
    }
}
