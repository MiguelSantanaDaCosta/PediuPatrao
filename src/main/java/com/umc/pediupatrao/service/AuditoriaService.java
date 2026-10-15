package com.umc.pediupatrao.service;

import com.umc.pediupatrao.entity.AlteracaoCampo;
import com.umc.pediupatrao.entity.Auditoria;
import com.umc.pediupatrao.entity.enums.TipoOperacao;
import com.umc.pediupatrao.repository.AuditoriaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Service
public class AuditoriaService {

    private static final Logger log = LoggerFactory.getLogger(AuditoriaService.class);

    // RN05.5 nunca persistir credenciais
    private static final Set<String> CAMPOS_PROIBIDOS =
            Set.of("senha", "password", "token", "secret", "credencial", "authorization");

    private final AuditoriaRepository repo;

    public AuditoriaService(AuditoriaRepository repo) {
        this.repo = repo;
    }

    public void registrar(TipoOperacao tipo,
                          String entidade,
                          String entidadeId,
                          List<AlteracaoCampo> campos,
                          String justificativa) {
        // RN05.8 — falha de auditoria NÃO derruba a operação principal
        try {
            String usuario = "SISTEMA";
            String perfil = "N/A";
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated()
                    && !"anonymousUser".equals(auth.getPrincipal())) {
                usuario = auth.getName();
                perfil = auth.getAuthorities().stream()
                        .findFirst().map(Object::toString).orElse("N/A");
            }

            Auditoria a = new Auditoria();
            a.setUsuario(usuario);
            a.setPerfil(perfil);
            a.setDataHora(LocalDateTime.now());
            a.setTipoOperacao(tipo);
            a.setEntidade(entidade);
            a.setEntidadeId(entidadeId);
            a.setJustificativa(justificativa);

            if (campos != null) {
                campos.stream()
                        .filter(c -> c.campo() != null)
                        .filter(c -> !CAMPOS_PROIBIDOS.contains(c.campo().toLowerCase()))
                        .forEach(a.getCampos()::add);
            }

            repo.save(a);
        } catch (Exception e) {
            log.error("Falha ao registrar auditoria: {}", e.getMessage(), e);
        }
    }

    public List<Auditoria> listar() {
        return repo.findAll();
    }

    public List<Auditoria> listarPorEntidade(String entidade, String entidadeId) {
        return repo.findByEntidadeAndEntidadeId(entidade, entidadeId);
    }
}
