package com.umc.pediupatrao.controller;

import com.umc.pediupatrao.dto.UsuarioDTO;
import com.umc.pediupatrao.dto.UsuarioResponse;
import com.umc.pediupatrao.exception.RecursoNaoEncontradoException;
import com.umc.pediupatrao.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/api/usuarios")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Usuários", description = "Gestão de usuários (apenas ADMIN)")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping
    @Operation(summary = "Lista usuários paginados (nunca retorna password)")
    public ResponseEntity<Page<UsuarioResponse>> listarTodos(
            @PageableDefault(size = 20, sort = "username") Pageable pageable) {
        return ResponseEntity.ok(usuarioService.listar(pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca usuário por id (nunca retorna password)")
    public ResponseEntity<UsuarioResponse> buscarPorId(@PathVariable String id) {
        var usuario = usuarioService.buscarPorId(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado."));

        UsuarioResponse response = UsuarioResponse.builder()
                .id(usuario.getId())
                .username(usuario.getUsername())
                .role(usuario.getRole())
                .build();

        return ResponseEntity.ok(response);
    }

    @PostMapping
    @Operation(summary = "Cria usuário")
    public ResponseEntity<UsuarioResponse> criar(
            @Valid @RequestBody UsuarioDTO dto,
            UriComponentsBuilder uri) {
        UsuarioResponse salvo = usuarioService.criar(dto);
        URI location = uri.path("/api/usuarios/{id}")
                .buildAndExpand(salvo.getId())
                .toUri();
        return ResponseEntity.created(location).body(salvo);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza usuário (senha é opcional; se em branco, não altera)")
    public ResponseEntity<UsuarioResponse> atualizar(
            @PathVariable String id,
            @Valid @RequestBody UsuarioDTO dto) {
        return ResponseEntity.ok(usuarioService.atualizar(id, dto));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remove usuário")
    public ResponseEntity<Void> deletar(@PathVariable String id) {
        usuarioService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
