package com.umc.pediupatrao.service;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.umc.pediupatrao.audit.AuditContext;
import com.umc.pediupatrao.dto.UsuarioDTO;
import com.umc.pediupatrao.dto.UsuarioResponse;
import com.umc.pediupatrao.entity.Usuario;
import com.umc.pediupatrao.entity.enums.Role;
import com.umc.pediupatrao.entity.enums.TipoOperacao;
import com.umc.pediupatrao.exception.DominioException;
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

    public Iterable<Usuario> listarTodos() {
        return repo.findAll();
    }

    public Page<UsuarioResponse> listar(Pageable pageable) {
        return repo.findAll(pageable).map(mapper::toResponse);
    }


    // Usado pela tela (Thymeleaf)
    public void salvarUsuario(Usuario usuario) {
        if (usuario.getPassword() == null || usuario.getPassword().isBlank()) {
            throw new DominioException("Senha é obrigatória na criação de usuário.");
        }
        if (usuario.getRole() == null) {
            throw new DominioException("O perfil (role) é obrigatório.");
        }
        validarUsernameLivre(usuario.getUsername());
        usuario.setId(null);
        usuario.setPassword(encoder.encode(usuario.getPassword()));
        AuditContext.run(TipoOperacao.INCLUSAO_USUARIO, null, () -> repo.save(usuario));
    }

    // Usado pela API
    public UsuarioResponse criar(UsuarioDTO dto) {
        if (dto.getPassword() == null || dto.getPassword().isBlank()) {
            throw new DominioException("Senha é obrigatória na criação de usuário.");
        }
        validarUsernameLivre(dto.getUsername());
        Usuario u = mapper.toEntity(dto);
        u.setPassword(encoder.encode(dto.getPassword()));
        Usuario salvo = AuditContext.run(TipoOperacao.INCLUSAO_USUARIO, null, () -> repo.save(u));
        return mapper.toResponse(salvo);
    }


    // Usado pela tela (Thymeleaf)
    public void atualizarUsuario(String id, Usuario atualizado) {
        Usuario existente = obter(id);
        if (atualizado.getRole() == null) {
            throw new DominioException("O perfil (role) é obrigatório.");
        }

        validarAlteracaoPerfil(existente, atualizado.getRole());
        if (!existente.getUsername().equals(atualizado.getUsername())) {
            validarUsernameLivre(atualizado.getUsername());
        }

        existente.setUsername(atualizado.getUsername());
        existente.setRole(atualizado.getRole());
        if (atualizado.getPassword() != null && !atualizado.getPassword().isBlank()) {
            existente.setPassword(encoder.encode(atualizado.getPassword()));
        }
        AuditContext.run(TipoOperacao.ALTERACAO_USUARIO, null, () -> repo.save(existente));
    }

    // Usado pela API
    public UsuarioResponse atualizar(String id, UsuarioDTO dto) {
        Usuario u = obter(id);

        validarAlteracaoPerfil(u, dto.getRole());
        if (!u.getUsername().equals(dto.getUsername())) {
            validarUsernameLivre(dto.getUsername());
        }

        mapper.updateEntity(dto, u);
        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            u.setPassword(encoder.encode(dto.getPassword()));
        }
        Usuario salvo = AuditContext.run(TipoOperacao.ALTERACAO_USUARIO, null, () -> repo.save(u));
        return mapper.toResponse(salvo);
    }


    public void deletarUsuario(String id) {
        deletar(id);
    }

    public void deletar(String id) {
        Usuario u = obter(id);

        if (u.getUsername().equals(usuarioLogado())) {
            throw new DominioException("Você não pode excluir o seu próprio usuário.");
        }
        if (u.getRole() == Role.ADMIN && repo.countByRole(Role.ADMIN) <= 1) {
            throw new DominioException("Não é permitido excluir o último ADMIN.");
        }

        AuditContext.executar(TipoOperacao.EXCLUSAO_USUARIO, null, () -> repo.deleteById(id));
    }


    private Usuario obter(String id) {
        return repo.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado."));
    }

    private void validarUsernameLivre(String username) {
        if (repo.findByUsername(username).isPresent()) {
            throw new DominioException("Username já existe.");
        }
    }

    /** Anti-autoelevação e proteção do último ADMIN. */
    private void validarAlteracaoPerfil(Usuario existente, Role novoRole) {
        if (novoRole == existente.getRole()) {
            return;
        }
        if (existente.getUsername().equals(usuarioLogado())) {
            throw new DominioException("Você não pode alterar o seu próprio perfil de acesso.");
        }
        if (existente.getRole() == Role.ADMIN && repo.countByRole(Role.ADMIN) <= 1) {
            throw new DominioException("Não é permitido rebaixar o último ADMIN.");
        }
    }

    private String usuarioLogado() {
        Authentication a = SecurityContextHolder.getContext().getAuthentication();
        if (a == null || !a.isAuthenticated() || "anonymousUser".equals(a.getPrincipal())) {
            return null;
        }
        return a.getName();
    }
}
