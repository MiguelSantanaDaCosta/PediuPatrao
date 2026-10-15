package com.umc.pediupatrao.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.umc.pediupatrao.dto.ClienteDTO;
import com.umc.pediupatrao.dto.ClienteResponse;
import com.umc.pediupatrao.entity.Cliente;
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
        return repo.save(cliente);
    }

    public List<Cliente> listarClientes() {
        return repo.findAll();
    }

    public void excluir(String id) {
        repo.deleteById(id);
    }

    public Optional<Cliente> buscarPorId(String id) {
        return repo.findById(id);
    }

    public Cliente salvar(Cliente cliente) {
        if (cliente.getId() == null) {
            return repo.save(cliente);
        }
        if (repo.existsById(cliente.getId())) {
            return repo.save(cliente);
        }
        throw new RecursoNaoEncontradoException("Cliente não encontrado para atualização.");
    }

    public ClienteResponse criar(ClienteDTO dto) {
        Cliente c = mapper.toEntity(dto);
        return mapper.toResponse(repo.save(c));
    }

    public ClienteResponse atualizar(String id, ClienteDTO dto) {
        Cliente c = repo.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Cliente não encontrado."));
        mapper.updateEntity(dto, c);
        return mapper.toResponse(repo.save(c));
    }
}
