package com.umc.pediupatrao.service;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.umc.pediupatrao.dto.UsuarioDTO;
import com.umc.pediupatrao.dto.UsuarioResponse;
import com.umc.pediupatrao.entity.Usuario;
import com.umc.pediupatrao.exception.RecursoNaoEncontradoException;
import com.umc.pediupatrao.mapper.UsuarioMapper;
import com.umc.pediupatrao.repository.UsuarioRepository;

@Service
public class UsuarioService {

    private final UsuarioRepository repo;
    private final UsuarioMapper mapper;
    private final PasswordEncoder encoder;

    public UsuarioService(UsuarioRepository repo, UsuarioMapper mapper, PasswordEncoder encoder) {
        this.repo = repo;
        this.mapper = mapper;
        this.encoder = encoder;
    }

    public Optional<Usuario> buscarPorId(String id) {
        return repo.findById(id);
    }

    public Optional<Usuario> buscarPorUsername(String username) {
        return repo.findByUsername(username);
    }

    public void salvarUsuario(Usuario usuario) {
        usuario.setPassword(encoder.encode(usuario.getPassword()));
        repo.save(usuario);
    }

    public void deletarUsuario(String id) {
        repo.deleteById(id);
    }

    public Iterable<Usuario> listarTodos() {
        return repo.findAll();
    }

    public Page<UsuarioResponse> listar(Pageable pageable) {
        return repo.findAll(pageable).map(mapper::toResponse);
    }

    public void deletar(String id) {
        repo.deleteById(id);
    }

    public void atualizarUsuario(String id, Usuario usuarioAtualizado) {
        repo.findById(id).ifPresent(usuarioExistente -> {
            usuarioExistente.setUsername(usuarioAtualizado.getUsername());
            usuarioExistente.setRole(usuarioAtualizado.getRole());
            if (usuarioAtualizado.getPassword() != null && !usuarioAtualizado.getPassword().isBlank()) {
                usuarioExistente.setPassword(encoder.encode(usuarioAtualizado.getPassword()));
            }
            repo.save(usuarioExistente);
        });
    }

    public UsuarioResponse criar(UsuarioDTO dto) {
        if (dto.getPassword() == null || dto.getPassword().isBlank()) {
            throw new IllegalArgumentException("Senha é obrigatória na criação de usuário.");
        }
        if (repo.findByUsername(dto.getUsername()).isPresent()) {
            throw new IllegalArgumentException("Username já existe.");
        }
        Usuario u = mapper.toEntity(dto);
        u.setPassword(encoder.encode(dto.getPassword()));
        return mapper.toResponse(repo.save(u));
    }

    public UsuarioResponse atualizar(String id, UsuarioDTO dto) {
        Usuario u = repo.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado."));
        mapper.updateEntity(dto, u);
        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            u.setPassword(encoder.encode(dto.getPassword()));
        }
        return mapper.toResponse(repo.save(u));
    }
}
