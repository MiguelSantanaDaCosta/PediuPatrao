package com.umc.pediupatrao.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.umc.pediupatrao.dto.ProdutoDTO;
import com.umc.pediupatrao.dto.ProdutoResponse;
import com.umc.pediupatrao.entity.Produto;
import com.umc.pediupatrao.exception.RecursoNaoEncontradoException;
import com.umc.pediupatrao.mapper.ProdutoMapper;
import com.umc.pediupatrao.repository.ProdutoRepository;

@Service
public class ProdutoService {

    private final ProdutoRepository repo;
    private final ProdutoMapper mapper;

    public ProdutoService(ProdutoRepository repo, ProdutoMapper mapper) {
        this.repo = repo;
        this.mapper = mapper;
    }

    public Produto novoProduto(Produto produto) {
        return repo.save(produto);
    }

    public List<Produto> listarProdutos() {
        return repo.findAll();
    }

    public void excluir(String id) {
        repo.deleteById(id);
    }

    public Produto salvar(Produto produto) {
        if (produto.getId() == null) {
            return repo.save(produto);
        }
        if (repo.existsById(produto.getId())) {
            return repo.save(produto);
        }
        throw new RecursoNaoEncontradoException("Produto não encontrado para atualização.");
    }

    public ProdutoResponse criar(ProdutoDTO dto) {
        Produto p = mapper.toEntity(dto);
        if (p.getAtivo() == null) {
            p.setAtivo(true);
        }
        return mapper.toResponse(repo.save(p));
    }

    public ProdutoResponse atualizar(String id, ProdutoDTO dto) {
        Produto p = repo.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Produto não encontrado para atualização."));
        mapper.updateEntity(dto, p);
        return mapper.toResponse(repo.save(p));
    }
}
