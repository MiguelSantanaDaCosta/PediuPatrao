package com.umc.pediupatrao.config;

import com.umc.pediupatrao.entity.Usuario;
import com.umc.pediupatrao.entity.enums.Role;
import com.umc.pediupatrao.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    @Value("${app.bootstrap.enabled:false}")
    private boolean enabled;

    @Value("${app.bootstrap.admin.username:admin}")
    private String adminUser;
    @Value("${app.bootstrap.admin.password:}")
    private String adminPass;

    @Value("${app.bootstrap.gerente.username:gerente}")
    private String gerenteUser;
    @Value("${app.bootstrap.gerente.password:}")
    private String gerentePass;

    @Value("${app.bootstrap.atendente.username:atendente}")
    private String atendenteUser;
    @Value("${app.bootstrap.atendente.password:}")
    private String atendentePass;

    @Bean
    CommandLineRunner initDatabase(UsuarioRepository repo, PasswordEncoder encoder) {
        return args -> {
            if (!enabled) {
                log.info("[DataInitializer] bootstrap desabilitado (app.bootstrap.enabled=false).");
                return;
            }
            criarSeNaoExistir(repo, encoder, adminUser, adminPass, Role.ADMIN);
            criarSeNaoExistir(repo, encoder, gerenteUser, gerentePass, Role.GERENTE);
            criarSeNaoExistir(repo, encoder, atendenteUser, atendentePass, Role.ATENDENTE);
        };
    }

    private void criarSeNaoExistir(UsuarioRepository repo, PasswordEncoder encoder,
                                   String username, String senha, Role role) {
        if (username == null || username.isBlank()
                || senha == null || senha.isBlank()) {
            log.warn("[DataInitializer] Usuário {} ignorado: credenciais vazias.", username);
            return;
        }
        if (repo.findByUsername(username).isPresent()) {
            return;
        }
        Usuario u = new Usuario();
        u.setUsername(username);
        u.setPassword(encoder.encode(senha));
        u.setRole(role);
        repo.save(u);
        log.info("[DataInitializer] Criado usuário bootstrap: {} ({})", username, role);
    }
}
